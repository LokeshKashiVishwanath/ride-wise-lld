package strategy;

import entities.Driver;
import entities.Rider;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LeastAcivetDriverStrategy implements RideMatchingStrategy{
    @Override
    public Optional<Driver> matchRider(Rider rider, List<Driver> availableRiders) {
        return availableRiders.stream().filter(Driver::isAvailable).min(Comparator.comparingInt(driver -> driver.getCompletedRidesCount()));
    }
}
