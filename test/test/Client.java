import com.cinepolis.master.model.MasterClient;
import com.cinepolis.master.model.entities.Area;
import com.hazelcast.client.config.ClientConfig;

import java.util.List;

public class Client {


	public static void main(String[] args) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress("localhost:5701");
		MasterClient master = new MasterClient(cfg);
		List<Area> areas = master.areas();
	}
}
