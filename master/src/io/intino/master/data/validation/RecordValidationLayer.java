package io.intino.master.data.validation;

import io.intino.master.data.validation.RecordValidator.TripleRecord;

import java.util.*;
import java.util.stream.Stream;

public class RecordValidationLayer {

	private final List<RecordValidator> generalValidators = new ArrayList<>();
	private final Map<String, RecordValidator> validatorsPerType = new HashMap<>();

	public Stream<Issue> validate(TripleRecordStore store) {
		return store.stream().map(record -> validate(record, store)).reduce(Stream::concat).orElse(Stream.empty());
	}

	public Stream<Issue> validate(TripleRecord record, TripleRecordStore store) {
		return Stream.concat(
				generalValidators.stream().flatMap(v -> v.validate(record, store)).filter(Objects::nonNull),
				validatorsPerType.getOrDefault(record.type(), RecordValidator.none())
						.validate(record, store)
						.filter(Objects::nonNull)
						.peek(issue -> issue.scope(ValidationLayers.Scope.RECORDS))
		);
	}

	public RecordValidationLayer addValidator(RecordValidator validator) {
		if(validator == null) return this;
		generalValidators.add(validator);
		return this;
	}

	public RecordValidationLayer setValidator(String type, RecordValidator validator) {
		if(validator == null) return this;
		validatorsPerType.put(type, validator);
		return this;
	}
}
