package com.forksa.carparking;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class BookedTest {

    private Booked booked;

    @Before
    public void setUp() {
        booked = new Booked();
    }

    // Test for calculateCheckOutTime
    @Test
    public void testCalculateCheckOutTime() {
        // Case 1: Standard case
        String result = booked.calculateCheckOutTime(10, 30, 2);
        assertEquals("12:30", result);

        // Case 2: Over midnight
        result = booked.calculateCheckOutTime(23, 45, 2);
        assertEquals("01:45", result);

        // Case 3: No duration
        result = booked.calculateCheckOutTime(10, 0, 0);
        assertEquals("10:00", result);
    }

    // Test for calculateParkingCharge
    @Test
    public void testCalculateParkingCharge() {
        // Case 1: Regular parking for 2 hours, no extra minutes
        int charge = booked.calculateParkingCharge(10, 0, 2, false);
        assertEquals(20, charge); // 10 * 2

        // Case 2: VIP parking for 2 hours, no extra minutes
        charge = booked.calculateParkingCharge(10, 0, 2, true);
        assertEquals(32, charge); // 16 * 2

        // Case 3: Regular parking with extra minutes
        charge = booked.calculateParkingCharge(10, 30, 2, false);
        assertEquals(30, charge); // 10 * (2 + 1) due to extra minutes

        // Case 4: VIP parking with extra minutes
        charge = booked.calculateParkingCharge(10, 30, 2, true);
        assertEquals(48, charge); // 16 * (2 + 1) due to extra minutes
    }
}
