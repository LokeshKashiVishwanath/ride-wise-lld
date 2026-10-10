# RideWise Project Requirements

## Project Overview
**RideWise** is a simplified, console-based ride-sharing system inspired by Uber and Ola. It demonstrates enterprise-level software design by allowing users to book rides, match them with nearby drivers, calculate fares dynamically, and track the entire ride lifecycle.

The goal is **not** to build a feature-complete app, but to showcase clean code architecture, SOLID principles, and thoughtful low-level system design.

---

## A. Functional Requirements

### 1. Rider Management
- **Register a new rider** with name and location (x, y coordinates)
- **Retrieve rider by ID** for ride booking
- **View all registered riders** (optional)
- Riders initiate ride requests and receive fare receipts

### 2. Driver Management
- **Register a new driver** with:
  - Name
  - Phone number
  - Current location (x, y coordinates)
  - Vehicle type (BIKE, AUTO, or CAR)
- **Mark driver as available** when they log in / finish a ride
- **Mark driver as unavailable** during an active ride
- **List all available drivers** visible to the rider service
- Drivers are matched to riders based on proximity or activity level

### 3. Ride Request & Matching
- Rider **requests a ride** from their current location
- System uses a **matching strategy** to find the best driver:
  - **Nearest Driver Strategy**: Pick driver closest to the rider
  - **Least Active Driver Strategy**: Pick driver with fewest completed rides
- Ride is **assigned** to the matched driver
- If no drivers are available, throw `NoDriverAvailableException`

### 4. Fare Calculation
- System calculates fare **based on distance traveled**
- Support multiple **pricing strategies**:
  - **Default Fare Strategy**: Base fare + per-km rate
  - **Peak Hour Fare Strategy**: Apply surge multiplier during peak hours
- Fare is calculated when the ride is **completed**

### 5. Ride Lifecycle & Status Tracking
Ride passes through these states:
1. **REQUESTED** — Rider has requested a ride
2. **ASSIGNED** — Driver has been matched
3. **IN_PROGRESS** — Driver and rider are on the road
4. **COMPLETED** — Ride finished, fare paid
5. **CANCELLED** — Ride was cancelled (optional)

### 6. Fare Receipt
- Generated when a ride is **completed**
- Contains:
  - Ride ID
  - Final fare amount
  - Timestamp of completion
- Acts as a proof of payment for the rider

---

## B. Non-Functional Requirements

### Code Quality & Maintainability
- **Low coupling**: Services depend on abstractions, not concrete classes
- **High cohesion**: Each class has a single, well-defined responsibility
- **Extensibility**: Adding new strategy implementations should not require changes to service layer
- **Readability**: Code is self-documenting with clear naming conventions

### Design Patterns
- **Strategy Pattern**: Pluggable ride-matching and fare-calculation algorithms
- **Composition**: Services compose strategies, not inherit from them
- **Dependency Injection**: Strategies and collaborators are injected, not created inside services

### Performance & Scalability (MVP level)
- In-memory storage (HashMaps/ArrayLists) — good enough for MVP
- No database required for this prototype
- Simple distance calculation (Euclidean distance on 2D plane)

---

## C. Domain Model

### Core Entities

#### **Rider**
```
- id: String (unique identifier)
- name: String
- location: (double x, double y)
```
Represents a passenger who requests rides.

#### **Driver**
```
- id: String (unique identifier)
- name: String
- phoneNumber: String
- currentLocation: (double x, double y)
- vehicleType: VehicleType (BIKE, AUTO, CAR)
- available: boolean
- ridesCompleted: int (for least-active strategy)
```
Represents a driver who accepts ride requests.

#### **Ride**
```
- id: String (unique identifier)
- rider: Rider
- driver: Driver
- pickupLocation: (double x, double y)
- dropoffLocation: (double x, double y) [optional for MVP]
- distanceInKm: double
- status: RideStatus
- createdAt: long (timestamp)
```
Represents a ride request and its journey.

#### **FareReceipt**
```
- rideId: String
- amount: double
- generatedAt: long (timestamp)
```
Represents proof of payment after a ride completes.

### Enums

#### **RideStatus**
```
REQUESTED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
```

#### **VehicleType**
```
BIKE, AUTO, CAR
```

---

## D. Strategy Interfaces (Composition Points)

### RideMatchingStrategy
Determines which driver is best-suited for a ride request.

```java
public interface RideMatchingStrategy {
    Driver findDriver(Rider rider, List<Driver> availableDrivers);
}
```

**Implementations:**
- **NearestDriverStrategy**: Finds driver with minimum distance to rider
- **LeastActiveDriverStrategy**: Finds driver with fewest completed rides

### FareStrategy
Calculates the fare based on ride context (distance, time of day, etc.).

```java
public interface FareStrategy {
    double calculateFare(Ride ride);
}
```

**Implementations:**
- **DefaultFareStrategy**: `fare = baseRate + (distanceInKm * perKmRate)`
- **PeakHourFareStrategy**: Applies a surge multiplier if current hour is 8-9 AM or 5-7 PM

---

## E. Service Layer

### RiderService
- `registerRider(name, x, y)` → creates and returns a new Rider
- `getRiderById(id)` → retrieves a rider by ID
- Handles all rider-related business logic

### DriverService
- `registerDriver(name, phone, x, y, vehicleType)` → creates and returns a new Driver
- `getAvailableDrivers()` → returns all drivers currently available
- `updateDriverAvailability(driverId, available)` → marks a driver as available/unavailable
- `markDriverActive(driverId)` → track that driver has an active ride
- Handles all driver-related business logic

### RideService
- `requestRide(rider, distance)` → initiates a ride request
- `completeRide(rideId)` → marks ride as complete, calculates fare
- `cancelRide(rideId)` → cancels an active ride
- `getAllRides()` → returns all rides (for viewing)
- Orchestrates ride lifecycle using injected strategies

---

## F. Console Application Flow

The `Main` class provides an interactive menu:

```
==== RideWise Menu ====
1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Exit
```

### User Flow Example
1. User adds 2 riders
2. User adds 3 drivers (with locations)
3. User views available drivers
4. User requests a ride for Rider 1
5. System automatically matches to nearest driver
6. User completes the ride and gets a fare receipt
7. User exits

---

## G. Design Principles Applied

### Why Strategy Pattern?
- **Problem**: Different ride-matching and pricing algorithms need to coexist
- **Solution**: Define interfaces and let clients swap implementations at runtime
- **Benefit**: New strategies can be added without modifying RideService

### Why Composition over Inheritance?
- **Problem**: Inheritance creates tight hierarchies that are hard to change
- **Solution**: Services *compose* strategies via dependency injection
- **Benefit**: Flexible and testable

### Why Dependency Injection?
- **Problem**: Services creating their own dependencies makes testing hard
- **Solution**: Pass dependencies into constructors
- **Benefit**: Easy to mock, swap implementations, and test in isolation

---

## H. Success Criteria

By the end, this project should:

✅ Compile and run without errors  
✅ Allow users to register riders and drivers via console menu  
✅ Match riders to drivers using pluggable strategies  
✅ Calculate fares using pluggable pricing models  
✅ Track ride lifecycle from REQUESTED → COMPLETED  
✅ Generate fare receipts  
✅ Demonstrate clear separation of concerns (entities, services, strategies)  
✅ Follow SOLID principles consistently  
✅ Be easily extensible (add new strategies without modifying core logic)  

---

## I. Future Enhancements (Out of Scope for MVP)

- Database persistence (replace in-memory storage)
- Real map integration (replace 2D coordinates with latitude/longitude)
- Cancellation fees and promo codes
- Rider ratings and driver ratings
- Multiple concurrent rides
- Push notifications
- Payment gateway integration

