package com.cinepolis.master;

import com.cinepolis.master.model.Master;
import com.cinepolis.master.model.TripleRecord;
import com.cinepolis.master.model.entities.*;
import com.google.gson.Gson;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class MasterClientLocalMaps implements Master {

	public static void main(String[] args) {
		connect("localhost:62555");
	}

	public static MasterClientLocalMaps connect(String url) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress(url);
		return new MasterClientLocalMaps(cfg);
	}

	private static BiConsumer<String, Triple> publisher;

	private final Map<String, Consumer<String>> removers = new HashMap<>() {{
		put("employee", MasterClientLocalMaps.this::removeFromEmployee);
		put("country", MasterClientLocalMaps.this::removeFromCountry);
		put("area", MasterClientLocalMaps.this::removeFromArea);
		put("region", MasterClientLocalMaps.this::removeFromRegion);
		put("theater", MasterClientLocalMaps.this::removeFromTheater);
		put("screen", MasterClientLocalMaps.this::removeFromScreen);
		put("dock", MasterClientLocalMaps.this::removeFromDock);
		put("screendock", MasterClientLocalMaps.this::removeFromScreenDock);
		put("depot", MasterClientLocalMaps.this::removeFromDepot);
		put("office", MasterClientLocalMaps.this::removeFromOffice);
		put("desk", MasterClientLocalMaps.this::removeFromDesk);
		put("asset", MasterClientLocalMaps.this::removeFromAsset);
		put("dualasset", MasterClientLocalMaps.this::removeFromDualAsset);
	}};

	private final Map<String, Consumer<TripleRecord>> adders = new HashMap<>() {{
		put("employee", MasterClientLocalMaps.this::addToEmployee);
		put("country", MasterClientLocalMaps.this::addToCountry);
		put("area", MasterClientLocalMaps.this::addToArea);
		put("region", MasterClientLocalMaps.this::addToRegion);
		put("theater", MasterClientLocalMaps.this::addToTheater);
		put("screen", MasterClientLocalMaps.this::addToScreen);
		put("dock", MasterClientLocalMaps.this::addToDock);
		put("screendock", MasterClientLocalMaps.this::addToScreenDock);
		put("depot", MasterClientLocalMaps.this::addToDepot);
		put("office", MasterClientLocalMaps.this::addToOffice);
		put("desk", MasterClientLocalMaps.this::addToDesk);
		put("asset", MasterClientLocalMaps.this::addToAsset);
		put("dualasset", MasterClientLocalMaps.this::addToDualAsset);
	}};

	private final Map<String, Employee> employeeMap = new ConcurrentHashMap<>();
	private final Map<String, Place> placeMap = new ConcurrentHashMap<>();
	private final Map<String, Country> countryMap = new ConcurrentHashMap<>();
	private final Map<String, Area> areaMap = new ConcurrentHashMap<>();
	private final Map<String, Region> regionMap = new ConcurrentHashMap<>();
	private final Map<String, Theater> theaterMap = new ConcurrentHashMap<>();
	private final Map<String, Screen> screenMap = new ConcurrentHashMap<>();
	private final Map<String, Dock> dockMap = new ConcurrentHashMap<>();
	private final Map<String, ScreenDock> screenDockMap = new ConcurrentHashMap<>();
	private final Map<String, Depot> depotMap = new ConcurrentHashMap<>();
	private final Map<String, Office> officeMap = new ConcurrentHashMap<>();
	private final Map<String, Desk> deskMap = new ConcurrentHashMap<>();
	private final Map<String, Asset> assetMap = new ConcurrentHashMap<>();
	private final Map<String, DualAsset> dualAssetMap = new ConcurrentHashMap<>();
	private final HazelcastInstance hz;

	public MasterClientLocalMaps() {
		this(new ClientConfig());
	}

	public MasterClientLocalMaps(ClientConfig config) {
		configureLogger();
		this.hz = HazelcastClient.newHazelcastClient(config);

		IMap<String, String> master = hz.getMap("master");

		Gson gson = new Gson();

		Logger.info("Loading data from master...");

		master.forEach((id, recordJson) -> {
			Map<String, String> attributes = gson.fromJson(recordJson, Map.class);
			add(new TripleRecord(id, attributes));
		});

		Logger.info("Data from master loaded: " + this);

		master.addEntryListener(new TripleEntryDispatcher(), true);

		publisher = (publisher, triple) -> hz.getTopic("requests").publish(publisher + "##" + triple.toString());

		publish("test", new Triple("2520001:theater", "name", "Cine 1"));
	}

	private void add(TripleRecord record) {
		if(!adders.containsKey(record.type())) {
//			Logger.error("No adder defined for type " + record.type());
			return;
		}
		adders.get(record.type()).accept(record);
	}

	private void remove(String id) {
		if(!removers.containsKey(typeOf(id))) {
//			Logger.error("No remover defined for type " + typeOf(id));
			return;
		}
		removers.get(typeOf(id)).accept(id);
	}

	private String typeOf(String id) {
		return id.contains(":") ? id.substring(id.indexOf(':') + 1) : "unknown";
	}

	public Employee employee(String id) {
		return employeeMap.get(id);
	}

	public List<Employee> employees() {
		return new ArrayList<>(employeeMap.values());
	}

	public Place place(String id) {
		return placeMap.get(id);
	}

	public List<Place> places() {
		return new ArrayList<>(placeMap.values());
	}

	public Country country(String id) {
		return countryMap.get(id);
	}

	public List<Country> countries() {
		return new ArrayList<>(countryMap.values());
	}

	public Area area(String id) {
		return areaMap.get(id);
	}

	public List<Area> areas() {
		return new ArrayList<>(areaMap.values());
	}

	public Region region(String id) {
		return regionMap.get(id);
	}

	public List<Region> regions() {
		return new ArrayList<>(regionMap.values());
	}

	public Theater theater(String id) {
		return theaterMap.get(id);
	}

	public List<Theater> theaters() {
		return new ArrayList<>(theaterMap.values());
	}

	public Screen screen(String id) {
		return screenMap.get(id);
	}

	public List<Screen> screens() {
		return new ArrayList<>(screenMap.values());
	}

	public Dock dock(String id) {
		return dockMap.get(id);
	}

	public List<Dock> docks() {
		return new ArrayList<>(dockMap.values());
	}

	public ScreenDock screenDock(String id) {
		return screenDockMap.get(id);
	}

	public List<ScreenDock> screenDocks() {
		return new ArrayList<>(screenDockMap.values());
	}

	public Depot depot(String id) {
		return depotMap.get(id);
	}

	public List<Depot> depots() {
		return new ArrayList<>(depotMap.values());
	}

	public Office office(String id) {
		return officeMap.get(id);
	}

	public List<Office> offices() {
		return new ArrayList<>(officeMap.values());
	}

	public Desk desk(String id) {
		return deskMap.get(id);
	}

	public List<Desk> desks() {
		return new ArrayList<>(deskMap.values());
	}

	public Asset asset(String id) {
		return assetMap.get(id);
	}

	public List<Asset> assets() {
		return new ArrayList<>(assetMap.values());
	}

	public DualAsset dualAsset(String id) {
		return dualAssetMap.get(id);
	}

	public List<DualAsset> dualAssets() {
		return new ArrayList<>(dualAssetMap.values());
	}

	public void stop() {
		hz.shutdown();
	}

	public void publish(String publisherName, Triple triple) {
		if (publisher != null) publisher.accept(publisherName, triple);
	}

	private void addToEmployee(TripleRecord record) {
		Employee employee = new Employee(record.id(), this);
		record.triples().forEach(employee::add);
		employeeMap.put(record.id(), employee);
	}

	private void addToCountry(TripleRecord record) {
		Country country = new Country(record.id(), this);
		record.triples().forEach(country::add);
		countryMap.put(record.id(), country);
	}

	private void addToArea(TripleRecord record) {
		Area area = new Area(record.id(), this);
		record.triples().forEach(area::add);
		areaMap.put(record.id(), area);
	}

	private void addToRegion(TripleRecord record) {
		Region region = new Region(record.id(), this);
		record.triples().forEach(region::add);
		regionMap.put(record.id(), region);
	}

	private void addToTheater(TripleRecord record) {
		Theater theater = new Theater(record.id(), this);
		record.triples().forEach(theater::add);
		theaterMap.put(record.id(), theater);
	}

	private void addToScreen(TripleRecord record) {
		Screen screen = new Screen(record.id(), this);
		record.triples().forEach(screen::add);
		screenMap.put(record.id(), screen);
	}

	private void addToDock(TripleRecord record) {
		Dock dock = new Dock(record.id(), this);
		record.triples().forEach(dock::add);
		dockMap.put(record.id(), dock);
	}

	private void addToScreenDock(TripleRecord record) {
		ScreenDock screenDock = new ScreenDock(record.id(), this);
		record.triples().forEach(screenDock::add);
		screenDockMap.put(record.id(), screenDock);
	}

	private void addToDepot(TripleRecord record) {
		Depot depot = new Depot(record.id(), this);
		record.triples().forEach(depot::add);
		depotMap.put(record.id(), depot);
	}

	private void addToOffice(TripleRecord record) {
		Office office = new Office(record.id(), this);
		record.triples().forEach(office::add);
		officeMap.put(record.id(), office);
	}

	private void addToDesk(TripleRecord record) {
		Desk desk = new Desk(record.id(), this);
		record.triples().forEach(desk::add);
		deskMap.put(record.id(), desk);
	}

	private void addToAsset(TripleRecord record) {
		Asset asset = new Asset(record.id(), this);
		record.triples().forEach(asset::add);
		assetMap.put(record.id(), asset);
	}

	private void addToDualAsset(TripleRecord record) {
		DualAsset dualAsset = new DualAsset(record.id(), this);
		record.triples().forEach(dualAsset::add);
		dualAssetMap.put(record.id(), dualAsset);
	}

	private void removeFromEmployee(String id) {
		employeeMap.remove(id);
	}

	private void removeFromCountry(String id) {
		countryMap.remove(id);
	}

	private void removeFromArea(String id) {
		areaMap.remove(id);
	}

	private void removeFromRegion(String id) {
		regionMap.remove(id);
	}

	private void removeFromTheater(String id) {
		theaterMap.remove(id);
	}

	private void removeFromScreen(String id) {
		screenMap.remove(id);
	}

	private void removeFromDock(String id) {
		dockMap.remove(id);
	}

	private void removeFromScreenDock(String id) {
		screenDockMap.remove(id);
	}

	private void removeFromDepot(String id) {
		depotMap.remove(id);
	}

	private void removeFromOffice(String id) {
		officeMap.remove(id);
	}

	private void removeFromDesk(String id) {
		deskMap.remove(id);
	}

	private void removeFromAsset(String id) {
		assetMap.remove(id);
	}

	private void removeFromDualAsset(String id) {
		dualAssetMap.remove(id);
	}

	private String typeOf(Triple triple) {
		String[] split = triple.subject().split(":");
		return split.length == 1 ? "unknown" : split[1].toLowerCase();
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

	@Override
	public String toString() {
		return "MasterClientLocalMaps{" +
				"employeeMap=" + employeeMap.size() +
				", placeMap=" + placeMap.size() +
				", countryMap=" + countryMap.size() +
				", areaMap=" + areaMap.size() +
				", regionMap=" + regionMap.size() +
				", theaterMap=" + theaterMap.size() +
				", screenMap=" + screenMap.size() +
				", dockMap=" + dockMap.size() +
				", screenDockMap=" + screenDockMap.size() +
				", depotMap=" + depotMap.size() +
				", officeMap=" + officeMap.size() +
				", deskMap=" + deskMap.size() +
				", assetMap=" + assetMap.size() +
				", dualAssetMap=" + dualAssetMap.size() +
				'}';
	}

	public class TripleEntryDispatcher extends EntryAdapter<String, String> {

		@Override
		public void entryAdded(EntryEvent<String, String> event) {
			updateRecord(event.getKey(), event.getValue());
//			add(triple(event));
		}

		@Override
		public void entryUpdated(EntryEvent<String, String> event) {
			updateRecord(event.getKey(), event.getValue());
//			add(triple(event));
		}

		@Override
		public void entryRemoved(EntryEvent<String, String> event) {
			remove(event.getKey());
		}

		@Override
		public void entryEvicted(EntryEvent<String, String> event) {
			remove(event.getKey());
		}

		private void updateRecord(String id, String recordJson) {
			Map<String, String> attributes = new Gson().fromJson(recordJson, Map.class);
			add(new TripleRecord(id, attributes));
		}
	}
}