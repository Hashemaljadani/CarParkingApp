package com.forksa.carparking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
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

public class fragment_reservation_details extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // إنشاء تخطيط واجهة المستخدم لهذا المقطع
        View view = inflater.inflate(R.layout.fragment_reservation_details, container, false);

        // Find the views by their ID
        // العثور على عناصر واجهة المستخدم باستخدام المعرف
        TextView arrivalTimeText = view.findViewById(R.id.arrival_time_text);
        TextView checkoutTimeText = view.findViewById(R.id.checkoutText);
        TextView parkingDurationText = view.findViewById(R.id.parking_duration_text);
        TextView parkingSpotText = view.findViewById(R.id.parking_spot_text);
        TextView locationText = view.findViewById(R.id.location_text);
        TextView chargesText = view.findViewById(R.id.charges_text);
        Button reserveButton = view.findViewById(R.id.reserve_button); // Initialize the reserve button
        // تهيئة زر الحجز

        // Fetch and display booking details from Firebase
        // جلب وعرض تفاصيل الحجز من Firebase
        fetchBookingDetailsFromFirebase(arrivalTimeText, checkoutTimeText, parkingDurationText, parkingSpotText, locationText, chargesText, reserveButton);

        // Set up the reserve button to navigate to the PaymentFragment
        // إعداد زر الحجز للانتقال إلى Fragment الدفع
        reserveButton.setOnClickListener(v -> navigateToPaymentFragment());

        return view; // Return the created view
        // إعادة العرض الذي تم إنشاؤه
    }

    // Helper method to fetch booking details from Firebase
    // طريقة مساعدة لجلب تفاصيل الحجز من Firebase
    private void fetchBookingDetailsFromFirebase(TextView arrivalTimeText, TextView checkoutTimeText, TextView parkingDurationText, TextView parkingSpotText, TextView locationText, TextView chargesText, Button reserveButton) {
        // Replace periods in email with commas to match Firebase key format
        // استبدال النقاط في البريد الإلكتروني بالفواصل لتطابق صيغة المفتاح في Firebase
        String emailKey = Users.getEmail().replace(".", ",");

        // Reference the user's specific data based on their email
        // الإشارة إلى بيانات المستخدم بناءً على بريده الإلكتروني
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(emailKey);

        // Fetch the user data
        // جلب بيانات المستخدم
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch booking info
                    // جلب معلومات الحجز
                    String checkInTime = snapshot.child("checkInTime").getValue(String.class);
                    String checkOutTime = snapshot.child("checkOutTime").getValue(String.class);
                    String location = snapshot.child("location").getValue(String.class);
                    String parkingSpot = snapshot.child("parkingSpot").getValue(String.class);
                    Integer duration = snapshot.child("duration").getValue(Integer.class);
                    Integer charges = snapshot.child("parkingCharge").getValue(Integer.class);

                    // Set TextViews with the fetched data
                    // ضبط عناصر TextView بالبيانات المسترجعة
                    arrivalTimeText.setText(checkInTime != null ? checkInTime : "N/A");
                    checkoutTimeText.setText(checkOutTime != null ? checkOutTime : "N/A");
                    parkingDurationText.setText(duration != null ? (duration + (duration > 1 ? " hours" : " hour")) : "N/A");
                    parkingSpotText.setText(parkingSpot != null ? parkingSpot : "N/A");
                    locationText.setText(location != null ? location : "N/A");
                    chargesText.setText(charges != null ? charges + " SR" : "N/A");

                    // Update the button text with the charges
                    // تحديث نص الزر بتكلفة الحجز
                    reserveButton.setText("Reserve for " + (charges != null ? charges : "0") + " SR");
                } else {
                    Log.e("FirebaseError", "No booking found for the user.");
                    // تسجيل خطأ عند عدم وجود بيانات للمستخدم
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching booking details: " + error.getMessage());
                // تسجيل الخطأ عند فشل جلب البيانات
            }
        });
    }

    // Navigate to the PaymentFragment
    // الانتقال إلى Fragment الدفع
    private void navigateToPaymentFragment() {
        // Create an instance of the Payment fragment
        // إنشاء نسخة من Fragment الدفع
        Fragment paymentFragment = new Payment();

        // Perform the fragment transaction
        // تنفيذ عملية استبدال المقطع
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, paymentFragment);
        fragmentTransaction.addToBackStack(null); // Allow back navigation
        // السماح بالرجوع للخلف
        fragmentTransaction.commit();
    }
}
