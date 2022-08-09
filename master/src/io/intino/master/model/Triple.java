package io.intino.master.model;

public class Triple {
	public static final String SEPARATOR = "\t";
	private final String subject, predicate, value;

	public Triple(String line) {
		this(line.split(SEPARATOR, -1));
	}

	public Triple(String[] split) {
		this(split[0], split[1], split[2]);
	}

	public Triple(String subject, String predicate, String value) {
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
	}

	public String subject() {
		return subject;
	}

	public String predicate() {
		return predicate;
	}

	public String value() {
		return value;
	}

	@Override
	public String toString() {
		return subject + SEPARATOR + predicate + SEPARATOR + value;
	}
}
