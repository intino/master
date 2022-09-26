package io.intino.master.data.validation.report;

import io.intino.master.data.validation.Issue;

import java.io.File;
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
		new HtmlIssueReportDocumentBuilder(this).build(file);
	}
}
