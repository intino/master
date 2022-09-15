package com.cinepolis.master;

import com.cinepolis.master.model.entities.*;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface MasterClient {

	void start();
	void stop();

	HazelcastInstance hazelcast();

	Employee employee(String id);
	Stream<Employee> employees();
	default List<Employee> employeeList() {return employees().collect(Collectors.toList());}

	Place place(String id);
	Stream<Place> places();
	default List<Place> placeList() {return places().collect(Collectors.toList());}

	Country country(String id);
	Stream<Country> countries();
	default List<Country> countrieList() {return countries().collect(Collectors.toList());}

	Area area(String id);
	Stream<Area> areas();
	default List<Area> areaList() {return areas().collect(Collectors.toList());}

	Region region(String id);
	Stream<Region> regions();
	default List<Region> regionList() {return regions().collect(Collectors.toList());}

	Theater theater(String id);
	Stream<Theater> theaters();
	default List<Theater> theaterList() {return theaters().collect(Collectors.toList());}

	Screen screen(String id);
	Stream<Screen> screens();
	default List<Screen> screenList() {return screens().collect(Collectors.toList());}

	Dock dock(String id);
	Stream<Dock> docks();
	default List<Dock> dockList() {return docks().collect(Collectors.toList());}

	ScreenDock screenDock(String id);
	Stream<ScreenDock> screenDocks();
	default List<ScreenDock> screenDockList() {return screenDocks().collect(Collectors.toList());}

	Depot depot(String id);
	Stream<Depot> depots();
	default List<Depot> depotList() {return depots().collect(Collectors.toList());}

	Office office(String id);
	Stream<Office> offices();
	default List<Office> officeList() {return offices().collect(Collectors.toList());}

	Desk desk(String id);
	Stream<Desk> desks();
	default List<Desk> deskList() {return desks().collect(Collectors.toList());}

	Asset asset(String id);
	Stream<Asset> assets();
	default List<Asset> assetList() {return assets().collect(Collectors.toList());}

	DualAsset dualAsset(String id);
	Stream<DualAsset> dualAssets();
	default List<DualAsset> dualAssetList() {return dualAssets().collect(Collectors.toList());}
}