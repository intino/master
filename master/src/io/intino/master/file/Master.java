package io.intino.master.file;

import com.google.gson.Gson;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.intino.master.model.Triple.SEPARATOR;
import static java.util.stream.Collectors.toMap;

public class Master {

	protected final File folder;
	protected HazelcastInstance hz;
	protected IMap<String, Object> values;
	protected IMap<String, String> subjectFactors;
	protected IMap<String, String> predicateFactors;
	protected IMap<String, String> reverseSubjectFactors;
	protected IMap<String, String> reversePredicateFactors;

	public Master(File folder) {
		this.folder = folder;
	}

	public void start() {
		hz = Hazelcast.newHazelcastInstance(new Config());

		loadData();
//		clear();
//
//		loadData2();
//		clear();
//
//		loadData3();
//		clear();

//		loadData4();
//		clear();

//		loadData5();

		setupListeners();
	}

	private void initMaps() {
		values = hz.getMap("master");
		subjectFactors = hz.getMap("subjects");
		predicateFactors = hz.getMap("predicates");
		reverseSubjectFactors = hz.getMap("reverseSubjectFactors");
		reversePredicateFactors = hz.getMap("reversePredicateFactors");
	}

	private void loadData() {
		initMaps();

		long start = System.currentTimeMillis();
		Logger.info("Loading data (1)");

		values.setAll(readTriplesIn(folder)
				.map(t -> new Triple(subjectFactor(t.subject()), predicateFactor(t.predicate()), value(t.value())))
				.collect(toMap(t -> t.subject() + SEPARATOR + t.predicate(), Triple::value, (k1, k2) -> k1)));

		reverseSubjectFactors.setAll(subjectFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));

		reversePredicateFactors.setAll(predicateFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));

		long time = System.currentTimeMillis() - start;
		Logger.info("Data loaded in " + time + " ms\n" + memoryUsedStats());
	}

	private void setupListeners() {
		hz.getTopic("requests").addMessageListener(this::handleMessage);
	}

	public String memoryUsedStats() {
		StringBuilder sb = new StringBuilder();

		long valuesMem = values.getLocalMapStats().getOwnedEntryMemoryCost();
		long subjectsMem = subjectFactors == null ? 0 : subjectFactors.getLocalMapStats().getOwnedEntryMemoryCost();
		long predicatesMem = predicateFactors == null ? 0 : predicateFactors.getLocalMapStats().getOwnedEntryMemoryCost();
		long reverseSubjectsMem = reverseSubjectFactors == null ? 0 : reverseSubjectFactors.getLocalMapStats().getOwnedEntryMemoryCost();
		long reversePredicatesMem = reversePredicateFactors == null ? 0 : reversePredicateFactors.getLocalMapStats().getOwnedEntryMemoryCost();

		sb.append("Number of entries: ").append(values.size()).append('\n');
		sb.append("values size in memory: ").append(valuesMem / 1024.0f / 1024.0f).append(" MB").append('\n');
		sb.append("subjectFactors size in memory: ").append(subjectsMem / 1024.0f / 1024.0f).append(" MB").append('\n');
		sb.append("predicateFactors size in memory: ").append(predicatesMem / 1024.0f / 1024.0f).append(" MB").append('\n');
		sb.append("reverseSubjectFactors size in memory: ").append(reverseSubjectsMem / 1024.0f / 1024.0f).append(" MB").append('\n');
		sb.append("reversePredicateFactors size in memory: ").append(reversePredicatesMem / 1024.0f / 1024.0f).append(" MB").append('\n');
		sb.append("Total size in memory: ").append((valuesMem + subjectsMem + predicatesMem + reverseSubjectsMem + reversePredicatesMem) / 1024.0f / 1024.0f).append(" MB\n");

		return sb.toString();
	}

	protected Stream<Triple> readTriplesIn(File folder) {
		return new TriplesFileReader(folder).triples();
	}

	private String hex(Object integer) {
		return Integer.toHexString((Integer) integer);
	}

	private void handleMessage(Message<Object> message) {
		String[] split = message.getMessageObject().toString().split("##", -1);
		addAndSave(split[0], new Triple(split[1]));
	}

	private void addAndSave(String sender, Triple triple) {
		if (add(triple)) new TriplesFileWriter(folder, sender).write(triple);
	}

	public boolean add(Triple triple) {
		String key = subjectFactor(triple.subject()) + SEPARATOR + predicateFactor(triple.predicate());
		String value = value(triple.value());
		if (!value.equals(values.get(key))) {
			values.put(key, value);
			subjectFactors.put(triple.subject(), subjectFactor(triple.subject()));
			predicateFactors.put(triple.predicate(), predicateFactor(triple.subject()));
			hz.<String, String>getMap("reversePredicateFactors").put(predicateFactor(triple.subject()), triple.predicate());
			hz.<String, String>getMap("reverseSubjectFactors").put(subjectFactor(triple.subject()), triple.subject());
			return true;
		}
		return false;
	}

	protected String subjectFactor(String subject) {
		if (!subjectFactors.containsKey(subject)) registerSubjectFactor(subject);
		return subjectFactors.get(subject);
	}

	private synchronized void registerSubjectFactor(String subject) {
		subjectFactors.set(subject, hex(subjectFactors.size()));
	}

	protected String predicateFactor(String predicate) {
		if (!predicateFactors.containsKey(predicate)) registerPredicateFactor(predicate);
		return predicateFactors.get(predicate);
	}

	private synchronized void registerPredicateFactor(String predicate) {
		predicateFactors.set(predicate, hex(predicateFactors.size()));
	}

	protected String value(String value) {
		return value.startsWith("$") || value.startsWith("#") ? subjectFactor(value) : value;
	}
}