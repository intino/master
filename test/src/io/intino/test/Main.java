package io.intino.test;

import com.hazelcast.client.config.ClientConfig;
import io.intino.test.model.RemoteMaster;
import io.intino.test.model.entities.Area;

import java.util.List;

public class Main {


	public static void main(String[] args) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress("localhost:5701");
		RemoteMaster master = new RemoteMaster(cfg);
		List<Area> areas = master.areas();

	}
}
