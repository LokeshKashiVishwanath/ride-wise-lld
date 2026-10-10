# RideWise Class Model

This document describes each class in the RideWise system, its responsibilities, key attributes, and methods.

---

## Entity Classes (Package: `entities`)

### Rider
**Purpose**: Represents a passenger who can request rides.

**Attributes**:
- `id: String` — Unique identifier (UUID)
- `name: String` — Rider's name
- `location: Location` — Current location (x, y coordinates)

**Key Methods**:
- `getId()` → String
- `getName()` → String
- `getLocation()` → Location
- `toString()` → String (for console display)

**Design Note**: Rider is immutable once created. They don't manage their own rides; `RideService` tracks those.

---

### Driver
**Purpose**: Represents a driver who accepts and fulfills ride requests.

**Attributes**:
- `id: String` — Unique identifier (UUID)
- `name: String` — Driver's name
- `phoneNumber: String` — Contact number
- `currentLocation: Location` — Current location (x, y coordinates)
- `vehicleType: VehicleType` — Type of vehicle (BIKE, AUTO, CAR)
- `available: boolean` — Is the driver currently available for rides?
- `ridesCompleted: int` — Counter for completed rides (used by least-active strategy)

**Key Methods**:
- `getId()` → String
- `getName()` → String
- `getPhoneNumber()` → String
- `getCurrentLocation()` → Location
- `getVehicleType()` → VehicleType
- `isAvailable()` → boolean
- `setAvailable(boolean)` → void
- `getRidesCompleted()` → int
- `incrementRidesCompleted()` → void
- `calculateDistanceTo(Rider)` → double (Euclidean distance)
- `toString()` → String

**Design Note**: Non-immutable. Mutable attributes are `available` and `ridesCompleted`, which change as drivers accept/complete rides.

---

### Ride
**Purpose**: Represents a ride request and its current state throughout its lifecycle.

**Attributes**:
- `id: String` — Unique identifier (UUID)
- `rider: Rider` — Reference to the rider who requested the ride
- `driver: Driver` — Reference to the assigned driver (initially null)
- `distance: double` — Distance in kilometers
- `status: RideStatus` — Current state (REQUESTED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED)
- `createdAt: long` — Timestamp when ride was requested

**Key Methods**:
- `getId()` → String
- `getRider()` → Rider
- `getDriver()` → Driver
- `setDriver(Driver)` → void
- `getDistance()` → double
- `getStatus()` → RideStatus
- `setStatus(RideStatus)` → void
- `getCreatedAt()` → long
- `toString()` → String

**Design Note**: Mutable by design. State changes as the ride progresses. `RideService` manages all state transitions.

---

### FareReceipt
**Purpose**: Represents a proof of payment/receipt generated when a ride completes.

**Attributes**:
- `rideId: String` — ID of the completed ride
- `amount: double` — Final fare amount (in currency units)
- `generatedAt: long` — Timestamp when receipt was generated

**Key Methods**:
- `getRideId()` → String
- `getAmount()` → double
- `getGeneratedAt()` → long
- `toString()` → String (shows receipt details)

**Design Note**: Immutable once created. Acts as a final, timestamped proof of payment.

---

### Location
**Purpose**: Simple value object representing a geographic point in 2D space.

**Attributes**:
- `x: double` — X coordinate
- `y: double` — Y coordinate

**Key Methods**:
- `getX()` → double
- `getY()` → double
- `distanceTo(Location)` → double (Euclidean distance formula: sqrt((x2-x1)² + (y2-y1)²))

**Design Note**: Immutable. Used by both `Rider` and `Driver` to represent positions.

---

### Enums

#### RideStatus
```java
public enum RideStatus {
    REQUESTED,    // Ride has been requested by user
    ASSIGNED,     // Driver has been matched
    IN_PROGRESS,  // Ride is active
    COMPLETED,    // Ride finished, passenger dropped off
    CANCELLED     // Ride was cancelled
}
```

#### VehicleType
```java
public enum VehicleType {
    BIKE,  // Motorcycle/scooter (cheapest)
    AUTO,  // Tuk-tuk/auto (mid-range)
    CAR    // Car/sedan (premium)
}
```

---

## Strategy Interfaces (Package: `strategy`)

### RideMatchingStrategy
**Purpose**: Abstracts the logic for selecting which driver should get a ride.

```java
public interface RideMatchingStrategy {
    Driver findDriver(Rider rider, List<Driver> availableDrivers) 
        throws NoDriverAvailableException;
}
```

**Why an interface?**
- Different ride-matching algorithms can be plugged in without modifying `RideService`
- Supports OCP (Open Closed Principle)
- Enables easy testing with mock implementations

**Implementations**:

#### NearestDriverStrategy
- **Logic**: Finds the driver with the minimum distance to the rider
- **Use case**: Minimizes pickup time
- **Complexity**: O(n) where n = number of available drivers

#### LeastActiveDriverStrategy
- **Logic**: Finds the driver with the fewest completed rides
- **Use case**: Load balancing across drivers
- **Complexity**: O(n) where n = number of available drivers

---

### FareStrategy
**Purpose**: Abstracts the logic for calculating fare amounts.

```java
public interface FareStrategy {
    double calculateFare(Ride ride);
}
```

**Why an interface?**
- Different pricing models (base + distance, surge pricing, etc.) can coexist
- Supports OCP and SRP
- Easy to extend with new pricing rules

**Implementations**:

#### DefaultFareStrategy
- **Formula**: `fare = BASE_RATE + (distance * PER_KM_RATE)`
- **Example**: Base ₹50 + ₹15/km
- **When to use**: Off-peak hours, general pricing

#### PeakHourFareStrategy
- **Formula**: `fare = (BASE_RATE + (distance * PER_KM_RATE)) * SURGE_MULTIPLIER`
- **Surge hours**: 8-9 AM and 5-7 PM (typical rush hour)
- **Multiplier**: 1.5x or 2x during peak
- **When to use**: High-demand time periods

---

## Service Layer (Package: `service`)

### RiderService
**Purpose**: Manages all rider-related operations.

**Responsibilities**:
- Register new riders
- Retrieve riders by ID
- Maintain a registry of all riders
- Validate rider data

**Key Methods**:
- `registerRider(name: String, x: double, y: double) → Rider`
  - Creates a new rider with unique ID and location
  - Stores in internal registry
  - Returns the created rider object

- `getRiderById(id: String) → Rider`
  - Retrieves a registered rider by ID
  - Returns null if not found (should throw exception in production)

- `getAllRiders() → List<Rider>`
  - Returns all registered riders

**Storage**: Uses a HashMap (`riders`) keyed by rider ID.

**Design Note**: Stateless service (depends only on its data, not other services).

---

### DriverService
**Purpose**: Manages all driver-related operations and availability.

**Responsibilities**:
- Register new drivers
- Retrieve drivers by ID
- Track driver availability
- Filter available drivers
- Update driver status and statistics

**Key Methods**:
- `registerDriver(name, phone, x, y, vehicleType) → Driver`
  - Creates a new driver with unique ID
  - Sets initial status as available
  - Stores in internal registry
  - Returns the created driver object

- `getAvailableDrivers() → List<Driver>`
  - Critical for ride matching
  - Returns only drivers where `available == true`
  - Strategy implementations call this

- `getDriverById(id: String) → Driver`
  - Retrieves a driver by ID
  - Returns null if not found

- `updateDriverAvailability(driverId: String, available: boolean) → void`
  - Marks a driver as available (after completing a ride)
  - Marks a driver as unavailable (when accepting a ride)

- `incrementDriverRideCount(driverId: String) → void`
  - Increments `ridesCompleted` counter
  - Called when ride completes
  - Used by `LeastActiveDriverStrategy`

**Storage**: Uses a HashMap (`drivers`) keyed by driver ID.

**Design Note**: Depends on nothing; provides data to `RideService`.

---

### RideService
**Purpose**: Orchestrates the entire ride lifecycle using injected strategies.

**Responsibilities**:
- Handle ride requests
- Use matching strategy to assign drivers
- Use fare strategy for pricing
- Track ride status changes
- Generate receipts
- Coordinate between `RiderService` and `DriverService`

**Key Methods**:
- `requestRide(rider: Rider, distance: double) → Ride`
  - Creates a new ride in REQUESTED status
  - Calls matching strategy to find an available driver
  - Transitions ride to ASSIGNED status
  - Marks driver as unavailable
  - Returns the ride object
  - **Throws**: `NoDriverAvailableException` if no drivers found

- `completeRide(rideId: String) → FareReceipt`
  - Retrieves the ride by ID
  - Calls fare strategy to calculate final amount
  - Creates and returns a FareReceipt
  - Marks driver as available again
  - Increments driver's ride count
  - Updates ride status to COMPLETED

- `cancelRide(rideId: String) → void`
  - Marks ride as CANCELLED
  - Makes driver available immediately
  - Removes from active rides

- `getAllRides() → List<Ride>`
  - Returns all rides (for viewing/logging)

- `updateRideMatchingStrategy(strategy) → void`
  - Allows swapping strategies without restarting the app

- `updateFareStrategy(strategy) → void`
  - Allows swapping strategies without restarting the app

**Dependencies**:
- Injected: `FareStrategy`
- Injected: `RideMatchingStrategy`
- Uses: `DriverService` (to access available drivers)

**Storage**: Uses a HashMap (`rides`) keyed by ride ID.

**Design Note**: 
- Central orchestrator
- Depends on interfaces (strategies), not concrete classes
- Follows DIP (Dependency Inversion Principle)
- Loosely coupled with services through abstraction

---

## Exception Classes (Package: `exception`)

### NoDriverAvailableException
**Purpose**: Thrown when a rider requests a ride but no drivers are currently available.

**When thrown**: In `RideService.requestRide()` when matching strategy returns null/empty result.

**Why custom exception?**
- More semantic than generic Exception
- Allows callers to handle this specific case
- Follows good exception design practices

---

## Utility Classes (Package: `util`)

### IdGenerator
**Purpose**: Generates unique IDs for entities.

**Key Methods**:
- `generateId() → String`
  - Returns UUID-based unique identifier
  - Used by services when creating new entities

**Design Note**: Static utility; can be replaced with a dependency-injected service later.

---

## Console Application (Main.java)

**Purpose**: Entry point; provides menu-driven user interaction.

**Responsibilities**:
- Display menu options
- Collect user input
- Call services with user data
- Display results to console
- Handle user errors gracefully

**Key Features**:
- Menu loop until user exits
- Exception handling for invalid inputs
- Clear separation between UI and business logic
- All business logic delegated to services

**Design Note**: All printing and scanning happens here; services never print output.

---

## Complete Class Diagram Summary

```
┌─────────────────────────────────────────────────────────┐
│                      Main (Console UI)                  │
└────────────────────┬────────────────────────────────────┘
                     │
    ┌────────────────┼────────────────┐
    │                │                │
    v                v                v
┌─────────────┐ ┌──────────────┐ ┌──────────────┐
│RiderService │ │DriverService │ │ RideService  │
└─────────────┘ └──────────────┘ └──────────────┘
                                   │         │
                    ┌──────────────┴─────────┴──────────┐
                    │                                   │
                    v                                   v
          ┌─────────────────────┐         ┌──────────────────────┐
          │ RideMatchingStrategy│         │   FareStrategy       │
          │ (interface)         │         │   (interface)        │
          ├─────────────────────┤         ├──────────────────────┤
          │ • Nearest           │         │ • Default            │
          │ • LeastActive       │         │ • PeakHour           │
          └─────────────────────┘         └──────────────────────┘

Entities connected:
 Rider ←→ Ride ←→ Driver
           ↓
       FareReceipt
```

---

## Key Design Observations

1. **No inheritance** — All behavior comes from composition and interfaces
2. **Immutable entities** — Rider and FareReceipt don't change; Ride and Driver do (by design)
3. **Dependency injection** — Services receive strategies in constructors
4. **Single responsibility** — Each class does one thing well
5. **Strategy pattern** — Ride matching and fare calculation are pluggable
6. **Separation of concerns** — UI (Main) vs. Business Logic (services) vs. Domain (entities)

