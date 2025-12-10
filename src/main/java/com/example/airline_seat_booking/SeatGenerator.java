package com.example.airline_seat_booking;

import java.util.*;

public class SeatGenerator {

    private final List<String> seats = new ArrayList<>();
    private final Random random = new Random();

    public SeatGenerator() {
        // initialize all 120 seats
        for(int row = 1; row <= 20; row++) {
            for(char col = 'A'; col <= 'F'; col++) {
                seats.add(row + "" + col);
            }
        }
    }

    public String pickRandomSeat() {
        if (seats.isEmpty()) return null; // or throw exception

        int index = random.nextInt(seats.size());
        return seats.remove(index);   // removes permanently → cannot be picked again
    }
}
