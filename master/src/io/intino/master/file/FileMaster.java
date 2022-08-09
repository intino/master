package io.intino.master.file;

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
	private IMap<String, String> triples;
	private IMap<String, String> subjects2Hex;
	private IMap<String, String> predicates2Hex;

	public FileMaster(File folder) {
		this.folder = folder;
	}

	public void start() {
		HazelcastInstance hz = Hazelcast.newHazelcastInstance();
		triples = hz.getMap("master");
		subjects2Hex = hz.getMap("subjects");
		predicates2Hex = hz.getMap("predicates");
		Logger.info(Instant.now() + ": Loading data");
		triples.putAll(new TriplesFileReader(folder).triples()
				.map(t -> new Triple(subjectCode(t.subject()), predicateCode(t.predicate()), value(t.value())))
				.collect(toMap(t -> t.subject() + SEPARATOR + t.predicate(), Triple::value)));
		hz.<String, String>getMap("hex2subjects")
				.putAll(subjects2Hex.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		hz.<String, String>getMap("hex2predicates")
				.putAll(predicates2Hex.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		Logger.info(Instant.now() + ": Data loaded");
	}


	private String hex(Object integer) {
		return Integer.toHexString((Integer) integer);
	}

	private void handleMessage(Message<Object> message) {
		String[] split = message.getMessageObject().toString().split(":", -1);
		addAndSave(split[0], new Triple(split[1]));
	}

	private void addAndSave(String sender, Triple triple) {
		new TriplesFileWriter(folder, sender).write(triple);
		add(triple);
	}

	public void add(Triple triple) {
		String subjectCode = subjectCode(triple.subject());
		String predicateCode = predicateCode(triple.predicate());
		String value = value(triple.value());
		triples.put(subjectCode + "_" + predicateCode, value);
	}

	private String subjectCode(String subject) {
		if (!subjects2Hex.containsKey(subject)) synchronized (subjects2Hex) {
			subjects2Hex.put(subject, hex(subjects2Hex.size()));
		}
		return subjects2Hex.get(subject);
	}

	private String predicateCode(String predicate) {
		if (!predicates2Hex.containsKey(predicate)) synchronized (predicates2Hex) {
			predicates2Hex.put(predicate, hex(predicates2Hex.size()));
		}
		return predicates2Hex.get(predicate);
	}

	private String value(String value) {
		return value.startsWith("$") || value.startsWith("#") ? value : subjectCode(value) + "";
	}
}
