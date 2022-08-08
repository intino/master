package io.intino.master.framework.filesystem;

public class Triple {

	public static final String Tab = "\t";
	private final String subject, predicate, value;

	public Triple(String line) {
		this(line.split(Tab, -1));
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

	public String predicateValue() {
		return predicate + Tab + value;
	}

	@Override
	public String toString() {
		return subject + Tab + predicateValue();
	}
}
