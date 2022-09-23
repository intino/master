package io.intino.master.data.validation;

import io.intino.alexandria.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class IssueReport {

	public static final String OTHER = "Other";

	private final Map<String, List<Issue>> issues;

	public IssueReport() {
		this.issues = new HashMap<>();
	}

	public IssueReport put(Stream<Issue> issues) {
		if (issues == null) return this;
		issues.forEach(this::put);
		return this;
	}

	private void put(Issue issue) {
		if (issue == null) return;
		String key = issue.source() == null ? OTHER : issue.source().name();
		if(key == null) key = OTHER;
		List<Issue> issuesOfThatFile = issues.computeIfAbsent(key, k -> new ArrayList<>());
		issuesOfThatFile.add(issue);
	}

	public Map<String, List<Issue>> getAll() {
		return Collections.unmodifiableMap(issues);
	}

	public int errorCount() {
		return (int) filter(Issue.Level.Error).count();
	}

	public int warningCount() {
		return (int) filter(Issue.Level.Warning).count();
	}

	public List<Issue> errors() {
		return filter(Issue.Level.Error).collect(toList());
	}

	public List<Issue> warnings() {
		return filter(Issue.Level.Warning).collect(toList());
	}

	public int count() {
		return issues.values().stream().mapToInt(List::size).sum();
	}

	private Stream<Issue> filter(Issue.Level level) {
		return issues.values().stream().flatMap(List::stream).filter(i -> i.level().equals(level));
	}

	public void save(File file) {

		file.getParentFile().mkdirs();

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

			println(writer, "MASTER ISSUES REPORT\n");
			println(writer, "Date: " + LocalDateTime.now());
			println(writer, "Errors: " + errorCount());
			println(writer, "Warnings: " + warningCount());
			println(writer, "Files with problems: " + getAll().size());

			for (Map.Entry<String, List<Issue>> entry : sortByNumErrors(getAll().entrySet())) {

				String path = entry.getKey();
				List<Issue> issues = entry.getValue();
				if (issues.isEmpty()) continue;

				issues.sort(Comparator.naturalOrder());

				writer.newLine();
				writer.newLine();
				println(writer, path.equals(OTHER)
						? "==== General issues (" + issues.size() + ") ====\n"
						: "==== '" + path + "' (Issues: " + issues.size() + ") ====\n");

				printAll(writer, issues.stream().filter(i -> i.level() == Issue.Level.Error));
				printAll(writer, issues.stream().filter(i -> i.level() == Issue.Level.Warning));
			}

		} catch (IOException e) {
			Logger.error(e);
		}
	}

	private Iterable<? extends Map.Entry<String, List<Issue>>> sortByNumErrors(Set<Map.Entry<String, List<Issue>>> entrySet) {
		return entrySet.stream().sorted((e1, e2) -> -Integer.compare(numErrors(e1.getValue()), numErrors(e2.getValue()))).collect(toList());
	}

	private int numErrors(List<Issue> issueList) {
		return (int) issueList.stream().filter(issue -> issue.level().equals(Issue.Level.Error)).count();
	}

	private void printAll(BufferedWriter writer, Stream<Issue> issues) {
		issues.forEach(issue -> println(writer, issue.toString() + "\n"));
	}

	private void println(BufferedWriter writer, String msg) {
		try {
			writer.write(msg);
			writer.newLine();
		} catch (Exception ignored) {
		}
	}
}
