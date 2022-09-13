package com.cinepolis.master;

import com.cinepolis.master.model.Master;
import com.cinepolis.master.model.entities.*;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryAddedListener;
import com.hazelcast.map.listener.EntryEvictedListener;
import com.hazelcast.map.listener.EntryRemovedListener;
import com.hazelcast.map.listener.EntryUpdatedListener;
import io.intino.alexandria.logger.Logger;
import io.intino.master.model.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static io.intino.master.model.Triple.SEPARATOR;

public class MasterClientTriplesFactorsLocalMaps implements Master {
	private static BiConsumer<String, Triple> publisher;
	private final Map<String, Consumer<Triple>> removers = new HashMap<>() {{
		put("employee", MasterClientTriplesFactorsLocalMaps.this::removeFromEmployee);
		put("country", MasterClientTriplesFactorsLocalMaps.this::removeFromCountry);
		put("area", MasterClientTriplesFactorsLocalMaps.this::removeFromArea);
		put("region", MasterClientTriplesFactorsLocalMaps.this::removeFromRegion);
		put("theater", MasterClientTriplesFactorsLocalMaps.this::removeFromTheater);
		put("screen", MasterClientTriplesFactorsLocalMaps.this::removeFromScreen);
		put("dock", MasterClientTriplesFactorsLocalMaps.this::removeFromDock);
		put("screendock", MasterClientTriplesFactorsLocalMaps.this::removeFromScreenDock);
		put("depot", MasterClientTriplesFactorsLocalMaps.this::removeFromDepot);
		put("office", MasterClientTriplesFactorsLocalMaps.this::removeFromOffice);
		put("desk", MasterClientTriplesFactorsLocalMaps.this::removeFromDesk);
		put("asset", MasterClientTriplesFactorsLocalMaps.this::removeFromAsset);
		put("dualasset", MasterClientTriplesFactorsLocalMaps.this::removeFromDualAsset);
	}};
	private final Map<String, Consumer<Triple>> adders = new HashMap<>() {{
		put("employee", MasterClientTriplesFactorsLocalMaps.this::addToEmployee);
		put("country", MasterClientTriplesFactorsLocalMaps.this::addToCountry);
		put("area", MasterClientTriplesFactorsLocalMaps.this::addToArea);
		put("region", MasterClientTriplesFactorsLocalMaps.this::addToRegion);
		put("theater", MasterClientTriplesFactorsLocalMaps.this::addToTheater);
		put("screen", MasterClientTriplesFactorsLocalMaps.this::addToScreen);
		put("dock", MasterClientTriplesFactorsLocalMaps.this::addToDock);
		put("screendock", MasterClientTriplesFactorsLocalMaps.this::addToScreenDock);
		put("depot", MasterClientTriplesFactorsLocalMaps.this::addToDepot);
		put("office", MasterClientTriplesFactorsLocalMaps.this::addToOffice);
		put("desk", MasterClientTriplesFactorsLocalMaps.this::addToDesk);
		put("asset", MasterClientTriplesFactorsLocalMaps.this::addToAsset);
		put("dualasset", MasterClientTriplesFactorsLocalMaps.this::addToDualAsset);
	}};
	private final Map<String, Employee> employeeMap = new HashMap<>();
	private final Map<String, Place> placeMap = new HashMap<>();
	private final Map<String, Country> countryMap = new HashMap<>();
	private final Map<String, Area> areaMap = new HashMap<>();
	private final Map<String, Region> regionMap = new HashMap<>();
	private final Map<String, Theater> theaterMap = new HashMap<>();
	private final Map<String, Screen> screenMap = new HashMap<>();
	private final Map<String, Dock> dockMap = new HashMap<>();
	private final Map<String, ScreenDock> screenDockMap = new HashMap<>();
	private final Map<String, Depot> depotMap = new HashMap<>();
	private final Map<String, Office> officeMap = new HashMap<>();
	private final Map<String, Desk> deskMap = new HashMap<>();
	private final Map<String, Asset> assetMap = new HashMap<>();
	private final Map<String, DualAsset> dualAssetMap = new HashMap<>();
	private final IMap<String, String> reverseSubjectFactors;
	private final IMap<String, String> reversePredicateFactors;
	private HazelcastInstance hz;

	public static MasterClientTriplesFactorsLocalMaps connect(String url) {
		ClientConfig cfg = new ClientConfig();
		cfg.getNetworkConfig().addAddress(url);
		return new MasterClientTriplesFactorsLocalMaps(cfg);
	}

	public MasterClientTriplesFactorsLocalMaps() {
		this(new ClientConfig());
	}

	public MasterClientTriplesFactorsLocalMaps(ClientConfig config) {
		configureLogger();
		this.hz = HazelcastClient.newHazelcastClient(config);
		reverseSubjectFactors = hz.getMap("reverseSubjectFactors");
		reversePredicateFactors = hz.getMap("reversePredicateFactors");
		IMap<String, String> master = hz.getMap("master");
		master.forEach((key, value) -> {
			final String[] subjectVerb = key.split(SEPARATOR);
			add(new Triple(reverseSubjectFactors.get(subjectVerb[0]).toString(), reversePredicateFactors.get(subjectVerb[1]), value));
		});
		master.addEntryListener(new TripleEntryDispatcher(), true);
		publisher = (publisher, triple) -> hz.getTopic("requests").publish(publisher + "##" + triple.toString());

		Logger.info("Master load complete: " + this);
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

	public void publish(String publisher, Triple triple) {
		if (this.publisher != null) this.publisher.accept(publisher, triple);
	}

	private void add(Triple triple) {
		adders.getOrDefault(typeOf(triple), t -> {}).accept(triple);
	}

	private void remove(Triple triple) {
		removers.getOrDefault(typeOf(triple), t -> {}).accept(triple);
	}

	private void addToEmployee(Triple triple) {
		if (!employeeMap.containsKey(triple.subject())) employeeMap.put(triple.subject(), new Employee(triple.subject(), this));
		employeeMap.get(triple.subject()).add(triple);
	}

	private void addToCountry(Triple triple) {
		if (!countryMap.containsKey(triple.subject())) countryMap.put(triple.subject(), new Country(triple.subject(), this));
		countryMap.get(triple.subject()).add(triple);
	}

	private void addToArea(Triple triple) {
		if (!areaMap.containsKey(triple.subject())) areaMap.put(triple.subject(), new Area(triple.subject(), this));
		areaMap.get(triple.subject()).add(triple);
	}

	private void addToRegion(Triple triple) {
		if (!regionMap.containsKey(triple.subject())) regionMap.put(triple.subject(), new Region(triple.subject(), this));
		regionMap.get(triple.subject()).add(triple);
	}

	private void addToTheater(Triple triple) {
		if (!theaterMap.containsKey(triple.subject())) theaterMap.put(triple.subject(), new Theater(triple.subject(), this));
		theaterMap.get(triple.subject()).add(triple);
	}

	private void addToScreen(Triple triple) {
		if (!screenMap.containsKey(triple.subject())) screenMap.put(triple.subject(), new Screen(triple.subject(), this));
		screenMap.get(triple.subject()).add(triple);
	}

	private void addToDock(Triple triple) {
		if (!dockMap.containsKey(triple.subject())) dockMap.put(triple.subject(), new Dock(triple.subject(), this));
		dockMap.get(triple.subject()).add(triple);
	}

	private void addToScreenDock(Triple triple) {
		if (!screenDockMap.containsKey(triple.subject())) screenDockMap.put(triple.subject(), new ScreenDock(triple.subject(), this));
		screenDockMap.get(triple.subject()).add(triple);
	}

	private void addToDepot(Triple triple) {
		if (!depotMap.containsKey(triple.subject())) depotMap.put(triple.subject(), new Depot(triple.subject(), this));
		depotMap.get(triple.subject()).add(triple);
	}

	private void addToOffice(Triple triple) {
		if (!officeMap.containsKey(triple.subject())) officeMap.put(triple.subject(), new Office(triple.subject(), this));
		officeMap.get(triple.subject()).add(triple);
	}

	private void addToDesk(Triple triple) {
		if (!deskMap.containsKey(triple.subject())) deskMap.put(triple.subject(), new Desk(triple.subject(), this));
		deskMap.get(triple.subject()).add(triple);
	}

	private void addToAsset(Triple triple) {
		if (!assetMap.containsKey(triple.subject())) assetMap.put(triple.subject(), new Asset(triple.subject(), this));
		assetMap.get(triple.subject()).add(triple);
	}

	private void addToDualAsset(Triple triple) {
		if (!dualAssetMap.containsKey(triple.subject())) dualAssetMap.put(triple.subject(), new DualAsset(triple.subject(), this));
		dualAssetMap.get(triple.subject()).add(triple);
	}

	private void removeFromEmployee(Triple triple) {
		employeeMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromCountry(Triple triple) {
		countryMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromArea(Triple triple) {
		areaMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromRegion(Triple triple) {
		regionMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromTheater(Triple triple) {
		theaterMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromScreen(Triple triple) {
		screenMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromDock(Triple triple) {
		dockMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromScreenDock(Triple triple) {
		screenDockMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromDepot(Triple triple) {
		depotMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromOffice(Triple triple) {
		officeMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromDesk(Triple triple) {
		deskMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromAsset(Triple triple) {
		assetMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private void removeFromDualAsset(Triple triple) {
		dualAssetMap.computeIfPresent(triple.subject(), (k, v) -> v.remove(triple));
	}

	private String typeOf(Triple triple) {
		String subject = triple.subject();
		return subject.contains(":") ? subject.substring(subject.indexOf(':') + 1) : "unknown";
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
		return "MasterTriplesFactors{" +
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

	public class TripleEntryDispatcher implements EntryAddedListener<String, String>, EntryUpdatedListener<String, String>, EntryRemovedListener<String, String>, EntryEvictedListener<String, String> {

		@Override
		public void entryAdded(EntryEvent<String, String> event) {
			add(triple(event));
		}

		@Override
		public void entryUpdated(EntryEvent<String, String> event) {
			add(triple(event));
		}

		@Override
		public void entryRemoved(EntryEvent<String, String> event) {
			remove(triple(event));
		}

		@Override
		public void entryEvicted(EntryEvent<String, String> event) {
			remove(triple(event));
		}

		private Triple triple(EntryEvent<String, String> event) {
			final String[] subjectVerb = event.getKey().split(SEPARATOR);
			return new Triple(reverseSubjectFactors.get(subjectVerb[0]).toString(), reversePredicateFactors.get(subjectVerb[1]), event.getValue());
		}
	}
}