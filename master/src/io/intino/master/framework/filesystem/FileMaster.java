package io.intino.master.framework.filesystem;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.Message;
import io.intino.alexandria.logger.Logger;

import java.io.File;
import java.time.Instant;
import java.util.Map;

import static io.intino.master.filesystem.Triple.Tab;
import static java.util.stream.Collectors.toMap;

public class FileMaster {

	private final File folder;
	private HazelcastInstance hz;
	private IMap<String, Object> triples;
	private IMap<Object, String> subjects2Hex;
	private IMap<String, Object> hex2subjects;
	private IMap<Object, String> predicates2Hex;
	private IMap<String, Object> hex2predicates;

	public FileMaster(File folder) {
		this.folder = folder;
	}

	public void start() {
		hz = Hazelcast.newHazelcastInstance();
		initMaps();
		Logger.info(Instant.now() + ": Loading data");
		triples.putAll(new TriplesFileReader(folder).triples()
				.map(t -> new Triple(subjectCode(t.subject()), predicateCode(t.predicate()), value(t.value())))
				.collect(toMap(t -> t.subject() + Tab + t.predicate(), Triple::value)));
		this.hex2subjects.putAll(subjects2Hex.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		this.hex2predicates.putAll(predicates2Hex.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey)));
		Logger.info(Instant.now() + ": Data loaded");
	}

	private void initMaps() {
		triples = initStringObjectMap("master");
		subjects2Hex = initObjectStringMap("subjects");
		predicates2Hex = initObjectStringMap("predicates");
		hex2subjects = initStringObjectMap("hex2subjects");
		hex2predicates = initStringObjectMap("hex2predicates");
	}

	private IMap<String, Object> initStringObjectMap(String name) {
		IMap<String, Object> map = hz.getMap(name);
		map.evictAll();
		return map;
	}

	private IMap<Object, String> initObjectStringMap(String name) {
		IMap<Object, String> map = hz.getMap(name);
		map.evictAll();
		return map;
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
		if (value.startsWith("$") || value.startsWith("#")) return value;
		return subjectCode(value) + "";
	}
}
