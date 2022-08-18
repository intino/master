package io.intino.master.file;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;

import java.io.File;
import java.time.Instant;
import java.util.Map;

import static io.intino.master.model.Triple.SEPARATOR;
import static java.util.stream.Collectors.toMap;

public class FileMaster {
	private final File folder;
	private HazelcastInstance hz;
	private IMap<String, String> values;
	private IMap<String, String> subjectFactors;
	private IMap<String, String> predicateFactors;

	public FileMaster(File folder) {
		this.folder = folder;
	}

	public void start() {
		Config config = new Config();
		hz = Hazelcast.newHazelcastInstance(config);
		values = hz.getMap("master");
		subjectFactors = hz.getMap("subjects");
		predicateFactors = hz.getMap("predicates");
		Logger.info(Instant.now() + ": Loading data");
		values.putAll(new TriplesFileReader(folder).triples()
				.map(t -> new Triple(subjectFactor(t.subject()), predicateFactor(t.predicate()), value(t.value())))
				.collect(toMap(t -> t.subject() + SEPARATOR + t.predicate(), Triple::value, (k1, k2) -> k1)));
		hz.<String, String>getMap("reverseSubjectFactors")
				.putAll(subjectFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		hz.<String, String>getMap("reversePredicateFactors")
				.putAll(predicateFactors.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		Logger.info(Instant.now() + ": Data loaded");
		hz.getTopic("requests").addMessageListener(this::handleMessage);
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

	private String subjectFactor(String subject) {
		if (!subjectFactors.containsKey(subject)) synchronized (subjectFactors) {
			subjectFactors.put(subject, hex(subjectFactors.size()));
		}
		return subjectFactors.get(subject);
	}

	private String predicateFactor(String predicate) {
		if (!predicateFactors.containsKey(predicate)) synchronized (predicateFactors) {
			predicateFactors.put(predicate, hex(predicateFactors.size()));
		}
		return predicateFactors.get(predicate);
	}

	private String value(String value) {
		return value.startsWith("$") || value.startsWith("#") ? subjectFactor(value) + "" : value;
	}
}