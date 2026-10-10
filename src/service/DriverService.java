package service;

import entities.Driver;
import entities.VehicleType;

import java.util.ArrayList;
import java.util.List;

public class DriverService {
    private  static int driverIdGenerator=1;
    private  static List<Driver> drivers= new ArrayList<>();

    public Driver registerDriver(String name, String phoneNumber, int x, int y, VehicleType vehicleType){
        String driverID= "DRIVER"+driverIdGenerator++;

        Driver driver= new Driver(name, phoneNumber, driverID, new entities.Location(x,y), vehicleType);
        drivers.add(driver);
        return driver;
    }
    public void showAvailableDrivers(){
       drivers.stream().filter(Driver::isAvailable).forEach(driver -> System.out.println("Driver name: "+ driver.getDriverName()+", Driver ID: "+driver.getDriverID()));
    }

    public List<Driver> getAllDrivers() {
        return drivers;
    }
    public Driver getDriverById(String id) {
        return drivers.stream()
                .filter(d -> d.getDriverID().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void markAvailable(String driverId) {
        Driver driver = getDriverById(driverId);
        if (driver != null) {
            driver.markAvailable();
        }
    }

    public void markUnavailable(String driverId) {
        Driver driver = getDriverById(driverId);
        if (driver != null) {
            driver.markAvailable();
        }
    }
    public List<Driver> getAvailableDrivers() {
        List<Driver> availableDrivers = new ArrayList<>();
        for (Driver driver : drivers) {
            if (driver.isAvailable()) {
                availableDrivers.add(driver);
            }
        }
        return availableDrivers;
    }

}
