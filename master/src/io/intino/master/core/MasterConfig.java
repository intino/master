package io.intino.master.core;

import io.intino.master.serialization.MasterSerializer;
import io.intino.master.serialization.MasterSerializers;

import java.io.File;
import java.util.Map;

public class MasterConfig {

	private final File dataDirectory;
	private String instanceName = "master";
	private int port = 5701;
	private String host = "localhost";
	private MasterSerializer serializer = MasterSerializers.getDefault();

	public MasterConfig(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public MasterConfig(Map<String, String> arguments) {
		this.dataDirectory = new File(arguments.get("data_directory"));
		this.instanceName = arguments.getOrDefault("instance_name", instanceName);
		this.port = Integer.parseInt(arguments.getOrDefault("port", String.valueOf(port)));
		this.serializer = MasterSerializers.get(arguments.getOrDefault("serializer", MasterSerializers.Standard.getDefault()));
		this.host = arguments.getOrDefault("host", host);
	}

	public File dataDirectory() {
		return dataDirectory;
	}

	public String instanceName() {
		return instanceName;
	}

	public MasterConfig instanceName(String instanceName) {
		this.instanceName = instanceName;
		return this;
	}

	public int port() {
		return port;
	}

	public MasterConfig port(int port) {
		this.port = port;
		return this;
	}

	public String host() {
		return host;
	}

	public MasterConfig host(String host) {
		this.host = host;
		return this;
	}

	public MasterSerializer serializer() {
		return serializer;
	}

	public MasterConfig serializer(MasterSerializer serializer) {
		this.serializer = serializer;
		return this;
	}
}
