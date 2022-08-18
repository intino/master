import com.cinepolis.master.model.MasterClient;
import com.cinepolis.master.model.entities.Screen;
import io.intino.master.model.Triple;

import java.util.List;

public class Client {

	public static void main(String[] args) {
		MasterClient client = MasterClient.connect("localhost:5701");
		List<Screen> screens = client.screens();
		client.publish("test", new Triple("1020126.14:screen", "type", "LED"));
	}
}
