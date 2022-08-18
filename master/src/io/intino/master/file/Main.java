package io.intino.master.file;


import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static java.util.stream.Collectors.toMap;

public class Main {

	public static void main(String[] args) {
		configureLogger();
		new FileMaster(new File(asMap(args).get("triples_folder"))).start();
	}

	private static void configureLogger() {
		java.util.logging.Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.WARNING);
		for (Handler h : rootLogger.getHandlers()) rootLogger.removeHandler(h);
		final ConsoleHandler handler = new ConsoleHandler();
		handler.setLevel(Level.WARNING);
		handler.setFormatter(new io.intino.alexandria.logger.Formatter());
		rootLogger.setUseParentHandlers(false);
		rootLogger.addHandler(handler);
	}

	private static Map<String, String> asMap(String[] args) {
		return Arrays.stream(args)
				.map(a -> a.split("="))
				.collect(toMap(s -> s[0].trim(), s -> s[1].trim()));
	}
}