package com.cinepolis.master;

import com.cinepolis.master.model.Master;
import com.cinepolis.master.model.entities.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.intino.master.model.Entity;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializer;
import io.intino.master.serialization.MasterSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

import static io.intino.master.core.Master.*;
import static java.util.Objects.requireNonNull;

public class LazyMasterClient implements Master {

	public static void main(String[] args) {
		connect("localhost:62555");
	}

	public static LazyMasterClient connect(String url) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress(url);
		LazyMasterClient client = new LazyMasterClient(cfg);
		client.start();
		return client;
	}

	private final ClientConfig config;
	private HazelcastInstance hazelcast;
	private IMap<String, String> masterMap;
	private MasterSerializer serializer;

	public LazyMasterClient() {
		this(new ClientConfig());
	}

	public LazyMasterClient(ClientConfig config) {
		this.config = requireNonNull(config);
	}

	public void start() {
		configureLogger();
		initHazelcastClient();
	}

	private void initHazelcastClient() {
		hazelcast = HazelcastClient.newHazelcastClient(config);
		masterMap = hazelcast.getMap(MASTER_MAP_NAME);
		IMap<String, String> metadata = hazelcast.getMap(METADATA_MAP_NAME);
		serializer = MasterSerializers.get(metadata.get("serializer"));
	}

	public void stop() {
		hazelcast.shutdown();
	}

	public void publish(String publisherName, Triple triple) {
		if(publisherName == null) throw new NullPointerException("Publisher name cannot be null");
		if(triple == null) throw new NullPointerException("Triple cannot be null");
		hazelcast.getTopic(REQUESTS_TOPIC).publish(publisherName + MESSAGE_SEPARATOR + triple);
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
		return masterMap.entrySet().stream().parallel()
				.filter(e -> e.getKey().endsWith(":employee"))
				.map(e -> {
					Map<String, String> attribs = serializer.deserialize(e.getValue());
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
//		returnMap master.entrySet().stream().parallel().map(e -> {
//			Map<String, String> attribs = serializer.deserialize(e.getValue());
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
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":country")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Country::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Area area(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Area::new, id, record);
	}

	public List<Area> areas() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":area")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Area::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Region region(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Region::new, id, record);
	}

	public List<Region> regions() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":region")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Region::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Theater theater(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Theater::new, id, record);
	}

	public List<Theater> theaters() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":theater")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Theater::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Screen screen(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Screen::new, id, record);
	}

	public List<Screen> screens() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":screen")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Screen::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Dock dock(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Dock::new, id, record);
	}

	public List<Dock> docks() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":dock")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Dock::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public ScreenDock screenDock(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(ScreenDock::new, id, record);
	}

	public List<ScreenDock> screenDocks() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":screenDock")).map(e -> {
			return entity(ScreenDock::new, e.getKey(), serializer.deserialize(e.getValue()));
		}).collect(Collectors.toList());
	}

	public Depot depot(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Depot::new, id, record);
	}

	public List<Depot> depots() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":depot")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Depot::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Office office(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Office::new, id, record);
	}

	public List<Office> offices() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":office")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Office::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Desk desk(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Desk::new, id, record);
	}

	public List<Desk> desks() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":desk")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Desk::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public Asset asset(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(Asset::new, id, record);
	}

	public List<Asset> assets() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":asset")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(Asset::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	public DualAsset dualAsset(String id) {
		Map<String, String> record = getRecord(id);
		if(record == null) return null;
		return entity(DualAsset::new, id, record);
	}

	public List<DualAsset> dualAssets() {
		return masterMap.entrySet().stream().parallel().filter(e -> e.getKey().endsWith(":dualAsset")).map(e -> {
			Map<String, String> attribs = serializer.deserialize(e.getValue());
			return entity(DualAsset::new, e.getKey(), attribs);
		}).collect(Collectors.toList());
	}

	private Map<String, String> getRecord(String id) {
		String serializedRecord = masterMap.get(id);
		if(serializedRecord == null) return null;
		return serializer.deserialize(serializedRecord);
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
}