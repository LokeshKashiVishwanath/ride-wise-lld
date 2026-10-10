package entities;

import java.time.Instant;

public class Driver {
    private String driverName;
    private String driverPhoneNumber;
    private String driverID;
    private boolean isAvailable;
    private  Location location;
    private VehicleType vehicleType;
    private int totalRidesCompleted=0;

 private Instant driverWaitingSince= Instant.now();
    public Driver(String driverName, String driverPhoneNumber, String driverID, Location location, VehicleType vehicleType) {
        this.driverName = driverName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.driverID = driverID;
        this.isAvailable = true;
        this.location = location;
        this.vehicleType=vehicleType;
    }
    public void updateLocation(Location location){
        this.location=location;

    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhoneNumber() {
        return driverPhoneNumber;
    }

    public void setDriverPhoneNumber(String driverPhoneNumber) {
        this.driverPhoneNumber = driverPhoneNumber;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }
    public  void incrementTotalRidesCompleted(){
        this.totalRidesCompleted++;
    }
    public int getCompletedRidesCount(){
        return this.totalRidesCompleted;
    }





    public double calculateDistancetoRider(Location location){
        int dx=this.location.getX()-location.getX();
        int dy=this.location.getY()-location.getY();
        return Math.sqrt(dx*dx+dy*dy);
    }
    public void markAvailable(){
        this.isAvailable=true;
        this.driverWaitingSince=Instant.now();
    }

    public void endRide(Ride ride) {
        this.isAvailable = true;
        this.driverWaitingSince = Instant.now();

    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
    public String toString() {
        return "Driver{" +
                "driverName='" + driverName + '\'' +
                ", driverPhoneNumber='" + driverPhoneNumber + '\'' +
                ", driverID='" + driverID + '\'' +
                ", isAvailable=" + isAvailable +
                ", location=" + location +
                ", vehicleType=" + vehicleType +
                '}';
    }
}
