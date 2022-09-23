package io.intino.master.data;

import io.intino.master.data.validation.IssueReport;
import io.intino.master.data.validation.RecordValidator.TripleRecord;
import io.intino.master.data.validation.RecordValidator.TripleRecord.Value;
import io.intino.master.data.validation.TripleRecordStore;
import io.intino.master.data.validation.TripleSource;
import io.intino.master.data.validation.ValidationLayers;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class DataLoader {

	public static final String TRIPLES_EXTENSION = ".triples";

	public static MasterLoadResult load(File rootDir, ValidationLayers validationLayers, MasterSerializer serializer) {
		return new DataLoader().loadData(rootDir, validationLayers, serializer);
	}

	private final MasterLoadResult result = new MasterLoadResult();
	private ValidationLayers validationLayers;

	private DataLoader() {}

	public MasterLoadResult loadData(File rootDir, ValidationLayers validationLayers, MasterSerializer serializer) {
		this.validationLayers = requireNonNull(validationLayers);
		result.data = normalize(validateRecords(loadRecordsFromDisk(rootDir)), serializer);
		return result;
	}

	private Map<String, String> normalize(Map<String, TripleRecord> records, MasterSerializer serializer) {
		if(records == null) return null;
		return records.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> serializer.serialize(getAttributes(e.getValue()))));
	}

	private Map<String, String> getAttributes(TripleRecord value) {
		return value.attributes().entrySet().stream()
				.filter(e -> !e.getValue().isEmpty())
				.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().get(0)))
				.filter(e -> e.getValue() != null && !e.getValue().isEmpty())
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get()));
	}

	private Map<String, TripleRecord> validateRecords(Map<String, TripleRecord> records) {
		result.issues.put(validationLayers.recordValidationLayer().validate(store(records)));
		if(result.issues.errorCount() > 0) return null;
		return records;
	}

	private TripleRecordStore store(Map<String, TripleRecord> records) {
		return new TripleRecordStore() {
			@Override public TripleRecord get(String id) {return records.get(id);}
			@Override public Stream<TripleRecord> stream() {return records.values().stream();}
		};
	}

	private Map<String, TripleRecord> loadRecordsFromDisk(File rootDir) {
		Map<String, TripleRecord> records = new HashMap<>();
		try(Stream<Path> files = Files.walk(rootDir.toPath())) {
			files.map(Path::toFile)
					.filter(f -> f.isFile() && f.getName().endsWith(TRIPLES_EXTENSION))
					.flatMap(this::readTriplesFromFile)
					.forEach(e -> {
						Triple triple = e.getKey();
						TripleSource source = e.getValue();
						records.computeIfAbsent(triple.subject(), k -> new TripleRecord(k).source(source)).add(triple.predicate(), new Value(triple.value()).source(source));
					});
			return records;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Stream<Map.Entry<Triple, TripleSource>> readTriplesFromFile(File file) {
		result.filesRead.add(file);

		String path = file.getAbsolutePath();
		List<Map.Entry<Triple, TripleSource>> triples = new ArrayList<>();

		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			for(int index = 0;(line = reader.readLine()) != null;index++) {
				process(path, triples, line, index);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return triples.stream();
	}

	private void process(String path, List<Map.Entry<Triple, TripleSource>> triples, String line, int index) {
		++result.linesRead;

		if(line.isEmpty()) return;
		TripleSource source = TripleSource.ofFile(path, index);

		result.issues.put(validationLayers.tripleValidationLayer().validate(line, source));

		triples.add(new AbstractMap.SimpleEntry<>(new Triple(line), source));

		++result.triplesRead;
	}

	public static class MasterLoadResult {

		private final IssueReport issues = new IssueReport();
		private final List<File> filesRead = new ArrayList<>();
		private long linesRead;
		private long triplesRead;
		private int numRecords;
		private Map<String, String> data;

		private MasterLoadResult() {}

		public IssueReport issues() {
			return issues;
		}

		public List<File> filesRead() {
			return filesRead;
		}

		public long linesRead() {
			return linesRead;
		}

		public long triplesRead() {
			return triplesRead;
		}

		public int numRecords() {
			return numRecords;
		}

		public Map<String, String> data() {
			return data;
		}
	}
}
