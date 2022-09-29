package io.intino.master.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.master.data.DataLoader;
import io.intino.master.data.validation.*;
import io.intino.master.data.validation.report.IssueReport;
import io.intino.master.io.TriplesFileReader;
import io.intino.master.io.TriplesFileWriter;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializer;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class Master {

	public static final String METADATA_MAP_NAME = "metadata";
	public static final String MASTER_MAP_NAME = "master";
	public static final String REQUESTS_TOPIC = "requests";
	public static final String MESSAGE_SEPARATOR = "##";
	public static final String NONE_TYPE = "";

	private HazelcastInstance hazelcast;
	private final MasterConfig config;
	private IMap<String, String> metadataMap;
	private IMap<String, String> masterMap;
	private volatile ValidationLayers validationLayers;

	public Master(MasterConfig config) {
		this.config = requireNonNull(config);
		checkConfigValues();
		validationLayers = config.validationLayers() == null ? new ValidationLayers() : config.validationLayers();
	}

	public void start() {
		Logger.info("Initializing Master...");
		{
			Map<String, String> data = loadData();
			initHazelcast();
			initMaps(data);
			setupListeners();
		}
		System.gc();
		Logger.info("Data loaded into Master:\n" + histogram());
		Logger.info("Master initialized. Using " + getHazelcastMemoryUsedMB() + " MB");
	}

	private String histogram() {
		Map<String, Integer> histogram = new HashMap<>();
		masterMap.keySet().stream().map(Triple::typeOf).map(t -> "\"" + t + "\"").forEach(key -> histogram.compute(key, (k, v) -> v == null ? 1 : v + 1));
		return "  " + histogram.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n  "));
	}

	private void initHazelcast() {
		Logger.info("Initializing hazelcast instance...");
		hazelcast = Hazelcast.newHazelcastInstance(getHazelcastConfig());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> hazelcast.shutdown(), "Master-Shutdown"));
	}

	protected void initMaps(Map<String, String> data) {
		metadataMap = hazelcast.getMap(METADATA_MAP_NAME);
		metadataMap.set("instanceName", config.instanceName());
		metadataMap.set("port", String.valueOf(config.port()));
		metadataMap.set("host", config.host());
		metadataMap.set("serializer", config.serializer().name());
		metadataMap.set("dataDirectory", config.dataDirectory().getPath());
		metadataMap.set("logDirectory", config.logDirectory().getPath());

		masterMap = hazelcast.getMap(MASTER_MAP_NAME);
		masterMap.setAll(data.entrySet().stream().collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	protected Map<String, String> loadData() {
		Logger.info("Loading data...");
		long start = System.currentTimeMillis();
		int numTriples;

		DataLoader.MasterLoadResult result = DataLoader.load(config.dataDirectory(), validationLayers, serializer());

		if(result.issues().errorCount() > 0)
			loadFail(result);
		else if(result.issues().warningCount() > 0)
			saveIssuesReport(result.issues());

		numTriples = (int) result.triplesRead();

		final long time = System.currentTimeMillis() - start;

		Logger.info("Data loaded after " + time + " ms."
				+ " Num records: " + result.data().size()
				+ ", Num triples: " + numTriples
		);

		return result.data();
	}

	private void saveIssuesReport(IssueReport issues) {
		issues.save(issuesFile());
	}

	private void loadFail(DataLoader.MasterLoadResult result) {
		saveIssuesReport(result.issues());
		throw new MasterInitializationException("Failed to load data because there were " + result.issues().errorCount() + " errors. See " + issuesFile() + " for more info");
	}

	private File issuesFile() {
		return new File(config.logDirectory(), config.instanceName() + "_issues_report.html");
	}

	protected void setupListeners() {
		hazelcast.getTopic(REQUESTS_TOPIC).addMessageListener(this::handleRequestMessage);
		Logger.info("Hazelcast initialized");
	}

	protected List<Triple> readTriplesIn(File folder) {
		return new TriplesFileReader(folder).triples();
	}

	protected void handleRequestMessage(Message<Object> message) {
		String[] info = message.getMessageObject().toString().split(MESSAGE_SEPARATOR, -1);
		String publisher = info[0];
		String tripleLine = info[1];
		if(add(publisher, tripleLine))
			save(publisher, tripleLine);
	}

	public boolean add(String publisher, String tripleLine) {
		if(isInvalid(tripleLine, TripleSource.ofPublisher(publisher))) return false;
		return add(publisher, new Triple(tripleLine));
	}

	public boolean add(String publisher, Triple triple) {
		if(isInvalid(triple.toString(), TripleSource.ofPublisher(publisher))) return false;

		Map<String, String> record = getRecord(triple.subject());
		if(Objects.equals(record.get(triple.predicate()), triple.value())) return false;

		record.put(triple.predicate(), triple.value());

		if(!validate(triple.subject(), record)) return false;

		updateRecord(triple.subject(), record);
		return true;
	}

	private boolean validate(String id, Map<String, String> record) {
		List<Issue> issues = validationLayers.recordValidationLayer().validate(asValidationRecord(id, record), store()).collect(Collectors.toList());
		for(Issue issue : issues) {
			if(issue.level() == Issue.Level.Warning) Logger.warn(issue.toString());
			if(issue.level() == Issue.Level.Error) Logger.error(issue.toString());
		}
		return issues.stream().noneMatch(i -> i.level() == Issue.Level.Error);
	}

	private TripleRecordStore store() {
		return new TripleRecordStore() {
			@Override
			public RecordValidator.TripleRecord get(String id) {
				return asValidationRecord(id, getRecord(id));
			}

			@Override
			public Stream<RecordValidator.TripleRecord> stream() {
				MasterSerializer serializer = serializer();
				return masterMap.entrySet().stream().map(e -> asValidationRecord(e.getKey(), serializer.deserialize(e.getValue())));
			}
		};
	}

	private RecordValidator.TripleRecord asValidationRecord(String id, Map<String, String> record) {
		RecordValidator.TripleRecord r = new RecordValidator.TripleRecord(id);
		record.forEach((k, v) -> r.add(k, new RecordValidator.TripleRecord.Value(v)));
		return r;
	}

	protected void updateRecord(String id, Map<String, String> record) {
		masterMap.set(id, serializer().serialize(record));
	}

	protected Map<String, String> getRecord(String subject) {
		String serializedRecord = masterMap.get(subject);
		if(serializedRecord == null) return new HashMap<>();
		return serializer().deserialize(serializedRecord);
	}

	private boolean isInvalid(String tripleLine, TripleSource source) {
		List<Issue> issues = validationLayers.tripleValidationLayer().validate(tripleLine, source).collect(Collectors.toList());
		for(Issue issue : issues) {
			if(issue.level() == Issue.Level.Warning) Logger.warn(issue.toString());
			if(issue.level() == Issue.Level.Error) Logger.error(issue.toString());
		}
		return issues.stream().anyMatch(i -> i.level() == Issue.Level.Error);
	}

	private synchronized void save(String publisher, String tripleLine) {
		save(publisher, new Triple(tripleLine));
	}

	protected synchronized void save(String publisher, Triple triple) {
		try(TriplesFileWriter writer = new TriplesFileWriter(config.dataDirectory(), publisher)) {
			writer.write(triple);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private MasterSerializer serializer() {
		return config.serializer();
	}

	public ValidationLayers validationLayers() {
		return validationLayers;
	}

	public Master validationLayers(ValidationLayers validationLayers) {
		this.validationLayers = validationLayers;
		return this;
	}

	protected Config getHazelcastConfig() {
		Config hzConfig = new Config();
		hzConfig.setInstanceName(config.instanceName());
		hzConfig.setNetworkConfig(new NetworkConfig().setPort(config.port()));
		return hzConfig;
	}

	private void checkConfigValues() {
		if(config.instanceName() == null) throw new MasterInitializationException("Instance name cannot be null");
		if(config.dataDirectory() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.logDirectory() == null) throw new MasterInitializationException("Log directory cannot be null");
		if(config.port() <= 0) throw new MasterInitializationException("Port is invalid");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
	}

	private float getHazelcastMemoryUsedMB() {
		long metadata = metadataMap.getLocalMapStats().getOwnedEntryMemoryCost();
		long data = masterMap.getLocalMapStats().getOwnedEntryMemoryCost();
		return (metadata + data) / 1024.0f / 1024.0f;
	}
}