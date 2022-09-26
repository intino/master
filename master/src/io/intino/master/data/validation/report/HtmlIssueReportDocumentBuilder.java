package io.intino.master.data.validation.report;

import io.intino.master.data.validation.Issue;
import io.intino.master.data.validation.TripleSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class HtmlIssueReportDocumentBuilder {

	private static final String BLANK_LINE = "<p>&nbsp;</p>";

	private final IssueReport issueReport;

	public HtmlIssueReportDocumentBuilder(IssueReport issueReport) {
		this.issueReport = issueReport;
	}

	public void build(File file) {
		file.getParentFile().mkdirs();

		HtmlBuilder builder = new HtmlBuilder();
		builder.addHtmlTag(false);

		HtmlTemplate template = HtmlTemplate.get("issues-report.html");
		template.set("count", String.valueOf(issueReport.count()));
		template.set("error-count", String.valueOf(issueReport.errorCount()));
		template.set("warnings-count", String.valueOf(issueReport.warningCount()));

		template.set("sources-count", String.valueOf(issueReport.getAll().size()));

		template.set("sources", renderSources());
		template.set("content", renderContent());

		builder.append(template.html());

		try {
			Files.writeString(file.toPath(), builder.build());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String renderContent() {
		StringBuilder sb = new StringBuilder();
		for(var entry : sortByNumErrors(new HashSet<>(issueReport.getAll().entrySet()))) {
			sb.append("<h4>").append(entry.getKey()).append(" (").append(entry.getValue().size()).append("):</h4>");
			sb.append("<ul class=\"list-group\">");
			entry.getValue().stream().filter(e -> e.level() == Issue.Level.Error).sorted(sortByLine()).map(this::render).forEach(sb::append);
			entry.getValue().stream().filter(e -> e.level() == Issue.Level.Warning).sorted(sortByLine()).map(this::render).forEach(sb::append);
			sb.append("</ul>").append(BLANK_LINE);
		}
		return sb.toString();
	}

	private Comparator<? super Issue> sortByLine() {
		return Comparator.comparing(issue -> !(issue.source() instanceof TripleSource.FileTripleSource) ? Integer.MAX_VALUE : ((TripleSource.FileTripleSource) issue.source()).line());
	}

	private String render(Issue issue) {
		String level = issue.level() == Issue.Level.Error ? "danger" : "warning";
		Integer line = !(issue.source() instanceof TripleSource.FileTripleSource) ? null : ((TripleSource.FileTripleSource) issue.source()).line();
		return "<div class=\"list-group-item list-group-item-" + level + " mb-1\">"
				+ "<div><i class=\"fa-solid fa-skating fa-fw\" style=\"background:DodgerBlue\"></i>" + issue.levelMsg() + "</div>"
				+ (line == null ? "" : ("<small>At line " + line + "</small>"))
				+ "</div>";
	}

	private String renderSources() {
		StringBuilder sb = new StringBuilder();
		for(var entry : sortByNumErrors(new HashSet<>(issueReport.getAll().entrySet()))) {
			int errors = numErrors(entry.getValue());
			int warnings = entry.getValue().size() - errors;
			sb.append(listItemBadge(entry.getKey(), warnings, errors));
		}
		return sb.toString();
	}

	private Iterable<? extends Map.Entry<String, List<Issue>>> sortByNumErrors(Set<Map.Entry<String, List<Issue>>> entrySet) {
		return entrySet.stream().sorted((e1, e2) -> -Integer.compare(numErrors(e1.getValue()), numErrors(e2.getValue()))).collect(toList());
	}

	private int numErrors(List<Issue> issueList) {
		return (int) issueList.stream().filter(issue -> issue.level().equals(Issue.Level.Error)).count();
	}

	private String listItemBadge(String text, int warnings, int errors) {
		return "<li class=\"list-group-item d-flex\">"
				+ "<div>" + text + "</div>\n"
				+ "<div class=\"d-flex align-items-right ml-auto align-items-center ml-auto\">"
				+ "<span class=\"badge badge-danger badge-pill mr-1\">" + errors + "</span>"
				+ "<span class=\"badge badge-warning badge-pill mr-1\">" + warnings + "</span>"
				+ "<span class=\"badge badge-primary badge-pill mr-1\">" + (warnings + errors) + "</span>"
				+ "</div>\n"
				+ "<span class=\"border-bottom-1\"></span>"
				+ "</li>";
	}

	public static class HtmlBuilder {

		private final StringBuilder html;
		private boolean addHtmlTag = true;

		public HtmlBuilder() {
			this.html = new StringBuilder(8192);
		}

		public HtmlBuilder addHtmlTag(boolean addHtmlTag) {
			this.addHtmlTag = addHtmlTag;
			return this;
		}

		public HtmlBuilder append(HtmlTemplate template) {
			this.html.append(template.html());
			return this;
		}

		public HtmlBuilder append(String str) {
			this.html.append(str);
			return this;
		}

		public String build() {
			return addHtmlTag ? "<html>" + html + "</html>" : html.toString();
		}

		@Override
		public String toString() {
			return build();
		}
	}

	public static class HtmlTemplate {

		public static HtmlTemplate get(String name) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(HtmlTemplate.class.getResourceAsStream("/" + name)))) {
				return new HtmlTemplate(reader.lines().collect(Collectors.joining("\n")));
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to open " + name, e);
			}
		}

		private final StringBuilder html;

		public HtmlTemplate(String html) {
			this.html = new StringBuilder(html);
		}

		public HtmlTemplate set(String variable, String value) {
			variable = String.format("'$%s'", variable);
			int index;
			while((index = html.indexOf(variable)) >= 0) {
				html.replace(index, index + variable.length(), value);
			}
			return this;
		}

		public String html() {
			return html.toString();
		}

		@Override
		public String toString() {
			return html();
		}
	}
}
