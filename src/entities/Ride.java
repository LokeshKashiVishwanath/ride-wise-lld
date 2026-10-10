 package entities;

import strategy.FareStrategy;

public class Ride {

    private String rideID;
    private  Driver driver;
    private Rider rider;
    private  Ridestatus ridestatus;
    private FareReceipt fareReceipt;
    private double distanceInKm;


    public Ride(String rideID, Driver driver, Rider rider, double distance) {
        this.rideID = rideID;
        this.driver = driver;
        this.rider = rider;
        this.ridestatus = Ridestatus.REQUESTED;
        this.distanceInKm =distance;

    }
    public void startRide(){
        this.ridestatus=Ridestatus.ACCEPTED;

    }
    public void assignRide(Driver driver){
        this.driver=driver;
        this.ridestatus=Ridestatus.ACCEPTED;
    }

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Rider getRider() {
        return rider;
    }

    public void setRider(Rider rider) {
        this.rider = rider;
    }

    public Ridestatus getRidestatus() {
        return ridestatus;
    }

    public void setRidestatus(Ridestatus ridestatus) {
        this.ridestatus = ridestatus;
    }

    public FareReceipt getFareReceipt() {
        return fareReceipt;
    }

    public void setFareReceipt(FareReceipt fareReceipt) {
        this.fareReceipt = fareReceipt;
    }

    public double getDistanceInKm() {
        return distanceInKm;
    }

    public void setDistanceInKm(double distanceInKm) {
        this.distanceInKm = distanceInKm;
    }

    public  void completeRide(FareReceipt fareReceipt){
        this.fareReceipt=fareReceipt;
        this.ridestatus=Ridestatus.COMPLETED;
    }
    public void cancelRide(){
        this.ridestatus=Ridestatus.CANCELLED;
    }
    public String toString() {
        return "Ride{" +
                "rideID='" + rideID + '\'' +
                ", driver=" + driver.getDriverName() +
                ", rider=" + rider.getRiderName() +
                ", ridestatus=" + ridestatus +
                ", fareReceipt=" + (fareReceipt != null ? fareReceipt.getFareAmount() : "N/A") +
                ", distanceInKm=" + distanceInKm +
                '}';
    }
}
