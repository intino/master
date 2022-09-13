package com.cinepolis.master;

import com.cinepolis.master.model.Master;
import com.cinepolis.master.model.entities.*;
import com.google.gson.Gson;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.intino.master.model.Entity;
import io.intino.master.model.Triple;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

public class MasterClientRemoteMaps implements Master {

	public static void main(String[] args) {
		connect("localhost:62555");
	}

	public static MasterClientRemoteMaps connect(String url) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress(url);
		return new MasterClientRemoteMaps(cfg);
	}

	private final IMap<String, String> master;
	private static BiConsumer<String, Triple> publisher;
	private final HazelcastInstance hz;
	private final Gson gson = new Gson();

	public MasterClientRemoteMaps() {
		this(new ClientConfig());
	}

	public MasterClientRemoteMaps(ClientConfig config) {
		configureLogger();
		this.hz = HazelcastClient.newHazelcastClient(config);

		master = hz.getMap("master");

		publisher = (publisher, triple) -> hz.getTopic("requests").publish(publisher + "##" + triple.toString());

		publish("test", new Triple("2520001:theater", "name", "Cine 1"));
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

	private <T extends Entity> T entity(BiFunction<String, Master, T> constructor, String id, Map<String, String> record) {
		T entity = constructor.apply(id, this);
		record.entrySet().stream().map(e -> new Triple(id, e.getKey(), e.getValue())).forEach(entity::add);
		return entity;
	}

	public Employee employee(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Employee::new, id, record);
	}

	public List<Employee> employees() {
		return master.entrySet().stream().parallel()
				.filter(e -> e.getKey().endsWith(":employee"))
				.map(e -> {
					Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
					return entity(Employee::new, e.getKey(), attribs);
				}).collect(Collectors.toList());
	}

	public Place place(String id) {
//		Map<String, String> record = getRecord(id);
//		if(record == null) return null;
//		return entity(Place::new, id, record);
		return null; // TODO
	}

	public List<Place> places() {
//		return master.entrySet().stream().parallel().map(e -> {
//			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
//			return entity(Place::new, e.getKey(), attribs);
//		}).collect(Collectors.toList());
		return new ArrayList<>(); // TODO
	}

	public Country country(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Country::new, id, record);
	}

	public List<Country> countries() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":country")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Country::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Area area(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Area::new, id, record);
	}

	public List<Area> areas() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":area")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Area::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Region region(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Region::new, id, record);
	}

	public List<Region> regions() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":region")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Region::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Theater theater(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Theater::new, id, record);
	}

	public List<Theater> theaters() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":theater")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Theater::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Screen screen(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Screen::new, id, record);
	}

	public List<Screen> screens() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":screen")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Screen::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Dock dock(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Dock::new, id, record);
	}

	public List<Dock> docks() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":dock")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Dock::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public ScreenDock screenDock(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(ScreenDock::new, id, record);
	}

	public List<ScreenDock> screenDocks() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":screenDock")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(ScreenDock::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Depot depot(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Depot::new, id, record);
	}

	public List<Depot> depots() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":depot")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Depot::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Office office(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Office::new, id, record);
	}

	public List<Office> offices() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":office")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Office::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Desk desk(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Desk::new, id, record);
	}

	public List<Desk> desks() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":desk")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Desk::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Asset asset(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Asset::new, id, record);
	}

	public List<Asset> assets() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":asset")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(Asset::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public DualAsset dualAsset(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(DualAsset::new, id, record);
	}

	public List<DualAsset> dualAssets() {
		return master.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":dualAsset")).map(e -> {
			Map<String, String> attribs = gson.fromJson(e.getValue(), Map.class);
			return entity(DualAsset::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	private Map<String, String> getRecord(String id) {
		String recordJson = master.get(id);
		if(recordJson == null) return null;
		return gson.fromJson(recordJson, Map.class);
	}

	public void stop() {
		hz.shutdown();
	}

	public void publish(String publisherName, Triple triple) {
		if (publisher != null) publisher.accept(publisherName, triple);
	}
}