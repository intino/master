package io.intino.master.model;

import java.util.Map;
import java.util.stream.Stream;

public class TripleRecord {

	private final String id;
	private final Map<String, String> attributes;

	public TripleRecord(String id, Map<String, String> attributes) {
		this.id = id;
		this.attributes = attributes;
	}

	public String id() {
		return id;
	}

	public String type() {
		return Triple.typeOf(id);
	}

	public Map<String, String> attributes() {
		return attributes;
	}

	public Stream<Triple> triples() {
		return attributes.entrySet().stream().map(e -> new Triple(id, e.getKey(), e.getValue()));
	}
}
