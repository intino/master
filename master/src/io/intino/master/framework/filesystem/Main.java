package io.intino.master.framework.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class Main {

	public static void main(String[] args) {
		new FileMaster(new File(asMap(args).get("triples_folder"))).start();
	}

	private static Map<String, String> asMap(String[] args) {
		return Arrays.stream(args)
				.map(a -> a.split("="))
				.collect(toMap(s -> s[0].trim(), s -> s[1].trim()));
	}
}
