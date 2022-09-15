import com.cinepolis.master.CachedMasterClient;
import com.cinepolis.master.model.Master;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import io.intino.master.model.Entity;

import java.util.Random;
import java.util.function.Supplier;

public class Client {

	private static final int ITERATIONS = 8;
	private static long blackhole = new Random().nextLong();

	public static void main(String[] args) {
//		MasterClient client = MasterClient.connect("localhost:5701");
//		List<Screen> screens = client.screens();
//		client.publish("test", new Triple("1020126.14:screen", "type", "LED"));

//		test("Triples + factors + local cache", () -> new MasterClientTriplesFactorsLocalMaps(config()));
		test("Records (json) + local cache", () -> new CachedMasterClient(config()));
//		test("Records (json)", () -> new LazyMasterClient(config()));
//		test("Records (json)", () -> new MasterClientScatteredRemoteMaps(config()));

		System.out.println(blackhole);
	}

	private static void test(String name, Supplier<Master> masterImpl) {
		warmup(masterImpl);

		long loadTime = 0;
		long jvmMem = 0;
		long queryTime = 0;

		for(int i = 0;i < ITERATIONS;i++) {
			gc();
			long start = System.currentTimeMillis();

			Master master = masterImpl.get();
			master.start();
			blackhole += master.hashCode();

			loadTime += System.currentTimeMillis() - start;
			jvmMem += getJvmMemoryUsed();

			String[] theaterIds = master.theaters().stream().map(Entity::id).toArray(String[]::new);
			String[] employeeIds = master.employees().stream().map(Entity::id).toArray(String[]::new);

			start = System.currentTimeMillis();

			for(int j = 0;j < ITERATIONS;j++) {
				for(String id : theaterIds) blackhole += master.theater(id).name().hashCode();
				for(String id : employeeIds) blackhole += master.employee(id).name().hashCode();
			}

			queryTime += System.currentTimeMillis() - start;

			master.stop();
		}

		float timeResult = loadTime / (float) ITERATIONS;
		float queryTimeResult = queryTime / (float) ITERATIONS;
		float jvmMemResult = jvmMem / (float) ITERATIONS / 1024.0f / 1024.0f;

		System.out.println("name\tload-time-ms\tquery-time-ms\tjvm-mem-mb");
		System.out.println(name + "\t" + String.format("%04f", timeResult) + "\t" + String.format("%04f", queryTimeResult) + "\t" + String.format("%04f", jvmMemResult));
	}

	private static void warmup(Supplier<Master> masterImpl) {
		gc();
		blackhole += masterImpl.get().hashCode();
	}

	private static long getJvmMemoryUsed() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	private static void gc() {
		System.gc();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static ClientConfig config() {
		return new ClientConfig().setNetworkConfig(new ClientNetworkConfig().addAddress("localhost:62555"));
	}

	private static class BenchmarkResult {
		public String name;
		public float loadTimeMs;
		public float queryTimeMs;
		public float jvmMemMb;
	}
}
