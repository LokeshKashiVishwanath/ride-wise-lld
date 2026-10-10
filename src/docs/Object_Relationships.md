# RideWise Object Relationships

This document describes how objects in the RideWise system relate to, depend on, and communicate with each other.

---

## Core Relationships

### 1. Rider ↔ Ride (Association)
**Type**: One-to-Many  
**Direction**: Unidirectional (Ride knows Rider, Rider doesn't reference Rides)

```
Rider (1) ←→ Ride (many)
```

**Explanation**:
- A single rider can request multiple rides over time
- Each `Ride` object holds a reference to the `Rider` who requested it
- The `Rider` entity itself doesn't maintain a list of rides; `RideService` manages that
- This avoids coupling and allows riders to be independent of ride history

**Code**:
```java
public class Ride {
    private Rider rider;  // Reference to who requested this ride
    // ...
}
```

---

### 2. Driver ↔ Ride (Association)
**Type**: One-to-Many  
**Direction**: Unidirectional (Ride knows Driver, Driver doesn't reference Rides)

```
Driver (1) ←→ Ride (many)
```

**Explanation**:
- A single driver accepts multiple rides
- Each `Ride` object holds a reference to the assigned `Driver`
- `Driver` tracks statistics (`ridesCompleted`) but not individual ride objects
- Keeps `Driver` lightweight and focused on state, not history

**Code**:
```java
public class Ride {
    private Driver driver;  // Reference to assigned driver
    // ...
}
```

---

### 3. Ride → FareReceipt (Composition)
**Type**: One-to-One  
**Direction**: Unidirectional (Receipt is generated *for* a ride)

```
Ride (1) ◆→ FareReceipt (1)
```

**Explanation**:
- Each completed ride generates exactly one `FareReceipt`
- Receipt is created *by* the system (in `RideService`), not by the ride itself
- Receipt contains reference to `rideId` but doesn't hold a reference to the full `Ride` object
- This is composition-like (receipt lifetime is tied to ride completion) but not strict composition

**Code**:
```java
public class FareReceipt {
    private String rideId;  // Links receipt to its originating ride
    private double amount;
    private long generatedAt;
}
```

---

### 4. RideService → RideMatchingStrategy (Dependency Injection)
**Type**: Composition via Dependency Injection  
**Direction**: Unidirectional (Service depends on interface)

```
RideService ──→ RideMatchingStrategy (interface)
                        ↑
                        │ implemented by
                  ┌─────┴──────┐
                  │            │
        NearestDriverStrategy  LeastActiveDriverStrategy
```

**Explanation**:
- `RideService` receives a `RideMatchingStrategy` via constructor
- The strategy interface defines the contract; implementations provide behavior
- Allows swapping driver-matching algorithms without changing `RideService`
- Follows **Dependency Inversion Principle**
- Enables testing with mock strategies

**Code**:
```java
public class RideService {
    private RideMatchingStrategy rideMatchingStrategy;
    
    public RideService(FareStrategy fareStrategy, 
                       RideMatchingStrategy rideMatchingStrategy,
                       DriverService driverService) {
        this.rideMatchingStrategy = rideMatchingStrategy;
        // ...
    }
    
    public Ride requestRide(Rider rider, double distance) {
        // Uses the injected strategy
        Driver driver = rideMatchingStrategy.findDriver(rider, 
                                    driverService.getAvailableDrivers());
        // ...
    }
}
```

---

### 5. RideService → FareStrategy (Dependency Injection)
**Type**: Composition via Dependency Injection  
**Direction**: Unidirectional (Service depends on interface)

```
RideService ──→ FareStrategy (interface)
                      ↑
                      │ implemented by
                ┌─────┴──────┐
                │            │
        DefaultFareStrategy  PeakHourFareStrategy
```

**Explanation**:
- `RideService` receives a `FareStrategy` via constructor
- The strategy interface defines the contract for calculating fares
- Different pricing models can be plugged in without modifying `RideService`
- Allows switching strategies at runtime (via `updateFareStrategy()` method)
- Follows **Strategy Pattern** and **OCP (Open Closed Principle)**

**Code**:
```java
public class RideService {
    private FareStrategy fareStrategy;
    
    public RideService(FareStrategy fareStrategy, 
                       RideMatchingStrategy rideMatchingStrategy,
                       DriverService driverService) {
        this.fareStrategy = fareStrategy;
        // ...
    }
    
    public FareReceipt completeRide(String rideId) {
        Ride ride = rides.get(rideId);
        // Uses the injected strategy to calculate fare
        double fare = fareStrategy.calculateFare(ride);
        FareReceipt receipt = new FareReceipt(rideId, fare);
        return receipt;
    }
    
    public void updateFareStrategy(FareStrategy strategy) {
        this.fareStrategy = strategy;  // Can swap at runtime
    }
}
```

---

### 6. RideService → DriverService (Service-to-Service Dependency)
**Type**: Composition  
**Direction**: Unidirectional (RideService uses DriverService)

```
RideService ──→ DriverService
```

**Explanation**:
- `RideService` depends on `DriverService` to:
  - Get list of available drivers (for ride matching)
  - Update driver availability (when assigning/completing rides)
  - Increment driver's ride count
- This is necessary coupling (not all coupling is bad)
- Kept minimal by accessing drivers only through service methods (not static fields)

**Code**:
```java
public class RideService {
    private DriverService driverService;
    
    public RideService(FareStrategy fareStrategy,
                       RideMatchingStrategy rideMatchingStrategy,
                       DriverService driverService) {
        this.driverService = driverService;
    }
    
    public Ride requestRide(Rider rider, double distance) {
        // Fetch available drivers from service
        List<Driver> availableDrivers = driverService.getAvailableDrivers();
        Driver driver = rideMatchingStrategy.findDriver(rider, availableDrivers);
        
        // Mark driver as unavailable
        driverService.updateDriverAvailability(driver.getId(), false);
        // ...
    }
    
    public FareReceipt completeRide(String rideId) {
        // ...
        // Mark driver as available again
        driverService.updateDriverAvailability(driver.getId(), true);
        // Increment driver's statistics
        driverService.incrementDriverRideCount(driver.getId());
        // ...
    }
}
```

---

### 7. Main → Services (Service Locator / Injection)
**Type**: Composition  
**Direction**: Unidirectional (Main creates and uses services)

```
Main ──→ RiderService
     ├──→ DriverService
     └──→ RideService
          ├──→ RideMatchingStrategy
          └──→ FareStrategy
```

**Explanation**:
- `Main` is the application entry point and wiring point
- Creates all services and strategies
- Passes strategies to `RideService` (dependency injection setup)
- All user interaction happens through `Main`
- Services are never instantiated within `RideService`; instead, they're injected
- This is the **Composition Root** pattern

**Code**:
```java
public class Main {
    public static void main(String[] args) {
        // Create services
        RiderService riderService = new RiderService();
        DriverService driverService = new DriverService();
        
        // Create strategies
        RideMatchingStrategy matchingStrategy = new NearestDriverStrategy();
        FareStrategy fareStrategy = new DefaultFareStrategy();
        
        // Inject strategies into RideService (Dependency Injection)
        RideService rideService = new RideService(
            fareStrategy, 
            matchingStrategy, 
            driverService
        );
        
        // Now use services through the menu loop
        // ...
    }
}
```

---

## Communication Flow

### Scenario: User Requests a Ride

```
Main (user input)
  │
  ├─→ riderService.getRiderById(riderId)
  │       └─→ Returns: Rider object
  │
  ├─→ rideService.updateRideMatchingStrategy(new NearestDriverStrategy())
  │       └─→ Swaps the strategy used for matching
  │
  └─→ rideService.requestRide(rider, distance)
          │
          ├─→ driverService.getAvailableDrivers()
          │       └─→ Returns: List<Driver> of available drivers
          │
          ├─→ rideMatchingStrategy.findDriver(rider, availableDrivers)
          │       └─→ Returns: Driver (closest one)
          │
          ├─→ driverService.updateDriverAvailability(driver.getId(), false)
          │       └─→ Marks driver as busy
          │
          └─→ Returns: Ride object (in ASSIGNED status)

Main displays: "Ride booked successfully!"
```

### Scenario: User Completes a Ride

```
Main (user input)
  │
  └─→ rideService.completeRide(rideId)
          │
          ├─→ fareStrategy.calculateFare(ride)
          │       └─→ Returns: double (fare amount)
          │
          ├─→ driverService.updateDriverAvailability(driver.getId(), true)
          │       └─→ Marks driver as available again
          │
          ├─→ driverService.incrementDriverRideCount(driver.getId())
          │       └─→ Updates driver's completed ride count
          │
          └─→ Returns: FareReceipt object

Main displays: "Ride completed. Receipt: ..."
```

---

## Object Lifetime & Ownership

| Object | Created By | Owned By | Lifetime |
|--------|-----------|----------|----------|
| **Rider** | `RiderService.registerRider()` | `RiderService` | Until app exits |
| **Driver** | `DriverService.registerDriver()` | `DriverService` | Until app exits |
| **Ride** | `RideService.requestRide()` | `RideService` | Until app exits (persisted) |
| **FareReceipt** | `RideService.completeRide()` | `RideService` | Until app exits (persisted) |
| **Strategy** | `Main.main()` | `RideService` | Entire app lifecycle (can be swapped) |

---

## Dependency Graph

```
┌──────────────────────────────────────────────────────────────┐
│                           Main                               │
│                    (Composition Root)                        │
└──────────────┬───────────────────────────────┬───────────────┘
               │                               │
               v                               v
         ┌───────────────┐           ┌──────────────────┐
         │ RiderService  │           │ DriverService    │
         │ (no deps)     │           │ (no deps)        │
         └───────────────┘           └──────────────────┘
                │                            ▲
                │                            │ used by
                │                            │
                └─────────┬──────────────────┘
                          │
                          v
                  ┌──────────────────┐
                  │  RideService     │
                  │ (Depends on:)    │
                  │ • FareStrategy   │
                  │ • RideMatching   │
                  │ • DriverService  │
                  └──────────────────┘
                      ▲      ▲
                      │      │
           strategy   │      │ strategy
           injectin   │      │ injection
                      │      │
        ┌─────────────┴───┐ ┌┴──────────────────┐
        │ RideMatching    │ │   FareStrategy    │
        │  Strategy       │ │   (interface)     │
        │ (interface)     │ │                   │
        ├─────────────────┤ ├───────────────────┤
        │ • Nearest       │ │ • Default         │
        │ • LeastActive   │ │ • PeakHour        │
        └─────────────────┘ └───────────────────┘
```

---

## Design Principles in Relationships

### Single Responsibility Principle (SRP)
- Each service has one reason to change:
  - `RiderService` changes only when rider logic changes
  - `DriverService` changes only when driver logic changes
  - `RideService` changes only when ride orchestration logic changes

### Open/Closed Principle (OCP)
- Can add new strategies without modifying `RideService`:
  - New `RideMatchingStrategy` implementation? Add it, plug it in.
  - New `FareStrategy` implementation? Add it, plug it in.
  - No modification to existing code needed.

### Liskov Substitution Principle (LSP)
- Any `RideMatchingStrategy` implementation can replace another:
  ```java
  rideService.updateRideMatchingStrategy(new NearestDriverStrategy());
  // Later...
  rideService.updateRideMatchingStrategy(new LeastActiveDriverStrategy());
  // Both work identically from RideService's perspective
  ```

### Interface Segregation Principle (ISP)
- Strategies defined as minimal, focused interfaces:
  - `RideMatchingStrategy` has only one method: `findDriver()`
  - `FareStrategy` has only one method: `calculateFare()`
  - No bloated interfaces with unused methods

### Dependency Inversion Principle (DIP)
- Services depend on abstractions (interfaces), not concrete classes:
  ```java
  // Good: Depends on interface
  private FareStrategy fareStrategy;
  
  // Bad would be:
  // private DefaultFareStrategy fareStrategy;
  ```

---

## Extensibility Points (How to Add Features)

### 1. Add a New Driver Matching Strategy
```java
public class RandomDriverStrategy implements RideMatchingStrategy {
    @Override
    public Driver findDriver(Rider rider, List<Driver> availableDrivers) {
        return availableDrivers.get(new Random().nextInt(availableDrivers.size()));
    }
}

// In Main:
rideService.updateRideMatchingStrategy(new RandomDriverStrategy());
```
**No changes to RideService** ✅

### 2. Add a New Fare Strategy
```java
public class DistanceBasedSurgeStrategy implements FareStrategy {
    @Override
    public double calculateFare(Ride ride) {
        double base = 50;
        double perKm = 15;
        double distance = ride.getDistance();
        
        if (distance > 20) {
            return (base + (distance * perKm)) * 1.5;  // 50% surge for long rides
        }
        return base + (distance * perKm);
    }
}

// In Main:
rideService.updateFareStrategy(new DistanceBasedSurgeStrategy());
```
**No changes to RideService** ✅

### 3. Add a New Entity Relationship (Future)
When adding payment or user ratings:
```java
Ride ──→ Payment (Composition)
Driver ◆─→ Rating (Aggregation)
```
These would be added without modifying existing relationships.

---

## Summary

RideWise uses **composition over inheritance** and **dependency injection** to create:
- **Loose coupling**: Services don't tightly depend on each other
- **High cohesion**: Each class has focused responsibilities
- **Extensibility**: New strategies can be added without modifying core logic
- **Testability**: Dependencies can be mocked or replaced for testing
- **Maintainability**: Relationships are clear and well-defined

This makes the system **SOLID**, **maintainable**, and **scalable** for future enhancements.

