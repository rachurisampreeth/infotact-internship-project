package com.aerosaga.backend.workflow;

import java.util.ArrayList;
import java.util.List;

public class AeroSagaUtils {

    public static void main(String[] args) {
        System.out.println("AeroSaga Utility Module");
        System.out.println("-----------------------");

        System.out.println("Distance: " + calculateDistance(10, 20, 30, 40));
        System.out.println("Fuel needed: " + calculateFuel(500, 10));
        System.out.println("Flight status: " + getFlightStatus(75));

        List<String> flights = new ArrayList<>();
        flights.add("AS101");
        flights.add("AS102");
        flights.add("AS103");

        displayFlights(flights);
    }

    public static double calculateDistance(
            double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static double calculateFuel(double distance, double rate) {
        if (distance < 0 || rate < 0) {
            throw new IllegalArgumentException(
                    "Distance and rate cannot be negative");
        }
        return distance * rate / 100.0;
    }

    public static String getFlightStatus(int progress) {
        if (progress < 0 || progress > 100) {
            return "Invalid progress";
        }

        if (progress == 100) {
            return "Completed";
        }

        if (progress >= 75) {
            return "Landing Soon";
        }

        if (progress >= 40) {
            return "In Transit";
        }

        return "Preparing for Departure";
    }

    public static void displayFlights(List<String> flights) {
        System.out.println("\nAvailable Flights:");

        if (flights == null || flights.isEmpty()) {
            System.out.println("No flights available.");
            return;
        }

        for (int i = 0; i < flights.size(); i++) {
            System.out.println((i + 1) + ". " + flights.get(i));
        }
    }

    public static boolean isValidFlightId(String flightId) {
        if (flightId == null || flightId.isEmpty()) {
            return false;
        }

        return flightId.matches("[A-Z]{2}\\d{3,5}");
    }

    public static double convertKilometersToMiles(double kilometers) {
        return kilometers * 0.621371;
    }

    public static double convertMilesToKilometers(double miles) {
        return miles / 0.621371;
    }

    public static double calculateAverageSpeed(
            double distance, double hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException(
                    "Hours must be greater than zero");
        }

        return distance / hours;
    }

    public static int estimateTravelMinutes(
            double distance, double speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException(
                    "Speed must be greater than zero");
        }

        return (int) Math.ceil(distance / speed * 60);
    }

    public static String formatDuration(int totalMinutes) {
        if (totalMinutes < 0) {
            throw new IllegalArgumentException(
                    "Duration cannot be negative");
        }

        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        return hours + " hours " + minutes + " minutes";
    }

    public static int calculateRemainingSeats(
            int capacity, int occupied) {
        if (capacity < 0 || occupied < 0 || occupied > capacity) {
            throw new IllegalArgumentException(
                    "Invalid seat information");
        }

        return capacity - occupied;
    }

    public static boolean isFlightFull(int capacity, int occupied) {
        return calculateRemainingSeats(capacity, occupied) == 0;
    }

    public static String normalizeAirportCode(String code) {
        if (code == null) {
            return "";
        }

        return code.trim().toUpperCase();
    }

    public static boolean isValidAirportCode(String code) {
        if (code == null) {
            return false;
        }

        return code.matches("[A-Z]{3}");
    }

    public static String generateFlightLabel(
            String flightId, String origin, String destination) {
        return normalizeAirportCode(flightId)
                + ": "
                + normalizeAirportCode(origin)
                + " -> "
                + normalizeAirportCode(destination);
    }

    public static double calculateTicketPrice(
            double basePrice, double taxRate) {
        if (basePrice < 0 || taxRate < 0) {
            throw new IllegalArgumentException(
                    "Price and tax rate cannot be negative");
        }

        return basePrice * (1 + taxRate / 100.0);
    }

    public static String getGreeting(String passengerName) {
        if (passengerName == null
                || passengerName.trim().isEmpty()) {
            return "Welcome, Passenger!";
        }

        return "Welcome aboard, "
                + passengerName.trim()
                + "!";
    }

    public static List<String> filterFlights(
            List<String> flights, String prefix) {
        List<String> result = new ArrayList<>();

        if (flights == null || prefix == null) {
            return result;
        }

        for (String flight : flights) {
            if (flight != null && flight.startsWith(prefix)) {
                result.add(flight);
            }
        }

        return result;
    }

    public static void printSeparator() {
        System.out.println("-----------------------");
    }
}
