package io.intino.master.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Entity {

	private final String id;
	private final Map<String, String> unmappedAttributes = new HashMap<>(3);

	public Entity(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}

	public Entity add(Triple t) {
		unmappedAttributes.put(t.predicate(), t.value());
		return this;
	}

	public Entity remove(Triple t) {
		unmappedAttributes.remove(t.predicate());
		return this;
	}

	public Map<String, String> unmappedAttributes() {
		return Collections.unmodifiableMap(unmappedAttributes);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Entity entity = (Entity) o;
		return Objects.equals(id, entity.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return id;
	}
}
