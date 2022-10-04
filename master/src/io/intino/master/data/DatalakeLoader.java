package io.intino.master.data;

import java.io.File;
import java.util.Map;

public interface DatalakeLoader {

	Map<String, Map<String, String>> load(File rootDirectory);
}
