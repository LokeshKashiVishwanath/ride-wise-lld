package service;

import entities.*;
import exception.NoDriverAvailableException;
import strategy.FareStrategy;
import strategy.RideMatchingStrategy;
import util.IdGenerator;

import java.util.ArrayList;
import java.util.List;

public class RideService {
    private final List<Ride> rides = new ArrayList<>();

    private DriverService driverService;
    private FareStrategy fareStrategy;
    private RideMatchingStrategy rideMatchingStrategy;
    private FareReceipt receipt;

   public RideService (FareStrategy fareStrategy, RideMatchingStrategy rideMatchingStrategy,  DriverService driverService){

       this.fareStrategy = fareStrategy;
         this.rideMatchingStrategy = rideMatchingStrategy;
         this.driverService = driverService;
   }

   public Ride requestRide(Rider rider, Location distance){
       String rideID = IdGenerator.generateID("RIDE");
       Driver driver = rideMatchingStrategy.matchRider(rider, driverService.getAllDrivers()).orElseThrow(() -> new NoDriverAvailableException("No available drivers found for rider: " + rider.getRiderName()));
       Ride ride= new Ride(rideID,driver, rider, driver.calculateDistancetoRider(distance));

      ride.assignRide(driver);
      driver.setAvailable(false);
      rides.add(ride);
       return ride;
   }
   public void startRide(Ride ride){
       ride.startRide();
   }
   public void endRide(Ride ride){
       Driver driver = ride.getDriver();

       driver.endRide(ride);
       double finalFarePrice = fareStrategy.calculateFare(ride.getDistanceInKm());
       receipt= new FareReceipt(ride.getRideID(),finalFarePrice);
       ride.setRidestatus(entities.Ridestatus.COMPLETED);
       System.out.println("Ride completed. Fare: "+finalFarePrice);
   }
    public FareReceipt completeRide(String rideId) {
        Ride ride = getRideById(rideId);
        if (ride == null) {
            throw new IllegalArgumentException("Ride not found: " + rideId);
        }

        if (ride.getRidestatus() != Ridestatus.ACCEPTED) {
            throw new IllegalStateException("Only assigned rides can be completed");
        }

        double fare = fareStrategy.calculateFare(ride.getDistanceInKm());
        FareReceipt receipt = new FareReceipt(ride.getRideID(), fare);

        ride.completeRide(receipt);
        ride.getDriver().markAvailable();
        ride.getDriver().incrementTotalRidesCompleted();
        ride.getDriver().updateLocation(ride.getRider().getLocation());

        return receipt;
    }

    public void cancelRide(String rideId) {
        Ride ride = getRideById(rideId);
        if (ride == null) {
            throw new IllegalArgumentException("Ride not found: " + rideId);
        }

        if (ride.getRidestatus() == Ridestatus.ACCEPTED) {
            ride.cancelRide();
            ride.getDriver().markAvailable();
        }
    }

    public Ride getRideById(String rideId) {
        return rides.stream()
                .filter(r -> r.getRideID().equals(rideId))
                .findFirst()
                .orElse(null);
    }

    public void updateRideMatchingStrategy(RideMatchingStrategy rideMatchingStrategy) {
        this.rideMatchingStrategy = rideMatchingStrategy;
    }

    public void updateFareStrategy(FareStrategy fareStrategy) {
        this.fareStrategy = fareStrategy;
    }

    public List<Ride> getAllRides() {
        return rides;
    }


}
