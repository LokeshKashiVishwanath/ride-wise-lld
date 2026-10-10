package strategy;

public class DefaultFareStrategy implements FareStrategy {

    private  final double baseFare=12;
    private final double perKmRate= 5;
    @Override
    public double calculateFare(double distance) {
        return baseFare + (distance * perKmRate); // Example calculation
    }
}
