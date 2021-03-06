package civ.engine;

import civ.*;
import civ.enums.CityStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;


public class CityManager {
    World world;
    TerrainManager terrainManager;
    private static boolean DEBUG = false;
    
    public CityManager(World world, TerrainManager terrainManager) {
        this.world = world;
        this.terrainManager = terrainManager;
    }
    
    public City createCity(String name, CityStyle type, MapLocation mapLocation) {
        City city = new City(name, type, mapLocation);
        findAvailableResources(city);
        calculateResources(city);
        return city;
    }
    
    /**
     * This should look at the tiles surrounding a city given its radius, and 
     * store the available mapLocations in the cities available resources arrayList
     * @param city The city that we are finding resources for.
     */
    public void findAvailableResources(City city) {
        MapLocation topLeftLocation = new MapLocation(city.getMapLocation().x - city.getRadius(),
                city.getMapLocation().y - city.getRadius());
        
        if (topLeftLocation.x < 0) {
            topLeftLocation.x = world.getMap().wraps() ? world.getMap().getMapWidth() + topLeftLocation.x : 0;
        }
        if (topLeftLocation.y < 0) {
            topLeftLocation.y = 0;
        }
        
        ArrayList<MapLocation> resourceList = new ArrayList<MapLocation>();
        
        int yLimit = topLeftLocation.y + (city.getRadius() * 2) + 1;
        if (yLimit > world.getMap().getMapHeight()) {
            yLimit = world.getMap().getMapHeight();
        }
        
        int xLimit = topLeftLocation.x + (city.getRadius() * 2) + 1;
        if (xLimit > world.getMap().getMapWidth() - 1) {
            xLimit = world.getMap().wraps() ? xLimit - world.getMap().getMapWidth() : world.getMap().getMapWidth();
        }
        
        for (;topLeftLocation.y < yLimit; topLeftLocation.y++) {
            for (int i = topLeftLocation.x; i != xLimit; i++) {
                if (i > world.getMap().getMapWidth() - 1) {
                    i = 0;
                }
                MapLocation m = new MapLocation(i, topLeftLocation.y);
                if (!city.getMapLocation().compare(m)) {
                    resourceList.add(m);
                }
            }
        }
        
        city.setAvailableResources(resourceList);    
        if (DEBUG) {
            System.out.println(resourceList.size());
            Iterator it = resourceList.iterator();
            System.out.println("Map Dimensions: " + world.getMap().getMapWidth() + ":" + world.getMap().getMapHeight());
            System.out.println("City Location: " + city.getMapLocation().toString());
            while (it.hasNext()) {
                System.out.println(it.next().toString());
            }
        }
    }
    
    /**
     * Calculates a cities production value, food value, and science value based
     * on the resources around it. Defaults to Max Production.
     * @param city The city to be calculated.
     */
    public void calculateResources(City city) {        
        
        PriorityQueue<ResourceObj> queue 
                = new PriorityQueue<ResourceObj>((city.getRadius() * (city.getRadius() + 1)) - 1, getComparator());
        for (MapLocation rLoc : city.getAvailableResources()) {
            queue.add(new ResourceObj(city.getResourceWeight(), terrainManager.getTerrainInfo(rLoc)));
        }
        
        TerrainInfo cityLocationInfo = terrainManager.getTerrainInfo(city.getMapLocation());
        
        int prod = cityLocationInfo.getProductionResource(), food = cityLocationInfo.getFoodResource() , sci = cityLocationInfo.getScienceResource();
        
        for (int i = 0; i < city.getLevel(); i++) {
            ResourceObj value = queue.poll();
            prod += value.getProduction();
            food += value.getFood();
            sci += value.getScience();
        }
        
        city.setProduction(prod);
        city.prodBonus = prod - cityLocationInfo.getProductionResource();
        city.setFood(food);
        city.foodBonus = food - cityLocationInfo.getFoodResource();
        city.setScience(sci);
        city.sciBonus = sci - cityLocationInfo.getScienceResource();
    }

    private Comparator<ResourceObj> getComparator() {
        return new Comparator<ResourceObj>() {
            @Override
            public int compare(ResourceObj res1, ResourceObj res2) {
                int compare1 = res1.calculateValue();
                int compare2 = res2.calculateValue();
                if (compare1 < compare2) {
                    return 1;
                } else if (compare1 > compare2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
    }

    public void nextTurn(Player currentPlayer) {
       ArrayList<City> playerCities = currentPlayer.getCities(); 
       currentPlayer.resetScience();
        if (!playerCities.isEmpty()) {
            for (City city : playerCities) {
                calculateResources(city);
                city.nextTurn();
            }
        }
        if (DEBUG) {
            System.out.println("GOLD: " + String.valueOf(currentPlayer.getGold()));
            System.out.println("SCI: " + String.valueOf(currentPlayer.getScience()));
        }
    }
    
    private class ResourceObj {
        public WeightedResourceObj resourceWeight;
        public TerrainInfo terrainInfo;
        public int debugger;
        
        public ResourceObj(WeightedResourceObj w, TerrainInfo t) {
            resourceWeight = w;
            terrainInfo = t;
        }
        
        public int calculateValue() {
            debugger = resourceWeight.prodWeight * terrainInfo.getProductionResource() +
                    resourceWeight.foodWeight * terrainInfo.getFoodResource() +
                    resourceWeight.sciWeight * terrainInfo.getScienceResource();
            return debugger;
        }
        
        public int getProduction() {
            return terrainInfo.getProductionResource();
        }
        
        public int getFood() {
            return terrainInfo.getFoodResource();
        }
        
        public int getScience() {
            return terrainInfo.getScienceResource();
        }
    }
}
