package com.forksa.carparking;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.AdapterView;
import java.util.HashMap;
import java.util.Map;

public class Booked extends Fragment {

    // Firebase database reference
    // مرجع قاعدة بيانات Firebase
    private DatabaseReference databaseReference;

    // Currently selected parking spot view and ID
    // العرض والمعرف الخاص بالموقف المختار
    private View selectedSpot = null;
    private String selectedParkingSpot = null;

    // UI elements: TextView, TimePicker, Spinners, and Buttons
    // عناصر واجهة المستخدم: TextView و TimePicker و Spinners و Buttons
    private TextView chargeTextView;
    private TimePicker timePicker;
    private Spinner hourSpinner;
    private Spinner locationSpinner;
    private Button addParkingSpotButton;

    // Parking spot views
    // عناصر العرض الخاصة بمواقف السيارات
    private View parkingSpot1, parkingSpot2, parkingSpot3, parkingSpot4, parkingSpot5, parkingSpot6;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        // إنشاء تخطيط الواجهة الخاص بالمقطع
        View view = inflater.inflate(R.layout.fragment_booked, container, false);

        // Initialize Firebase reference
        // تهيئة مرجع Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
// Call the method to check payment status and navigate
        checkPaymentStatusAndNavigate();

        // Initialize TimePicker and set to 24-hour format
        // تهيئة TimePicker وضبطه للعمل بنظام 24 ساعة
        timePicker = view.findViewById(R.id.time_picker);
        timePicker.setIs24HourView(true);

        // Initialize Spinner for selecting parking duration
        // تهيئة Spinner لاختيار مدة الوقوف
        hourSpinner = view.findViewById(R.id.hour_spinner);
        ArrayAdapter<Integer> hourAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        for (int i = 1; i <= 24; i++) {
            hourAdapter.add(i); // Add hours from 1 to 24
            // إضافة الساعات من 1 إلى 24
        }
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hourSpinner.setAdapter(hourAdapter);

        // Initialize Spinner for selecting location
        // تهيئة Spinner لاختيار الموقع
        locationSpinner = view.findViewById(R.id.location_spinner);
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.parking_locations,
                android.R.layout.simple_spinner_item
        );
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        // Set listener for location selection
        // إعداد مستمع لتغيير الموقع المختار
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected location from the spinner
                // الحصول على الموقع المختار من Spinner
                String selectedLocation = locationSpinner.getSelectedItem().toString();
                Log.d("LocationSelected", "Selected location: " + selectedLocation);

                // Update parking spot availability based on location
                // تحديث توفر مواقف السيارات بناءً على الموقع المختار
                checkBookedSpots(parkingSpot1, "C01", selectedLocation);
                checkBookedSpots(parkingSpot2, "C02", selectedLocation);
                checkBookedSpots(parkingSpot3, "C03", selectedLocation);
                checkBookedSpots(parkingSpot4, "C04", selectedLocation);
                checkBookedSpots(parkingSpot5, "VIP-C05", selectedLocation);
                checkBookedSpots(parkingSpot6, "VIP-C06", selectedLocation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Log message when no selection is made
                // تسجيل رسالة عند عدم اختيار أي موقع
                Log.d("LocationSelected", "Nothing selected");
            }
        });

        // Initialize parking spot views
        // تهيئة عناصر العرض الخاصة بمواقف السيارات
        parkingSpot1 = view.findViewById(R.id.parking_spot_1);
        parkingSpot2 = view.findViewById(R.id.parking_spot_2);
        parkingSpot3 = view.findViewById(R.id.parking_spot_3);
        parkingSpot4 = view.findViewById(R.id.parking_spot_4);
        parkingSpot5 = view.findViewById(R.id.parking_spot_5);
        parkingSpot6 = view.findViewById(R.id.parking_spot_6);

        // Set OnClickListeners for parking spots to handle selection
        // إعداد المستمعات لكل موقف لتحديده عند النقر عليه
        parkingSpot1.setOnClickListener(spotClickListener);
        parkingSpot2.setOnClickListener(spotClickListener);
        parkingSpot3.setOnClickListener(spotClickListener);
        parkingSpot4.setOnClickListener(spotClickListener);
        parkingSpot5.setOnClickListener(spotClickListener);
        parkingSpot6.setOnClickListener(spotClickListener);

        // Initialize TextView to display the calculated parking charge
        // تهيئة TextView لعرض تكلفة الوقوف المحسوبة
        chargeTextView = view.findViewById(R.id.charges_edit_text);

        // Initialize the "Add Parking Spot" button and its click listener
        // تهيئة زر "إضافة موقف سيارة" ومستمع النقر الخاص به
        addParkingSpotButton = view.findViewById(R.id.add_parking_spot_button);
        addParkingSpotButton.setOnClickListener(v -> {
            if (selectedParkingSpot == null) {
                Toast.makeText(getActivity(), "Please select a parking spot", Toast.LENGTH_SHORT).show();
                // عرض رسالة عند عدم اختيار موقف
                return;
            }

            // Retrieve booking details from UI components
            // استرجاع تفاصيل الحجز من عناصر الواجهة
            String location = locationSpinner.getSelectedItem().toString();
            String checkInTime = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
            int duration = (Integer) hourSpinner.getSelectedItem();
            String checkOutTime = calculateCheckOutTime(timePicker.getHour(), timePicker.getMinute(), duration);
            int parkingCharge = calculateParkingCharge(timePicker.getHour(), timePicker.getMinute(), duration, selectedParkingSpot.startsWith("VIP"));
            String email = Users.getEmail();

            // Send booking data to Firebase and navigate to reservation details
            // إرسال بيانات الحجز إلى Firebase والانتقال إلى تفاصيل الحجز
            sendBookingDataToFirebase(email, location, checkInTime, checkOutTime, duration, selectedParkingSpot, parkingCharge);

            fragment_reservation_details reservationDetailsFragment = new fragment_reservation_details();
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, reservationDetailsFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

        return view; // Return the inflated view
        // إعادة تخطيط الواجهة إلى المستخدم
    }

    // Check if a specific parking spot is already booked
    // التحقق إذا ما كان موقف معين محجوزاً مسبقاً
    private void checkBookedSpots(View spotView, String spotLabel, String location) {
        String locationParkingSpot = location + "_" + spotLabel;

        databaseReference.orderByChild("location_parkingSpot")
                .equalTo(locationParkingSpot)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            spotView.setBackgroundColor(Color.RED); // Mark as booked
                            // وضع اللون الأحمر كدلالة على الحجز
                            spotView.setEnabled(false);
                        } else {
                            spotView.setBackgroundColor(Color.GREEN); // Mark as available
                            // وضع اللون الأخضر كدلالة على التوفر
                            spotView.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("Booked", "Error checking booked spots: " + databaseError.getMessage());
                        // تسجيل الخطأ عند فشل التحقق من الحجز
                    }
                });
    }

    // Calculate the check-out time based on check-in and duration
    // حساب وقت الخروج بناءً على وقت الدخول والمدة
    protected String calculateCheckOutTime(int checkInHour, int checkInMinute, int durationHours) {
        int checkOutHour = checkInHour + durationHours;
        int checkOutMinute = checkInMinute;

        if (checkOutHour >= 24) {
            checkOutHour = checkOutHour % 24; // Wrap around if past midnight
            // إعادة ضبط الساعة إذا تجاوزت منتصف الليل
        }

        return String.format("%02d:%02d", checkOutHour, checkOutMinute);
    }

    // Calculate the parking charge based on duration and VIP status
    // حساب تكلفة الوقوف بناءً على المدة ونوع الموقف (VIP)
    protected int calculateParkingCharge(int checkInHour, int checkInMinute, int duration, boolean isVIP) {
        int chargePerHour = isVIP ? 16 : 10; // Higher rate for VIP spots
        // تحديد السعر بناءً على نوع الموقف
        int totalHours = duration;
        if (checkInMinute > 0) {
            totalHours += 1; // Round up to the next hour if minutes are non-zero
            // تقريب الساعة في حالة وجود دقائق
        }

        return totalHours * chargePerHour;
    }

    // Send booking data to Firebase
    // إرسال بيانات الحجز إلى Firebase
    private void sendBookingDataToFirebase(String email, String location, String checkInTime, String checkOutTime, int duration, String parkingSpot, int parkingCharge) {
        String sanitizedEmail = email.replace(".", ","); // Replace dots to make it a valid Firebase key
        // استبدال النقاط في البريد الإلكتروني لجعله صالحاً في Firebase
        String locationParkingSpot = location + "_" + parkingSpot;

        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("location", location);
        bookingData.put("checkInTime", checkInTime);
        bookingData.put("checkOutTime", checkOutTime);
        bookingData.put("duration", duration);
        bookingData.put("parkingSpot", parkingSpot);
        bookingData.put("location_parkingSpot", locationParkingSpot);
        bookingData.put("parkingCharge", parkingCharge);

        databaseReference.child(sanitizedEmail).updateChildren(bookingData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Booked", "Booking info updated in Firebase");
                        Toast.makeText(getContext(), "Booking Info Updated in Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Booked", "Failed to update booking info in Firebase");
                        Toast.makeText(getContext(), "Failed to Update Booking Info in Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // OnClickListener for parking spot selection
    // مستمع النقر لاختيار موقف السيارة
    private final View.OnClickListener spotClickListener = v -> {
        if (selectedSpot != null) {
            selectedSpot.setBackgroundColor(Color.GREEN); // Reset previously selected spot
            // إعادة تعيين اللون للموقف السابق
        }

        selectedSpot = v;
        selectedSpot.setBackgroundColor(Color.BLUE); // Highlight the selected spot
        // تمييز الموقف المختار باللون الأزرق

        // Map parking spot view to its label
        // ربط العرض بمعرف الموقف
        if (v.getId() == R.id.parking_spot_1) {
            selectedParkingSpot = "C01";
        } else if (v.getId() == R.id.parking_spot_2) {
            selectedParkingSpot = "C02";
        } else if (v.getId() == R.id.parking_spot_3) {
            selectedParkingSpot = "C03";
        } else if (v.getId() == R.id.parking_spot_4) {
            selectedParkingSpot = "C04";
        } else if (v.getId() == R.id.parking_spot_5) {
            selectedParkingSpot = "VIP-C05";
        } else if (v.getId() == R.id.parking_spot_6) {
            selectedParkingSpot = "VIP-C06";
        }

        if (selectedParkingSpot != null && hourSpinner.getSelectedItem() != null) {
            int duration = (Integer) hourSpinner.getSelectedItem();
            int checkInHour = timePicker.getHour();
            int checkInMinute = timePicker.getMinute();

            // Calculate the parking charge and update the TextView
            // حساب تكلفة الوقوف وتحديث TextView
            int parkingCharge = calculateParkingCharge(checkInHour, checkInMinute, duration, selectedParkingSpot.startsWith("VIP"));
            chargeTextView.setText("Total Charge: " + parkingCharge + " Riyals");
        }
    };
    // Method to check if the user has an active booking and navigate accordingly
    private void checkPaymentStatusAndNavigate() {
        String email = Users.getEmail();
        if (email == null || email.isEmpty()) {
            Log.e("Booked", "Email is null or empty. Cannot check payment status.");
            return;
        }

        String sanitizedEmail = email.replace(".", ",");

        databaseReference.child(sanitizedEmail).child("paymentStatus")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                            requireActivity().runOnUiThread(() -> {
                                // Show a Toast informing the user about the active booking
                                Toast.makeText(requireContext(), "You already have an active booking. Redirecting to My Parking.", Toast.LENGTH_SHORT).show();

                                // Navigate to MyParking fragment
                                Fragment myParkingFragment = new MyParking();
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.fragment_container, myParkingFragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            });
                        } else {
                            // If no active booking, you can handle this case or leave it as is
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "No active bookings found.", Toast.LENGTH_SHORT).show()
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Booked", "Error checking payment status: " + error.getMessage());
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "Failed to check payment status. Please try again.", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }



}
