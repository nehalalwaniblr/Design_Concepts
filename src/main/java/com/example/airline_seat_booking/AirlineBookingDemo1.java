package com.example.airline_seat_booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
* The below code emphasizes on booking airline seats concurrently
* Due to lack of proper concurrency control the same seat may be booked by multiple users
* Observe the output of the program to see the issue
* */
public class AirlineBookingDemo1 {

    // DB Config
    static final String URL = "jdbc:mysql://localhost:3306/airline_system?useSSL=false";
    static final String USER = "root";
    static final String PASSWORD = "";
    static List<Integer> allUsers = new ArrayList<>();
    static Integer TRIP_ID = 1;
    static Connection conn;

    static {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            getAllUsersFromDb(TRIP_ID);
            resetUsersInDb(TRIP_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void resetUsersInDb(Integer tripId) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE seats SET user_id=NULL WHERE trip_id=?");
            ps.setInt(1, tripId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getAllUsersFromDb(int tripId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM users");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            allUsers.add(rs.getInt(1));
        }
    }

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(120);

        // simulate 120 concurrent users
        for (Integer userId : allUsers) {
            executor.submit(() -> {
                try {
                    updateSeatsInDb(userId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        System.out.println("All tasks completed in " + (endTime - startTime) + " ms");

    }

    static void updateSeatsInDb(Integer userId) throws Exception {
        conn.setAutoCommit(false);
        try {
            // try to get one random seat
            PreparedStatement ps = conn.prepareStatement("Select id, seat_number from seats where user_id IS NULL and trip_id = ? LIMIT 1");
            ps.setInt(1, TRIP_ID);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("No available seats for user " + userId);
                return; // no available seats
            }
            int seatId = rs.getInt(1);
            String seatNumber = rs.getString(2);


            ps = conn.prepareStatement("UPDATE seats SET user_id=? WHERE trip_id=? AND id=?" +
//                         " AND user_id IS NULL" +  // adding this condition will threads that have staled read the data will be rollbacked; an optimistic concurrency control mechanism.
                    "");
            ps.setInt(1, userId);
            ps.setInt(2, TRIP_ID);
            ps.setInt(3, seatId);
            int updated = ps.executeUpdate();

            if (updated == 1) {
                conn.commit();
                System.out.println("SUCCESS user " + userId + " booked " + seatNumber);
            } else {
                conn.rollback();
                System.out.println("FAILED user " + userId + " could NOT book " + seatNumber);
            }
        } catch (SQLException e) {
            conn.rollback();
        }

    }

    static void printResult(int tripId, String seat) throws Exception {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            PreparedStatement ps = conn.prepareStatement("SELECT s.seat_number, u.name  " + "FROM seats s LEFT JOIN users u ON s.user_id=u.id " + "WHERE trip_id=? AND seat_number=?");

            ps.setInt(1, tripId);
            ps.setString(2, seat);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("Seat " + rs.getString(1) + " booked by: " + rs.getString(2));
            } else {
                System.out.println("seat record not found.");
            }
        }
    }
}
