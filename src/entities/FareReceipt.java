package entities;

import strategy.FareStrategy;

import java.time.Instant;
import java.time.LocalDateTime;

public class FareReceipt {
   private String  rideID;
   private double fareAmount;
   private Instant timestamp;

    public FareReceipt(String rideID, double fareAmount) {
        this.rideID = rideID;
        this.fareAmount = fareAmount;
        this.timestamp = Instant.now();
        System.out.println("Fare receipt generated for ride ID: " + rideID + ", Fare Amount: " + fareAmount + ", Timestamp: " + timestamp);
    }

    public String getRideID() {
        return rideID;
    }

    public void setRideID(String rideID) {
        this.rideID = rideID;
    }

    public double getFareAmount() {
        return fareAmount;
    }

    public void setFareAmount(double fareAmount) {
        this.fareAmount = fareAmount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
