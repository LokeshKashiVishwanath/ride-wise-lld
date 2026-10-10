package strategy;

public class PeakHourFareStrategy implements FareStrategy {
    private  final double baseFare=12;
    private final double perKmRate= 5;
    @Override
    public double calculateFare(double distance) {
        return baseFare * (distance*perKmRate* 1.5); // 50% increase during peak hours
    }
}
