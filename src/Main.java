import entities.*;
import exception.NoDriverAvailableException;
import service.DriverService;
import service.*;
import strategy.*;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        RiderService riderService = new RiderService();
        DriverService driverService = new DriverService();

        RideMatchingStrategy matchingStrategy = new NearestDriverStrategy();
        FareStrategy fareStrategy = new DefaultFareStrategy();

        RideService rideService = new RideService(fareStrategy, matchingStrategy, driverService);

        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Choose an option: ");
            String input = scanner.nextLine();

            try {
                switch (input) {
                    case "1":
                        System.out.print("Enter rider name: ");
                        String riderName = scanner.nextLine();
                        System.out.print("Enter rider x: ");
                        int riderX = scanner.nextInt();
                        System.out.print("Enter rider y: ");
                        int riderY = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        Rider rider = riderService.registerRider(riderName,riderX, riderY);
                        System.out.println("Rider added: " + rider);
                        break;

                    case "2":
                        System.out.print("Enter driver name: ");
                        String driverName = scanner.nextLine();
                        System.out.print("Enter driver x: ");
                        int driverX = scanner.nextInt();
                        System.out.print("Enter driver y: ");
                        int driverY = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        System.out.print("Enter vehicle type (BIKE/AUTO/CAR): ");
                        String vehicle = scanner.nextLine().toUpperCase();
                        VehicleType vehicleType = VehicleType.valueOf(vehicle);
                        System.out.println("Enter driver contact number: ");
                        String phoneNumber = scanner.nextLine();
                        Driver driver = driverService.registerDriver(driverName, phoneNumber,driverX, driverY, vehicleType);
                        System.out.println("Driver added: " + driver.toString());
                        break;

                    case "3":
                        System.out.println("Available Drivers:");
                        driverService.getAvailableDrivers().forEach(System.out::println);
                        break;

                    case "4":
                        System.out.print("Enter Rider ID: ");
                        String riderId = scanner.nextLine();
                        Rider selectedRider = riderService.getRiderByiD(riderId);

                        if (selectedRider == null) {
                            System.out.println("Invalid rider ID");
                            break;
                        }
                        System.out.println("enter destination distance x: ");
                        int x = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        System.out.println("enter destination distance y: ");
                        int y = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        System.out.println("Select matching strategy: 1. Nearest  2. Least Active");
                        String strategyChoice = scanner.nextLine();
                        if ("1".equals(strategyChoice)) {
                            rideService.updateRideMatchingStrategy(new NearestDriverStrategy());
                        } else if ("2".equals(strategyChoice)) {
                            rideService.updateRideMatchingStrategy(new LeastAcivetDriverStrategy());
                        }

                        Ride ride = rideService.requestRide(selectedRider, new Location(x,y)); // Assuming a fixed distance for simplicity
                        System.out.println("Ride booked successfully: " + ride);
                        break;

                    case "5":
                        System.out.print("Enter Ride ID to complete: ");
                        String rideId = scanner.nextLine();

                        System.out.println("Select fare strategy: 1. Default  2. Peak Hour");
                        String fareChoice = scanner.nextLine();
                        if ("1".equals(fareChoice)) {
                            rideService.updateFareStrategy(new DefaultFareStrategy());
                        } else if ("2".equals(fareChoice)) {
                            rideService.updateFareStrategy(new PeakHourFareStrategy());
                        }

                        FareReceipt receipt = rideService.completeRide(rideId);
                        System.out.println("Ride completed. Receipt: " + receipt);
                        break;

                    case "6":
                        System.out.println("All Rides:");
                        rideService.getAllRides().forEach(System.out::println);
                        break;

                    case "7":
                        running = false;
                        System.out.println("Exiting RideWise...");
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (NoDriverAvailableException | IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (Exception ex) {
                System.out.println("Unexpected error: " + ex.getMessage());
            }
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n==== RideWise Menu ====");
        System.out.println("1. Add Rider");
        System.out.println("2. Add Driver");
        System.out.println("3. View Available Drivers");
        System.out.println("4. Request Ride");
        System.out.println("5. Complete Ride");
        System.out.println("6. View Rides");
        System.out.println("7. Exit");
    }

}