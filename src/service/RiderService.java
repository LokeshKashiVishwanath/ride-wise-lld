package service;

import entities.Location;
import entities.Ride;
import entities.Rider;
import util.IdGenerator;

import java.util.*;

public class RiderService {
    private final static Map<String,Rider> riders= new HashMap<>();
    public Rider registerRider(String riderName, int x, int y) {
        String id= IdGenerator.generateID("RIDER");
        Rider rider= new Rider(riderName, id, new Location(x,y));
        riders.put(id,rider);
        return rider;
    }
    public Rider getRiderByiD(String riderID){
        return riders.get(riderID);
    }
    public Collection<Rider> getAllRiders() {
        return riders.values();
    }
}
