package io.intino.master.model;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private final String id;
	public Map<String, String> unmappedAttributes = new HashMap<>(3);

	public Entity(String id) {
		this.id = id;
	}


	public Entity add(Triple t) {
		unmappedAttributes.put(t.predicate(), t.value());
		return this;
	}

	public Entity remove(Triple t) {
		unmappedAttributes.remove(t.predicate());
		return this;
	}

	public String id() {
		return id;
	}
}
