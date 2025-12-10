package com.example.airline_seat_booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
* The below code emphasizes on booking airline seats concurrently
* Due to proper concurrency control all seats will be booked but will be slower
* See using "SELECT ... FOR UPDATE" to lock the selected rows for the duration of the transaction.
* Observe the output of the program to see the issue
* */
public class AirlineBookingDemo3 {

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
                    throw new RuntimeException(e);
                }
            });

        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        System.out.println("All tasks completed in " + (endTime - startTime) + " ms");
    }


    static Boolean updateSeatsInDb(Integer userId) throws Exception {
        try(Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, seat_number FROM seats " +
                            "WHERE user_id IS NULL AND trip_id=? " +
                            "ORDER BY id " +
                            "LIMIT 1 " +
                            "FOR UPDATE");

            ps.setInt(1, TRIP_ID);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("NO SEATS LEFT for user " + userId);
                return null;   // tell caller to STOP retrying
            }

            int seatId = rs.getInt(1);
            String seatNumber = rs.getString(2);

            ps = conn.prepareStatement(
                    "UPDATE seats SET user_id=? WHERE trip_id=? AND id=? AND user_id IS NULL");

            ps.setInt(1, userId);
            ps.setInt(2, TRIP_ID);
            ps.setInt(3, seatId);

            int updated = ps.executeUpdate();

            if (updated == 1) {
                conn.commit();
                System.out.println("SUCCESS user " + userId + " booked " + seatNumber);
                return true;  // successfully booked
            } else {
                conn.rollback();
                System.out.println("Failed for user " + userId + " trying to book " + seatNumber);
                return false; // race/lost — TRY AGAIN
            }
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
