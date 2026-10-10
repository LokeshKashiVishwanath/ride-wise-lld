package entities;

public class Rider {

    private String riderName;
    private String riderID;
private Location location;

    public Rider(String riderName, String riderID, Location location) {
        this.riderName = riderName;
        this.riderID = riderID;

      this.location = location;
    }

    public String getRiderName() {
        return riderName;
    }

    public void setRiderName(String riderName) {
        this.riderName = riderName;
    }

    public String getRiderID() {
        return riderID;
    }

    public void setRiderID(String riderID) {
        this.riderID = riderID;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Rider{" +
                "riderName='" + riderName + '\'' +
                ", riderID='" + riderID + '\'' +
                ", location=" + location +
                '}';
    }
}
