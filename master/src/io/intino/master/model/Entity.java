package io.intino.master.model;

import java.util.HashMap;
import java.util.Map;

public class Entity {
	private final String id;
	public Map<String, String> attributes = new HashMap<>();

	public Entity(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}
}
