package strategy;

import entities.Driver;
import entities.Rider;

import java.util.List;
import java.util.Optional;

public interface RideMatchingStrategy {
    Optional<Driver> matchRider(Rider rider, List<Driver> availableRiders);
}
