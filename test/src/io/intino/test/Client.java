package io.intino.test;

import com.hazelcast.client.config.ClientConfig;
import io.intino.test.model.MasterClient;
import io.intino.test.model.entities.Area;

import java.util.List;

public class Client {


	public static void main(String[] args) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress("localhost:5701");
		MasterClient master = new MasterClient(cfg);
		List<Area> areas = master.areas();
	}
}
