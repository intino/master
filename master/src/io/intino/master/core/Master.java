package io.intino.master.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.master.io.TriplesFileReader;
import io.intino.master.io.TriplesFileWriter;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializer;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

public class Master {

	public static final String METADATA_MAP_NAME = "metadata";
	public static final String MASTER_MAP_NAME = "master";
	public static final String REQUESTS_TOPIC = "requests";
	public static final String MESSAGE_SEPARATOR = "##";
	public static final String NONE_TYPE = "";

	protected HazelcastInstance hazelcast;
	protected final MasterConfig config;
	protected IMap<String, String> metadataMap;
	protected IMap<String, String> masterMap;

	public Master(MasterConfig config) {
		this.config = requireNonNull(config);
		checkConfigValues();
	}

	public void start() {
		Logger.info("Initializing master...");
		initHazelcast();
		initMaps();
		loadConfig();
		loadData();
		setupListeners();
		Logger.info("Master initialized");
	}

	private void initHazelcast() {
		hazelcast = Hazelcast.newHazelcastInstance(getHazelcastConfig());
		Runtime.getRuntime().addShutdownHook(new Thread(() -> hazelcast.shutdown(), "Master-Shutdown"));
	}

	protected void initMaps() {
		metadataMap = hazelcast.getMap(METADATA_MAP_NAME);
		masterMap = hazelcast.getMap(MASTER_MAP_NAME);
	}

	private void loadConfig() {
		metadataMap.set("instanceName", config.instanceName());
		metadataMap.set("port", String.valueOf(config.port()));
		metadataMap.set("host", config.host());
		metadataMap.set("serializer", config.serializer().name());
		metadataMap.set("dataDirectory", config.dataDirectory().getPath());
	}

	protected void loadData() {
		Logger.info("Loading data...");
		long start = System.currentTimeMillis();
		int numTriples;

		{
			Map<String, Map<String, String>> records = new HashMap<>(32 * 1024);

			List<Triple> triples = readTriplesIn(config.dataDirectory());
			numTriples = triples.size();

			triples.forEach(t -> records.computeIfAbsent(t.subject(), k -> new HashMap<>()).put(t.predicate(), t.value()));

			masterMap.setAll(records.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> serializer().serialize(e.getValue()))));
		}

		final long time = System.currentTimeMillis() - start;
		System.gc();

		Logger.info("Data loaded after " + time + " ms."
				+ " Num records: " + masterMap.size()
				+ ", Num triples: " + numTriples
				+ ", Memory used: " + masterMap.getLocalMapStats().getOwnedEntryMemoryCost() / 1024.0f / 1024.0f + " MB"
		);
	}

	protected void setupListeners() {
		hazelcast.getTopic(REQUESTS_TOPIC).addMessageListener(this::handleRequestMessage);
	}

	protected List<Triple> readTriplesIn(File folder) {
		return new TriplesFileReader(folder).triples();
	}

	protected void handleRequestMessage(Message<Object> message) {
		String[] info = message.getMessageObject().toString().split(MESSAGE_SEPARATOR, -1);
		Triple triple = new Triple(info[1]);
		if(add(triple)) save(info[0], triple);
	}

	protected boolean add(Triple triple) {
		Map<String, String> record = getRecord(triple.subject());
		if(Objects.equals(record.get(triple.predicate()), triple.value())) return false;
		record.put(triple.predicate(), triple.value());
		updateRecord(triple.subject(), record);
		return true;
	}

	protected void updateRecord(String id, Map<String, String> record) {
		masterMap.set(id, serializer().serialize(record));
	}

	protected Map<String, String> getRecord(String subject) {
		String serializedRecord = masterMap.get(subject);
		if(serializedRecord == null) return new HashMap<>();
		return serializer().deserialize(serializedRecord);
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

	protected Config getHazelcastConfig() {
		Config hzConfig = new Config();
		hzConfig.setInstanceName(config.instanceName());
		hzConfig.setNetworkConfig(new NetworkConfig().setPort(config.port()));
		return hzConfig;
	}

	private void checkConfigValues() {
		if(config.instanceName() == null) throw new MasterInitializationException("Instance name cannot be null");
		if(config.dataDirectory() == null) throw new MasterInitializationException("Data directory cannot be null");
		if(config.port() <= 0) throw new MasterInitializationException("Port is invalid");
		if(config.serializer() == null) throw new MasterInitializationException("Serializer cannot be null");
		if(config.serializer().name() == null) throw new MasterInitializationException("Serializer name cannot be null");
	}
}