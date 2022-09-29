package io.intino.master.data.validation.validators;

import io.intino.master.data.validation.Issue;
import io.intino.master.data.validation.RecordValidator;
import io.intino.master.data.validation.TripleRecordStore;
import io.intino.master.data.validation.TripleSource;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DuplicatedTripleRecordValidator implements RecordValidator {

	@Override
	public Stream<Issue> validate(TripleRecord record, TripleRecordStore store) {
		return record.attributes().entrySet()
				.stream()
				.filter(e -> e.getValue().size() > 1)
				.map(e -> getIssue(record, e));
	}

	private Issue getIssue(TripleRecord record, Map.Entry<String, List<TripleRecord.Value>> e) {
		TripleSource source0 = e.getValue().get(0).source();

		if(e.getValue().stream().allMatch(v -> Objects.equals(source0, v.source())))
			return Issue.error("Record (" + record.id() + ") defines " + e.getKey() + " " + e.getValue().size() + " times.")
				.source(e.getValue().get(e.getValue().size() - 1).source());

		return Issue.error("Record (" + record.id() + ") defines " + e.getKey() + " " + e.getValue().size() + " times in different files.")
				.source(new CombinedTripleSource(e.getValue().stream().map(TripleRecord.Value::source).map(TripleSource::get).collect(Collectors.toList())));
	}

	public static class CombinedTripleSource implements TripleSource {

		private final List<String> names;

		public CombinedTripleSource(List<String> names) {
			this.names = names;
		}

		public List<String> names() {
			return names;
		}

		@Override
		public String name() {
			return "Duplicated fields across different sources";
		}

		@Override
		public String get() {
			return "Sources:\n\t\t" + String.join("\n\t\t", names);
		}
	}
}
