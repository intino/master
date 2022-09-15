package com.cinepolis.master;

import com.cinepolis.master.model.Master;
import com.cinepolis.master.model.TripleRecord;
import com.cinepolis.master.model.entities.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;
import io.intino.master.serialization.MasterSerializer;
import io.intino.master.serialization.MasterSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static io.intino.master.core.Master.*;
import static java.util.Objects.requireNonNull;

public class CachedMasterClient implements Master {

	public static void main(String[] args) {
		connect("localhost:62555");
	}

	public static CachedMasterClient connect(String url) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress(url);
		CachedMasterClient client = new CachedMasterClient(cfg);
		client.start();
		return client;
	}

	private final Map<String, Employee> employeeMap = new ConcurrentHashMap<>();
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

	private final ClientConfig config;
	private HazelcastInstance hazelcast;

	public CachedMasterClient() {
		this(new ClientConfig());
	}

	public CachedMasterClient(ClientConfig config) {
		this.config = requireNonNull(config);
	}

	public void start() {
		configureLogger();
		initHazelcastClient();
		loadData();
		initListeners();
	}

	private void initListeners() {
		hazelcast.getMap(MASTER_MAP_NAME).addEntryListener(new TripleEntryDispatcher(), true);
	}

	private void loadData() {
		IMap<String, String> master = hazelcast.getMap(MASTER_MAP_NAME);
		MasterSerializer serializer = serializer();

		Logger.info("Loading data from master (serializer=" + serializer.name() + ")");
		long start = System.currentTimeMillis();

		loadDataMultiThread(master, serializer);

		long time = System.currentTimeMillis() - start;
		Logger.info("Data from master loaded in " + time + " ms => " + this);
	}

	private void loadDataSingleThread(IMap<String, String> master, MasterSerializer serializer) {
		master.forEach((id, serializedRecord) -> add(new TripleRecord(id, serializer.deserialize(serializedRecord))));
	}

	private void loadDataMultiThread(IMap<String, String> master, MasterSerializer serializer) {
		try {
			ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);

			master.forEach((id, serializedRecord) -> threadPool.submit(() -> add(new TripleRecord(id, serializer.deserialize(serializedRecord)))));

			threadPool.shutdown();
			threadPool.awaitTermination(1, TimeUnit.HOURS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initHazelcastClient() {
		hazelcast = HazelcastClient.newHazelcastClient(config);
	}

	public void stop() {
		hazelcast.shutdown();
	}

	public void publish(String publisherName, Triple triple) {
		if(publisherName == null) throw new NullPointerException("Publisher name cannot be null");
		if(triple == null) throw new NullPointerException("Triple cannot be null");
		hazelcast.getTopic(REQUESTS_TOPIC).publish(publisherName + MESSAGE_SEPARATOR + triple);
	}

	private MasterSerializer serializer() {
		IMap<String, String> metadata = hazelcast.getMap(METADATA_MAP_NAME);
		return MasterSerializers.get(metadata.get("serializer"));
	}

	private void add(TripleRecord record) {
		switch(record.type()) {
			case "employee": addToEmployee(record); break;
			case "country": addToCountry(record); break;
			case "area": addToArea(record); break;
			case "region": addToRegion(record); break;
			case "theater": addToTheater(record); break;
			case "screen": addToScreen(record); break;
			case "dock": addToDock(record); break;
			case "screendock": addToScreenDock(record); break;
			case "depot": addToDepot(record); break;
			case "office": addToOffice(record); break;
			case "desk": addToDesk(record); break;
			case "asset": addToAsset(record); break;
			case "dualasset": addToDualAsset(record); break;
		}
	}

	private void remove(String id) {
		switch(typeOf(id)) {
			case "employee": removeFromEmployee(id); break;
			case "country": removeFromCountry(id); break;
			case "area": removeFromArea(id); break;
			case "region": removeFromRegion(id); break;
			case "theater": removeFromTheater(id); break;
			case "screen": removeFromScreen(id); break;
			case "dock": removeFromDock(id); break;
			case "screendock": removeFromScreenDock(id); break;
			case "depot": removeFromDepot(id); break;
			case "office": removeFromOffice(id); break;
			case "desk": removeFromDesk(id); break;
			case "asset": removeFromAsset(id); break;
			case "dualasset": removeFromDualAsset(id); break;
		}
	}

	private String typeOf(String id) {
		return id.contains(":") ? id.substring(id.indexOf(':') + 1) : NONE_TYPE;
	}

	public Employee employee(String id) {
		return employeeMap.get(id);
	}

	public List<Employee> employees() {
		return new ArrayList<>(employeeMap.values());
	}

	public Place place(String id) {
		// TODO: this will get removed
		return null;
	}

	public List<Place> places() {
		// TODO: this will get removed
		return null;
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
			addOrUpdateRecord(event.getKey(), event.getValue());
		}

		@Override
		public void entryUpdated(EntryEvent<String, String> event) {
			addOrUpdateRecord(event.getKey(), event.getValue());
		}

		@Override
		public void entryRemoved(EntryEvent<String, String> event) {
			remove(event.getKey());
		}

		@Override
		public void entryEvicted(EntryEvent<String, String> event) {
			remove(event.getKey());
		}

		private void addOrUpdateRecord(String id, String serializedRecord) {
			MasterSerializer serializer = serializer();
			add(new TripleRecord(id, serializer.deserialize(serializedRecord)));
		}
	}
}