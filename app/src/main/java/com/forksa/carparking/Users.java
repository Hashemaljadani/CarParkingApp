package com.forksa.carparking;

public class Users {
    private static String name;
    private static String email;
    private static String phone;
    private static String password;
    private static String checkInTime;
    private static String checkOutTime;
    private static int duration;
    private static String parkingLocation;
    private static String parkingSpot; // Added parking spot field
    private static int charges; // Added charges field
    private static boolean isLoggedIn; // Add boolean field to track login or registration

    // Static methods to set and get user data from Register page
    public static void setCurrentUser(String name, String email, String phone, String password) {
        Users.name = name;
        Users.email = email;
        Users.phone = phone;
        Users.password = password;
    }

    // Set additional booking data from Booked class
    public static void setBookingInfo(String checkInTime, String checkOutTime, int duration, String parkingLocation) {
        Users.checkInTime = checkInTime;
        Users.checkOutTime = checkOutTime;
        Users.duration = duration;
        Users.parkingLocation = parkingLocation;
    }

    // Set the parking spot and charges for the booking
    public static void setParkingDetails(String parkingSpot, int charges) {
        Users.parkingSpot = parkingSpot;
        Users.charges = charges;
    }

    // Getters for user details
    public static String getName() {
        return name;
    }

    public static String getEmail() {
        return email;
    }

    public static String getPhone() {
        return phone;
    }

    public static String getPassword() {
        return password;
    }

    // Getters for booking information
    public static String getCheckInTime() {
        return checkInTime;
    }

    public static String getCheckOutTime() {
        return checkOutTime;
    }

    public static int getDuration() {
        return duration;
    }

    public static String getParkingLocation() {
        return parkingLocation;
    }

    // Getters for parking spot and charges
    public static String getParkingSpot() {
        return parkingSpot;
    }

    public static int getCharges() {
        return charges;
    }

    // Getter and Setter for the login status
    public static boolean isLoggedIn() {
        return isLoggedIn;
    }

    public static void setLoggedIn(boolean loggedIn) {
        Users.isLoggedIn = loggedIn;
    }
}
