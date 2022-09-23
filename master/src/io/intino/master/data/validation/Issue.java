package io.intino.master.data.validation;

import static io.intino.master.data.validation.TripleSource.FileTripleSource;
import static io.intino.master.data.validation.TripleSource.PublisherTripleSource;

public class Issue implements Comparable<Issue> {

	public static Issue error(String message) {
		return new Issue(Level.Error, message);
	}

	public static Issue warning(String message) {
		return new Issue(Level.Warning, message);
	}

	private final Level level;
	private final String message;
	private ValidationLayers.Scope scope;
	private TripleSource source;

	public Issue(Level level, String message) {
		this.level = level;
		this.message = message;
	}

	public Level level() {
		return level;
	}

	public String message() {
		return message;
	}

	public Issue scope(ValidationLayers.Scope scope) {
		this.scope = scope;
		return this;
	}

	public TripleSource source() {
		return source;
	}

	public Issue source(TripleSource source) {
		this.source = source;
		return this;
	}

	@Override
	public String toString() {
		return level + ": " + message + (source == null ? "" : "\n\t" + source.get());
	}

	@Override
	public int compareTo(Issue o) {
		if(o == null) return -1;
		return level == o.level ? compareSources(o.source) : level.compareTo(o.level);
	}

	private int compareSources(TripleSource otherSource) {
		if(source == null) return 1;
		if(otherSource == null) return -1;
		if(source instanceof PublisherTripleSource) return 1;
		if(otherSource instanceof PublisherTripleSource) return 1;
		if(source instanceof FileTripleSource && otherSource instanceof FileTripleSource)
			return Integer.compare(((FileTripleSource) source).line(), ((FileTripleSource) otherSource).line());
		return 0;
	}

	public enum Level {
		Error, Warning
	}
}
