package com.forksa.carparking;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MyParking extends Fragment {

    private TextView parkingInfo, parkingSpace, uniqueId, checkInTime, checkOutTime, directionsLink;
    private ImageView qrCodeImage;
    private Button backToHomeButton;
    private DatabaseReference bookingsRef;
    private Handler handler = new Handler();
    private Runnable qrCodeRemover;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_parking, container, false);

        // Initialize views
        parkingInfo = view.findViewById(R.id.parking_info);
        parkingSpace = view.findViewById(R.id.parking_space);
        uniqueId = view.findViewById(R.id.unique_id);
        checkInTime = view.findViewById(R.id.check_in_time);
        checkOutTime = view.findViewById(R.id.check_out_time);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        backToHomeButton = view.findViewById(R.id.back_to_home_button);

        // Firebase reference for bookings
        bookingsRef = FirebaseDatabase.getInstance().getReference("Users");

        // Fetch parking info from Firebase
        String email = Users.getEmail();
        if (email != null) {
            fetchParkingDetails(email);
        }



        // Back button listener to move to HomeFragment
        backToHomeButton.setOnClickListener(v -> {
            Fragment homeFragment = new HomeFragment(); // Create an instance of HomeFragment
            FragmentManager fragmentManager = getFragmentManager(); // Get the FragmentManager for interacting with fragments associated with this activity
            if (fragmentManager != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, homeFragment); // 'fragment_container' should be the ID of the container in the activity layout
                transaction.addToBackStack(null); // This line is optional, depending on whether you want back navigation
                transaction.commit(); // Commit the transaction
            }
        });

        return view;
    }


    // Fetch parking details from Firebase using the user's email
    private void fetchParkingDetails(String email) {
        String sanitizedEmail = email.replace(".", ",");
        DatabaseReference userRef = bookingsRef.child(sanitizedEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Check if the payment was successful
                    Boolean paymentStatus = snapshot.child("paymentStatus").getValue(Boolean.class);

                    if (paymentStatus != null && paymentStatus) {
                        // Payment successful, show parking details and QR code
                        String parkingSpot = snapshot.child("parkingSpot").getValue(String.class);
                        String location = snapshot.child("location").getValue(String.class);
                        String checkIn = snapshot.child("checkInTime").getValue(String.class);
                        String checkOut = snapshot.child("checkOutTime").getValue(String.class);
                        Integer duration = snapshot.child("duration").getValue(Integer.class);

                        // Populate the UI with booking details
                        parkingInfo.setText(location);
                        parkingSpace.setText(parkingSpot);
                        checkInTime.setText(checkIn);
                        checkOutTime.setText(checkOut);
                        uniqueId.setText("Unique ID: " + Users.getName());

                        // Generate QR code for the parking details
                        generateQRCode(location, checkIn, checkOut);

                        // Schedule QR code removal after duration ends
                        if (duration != null) {
                            scheduleQRCodeRemoval(duration);
                        }
                    } else {
                        // Payment not successful or not found
                        Toast.makeText(getContext(), "Payment not completed. Please complete the payment first.", Toast.LENGTH_LONG).show();
                        qrCodeImage.setVisibility(View.GONE);
                        uniqueId.setVisibility(View.GONE);
                        parkingInfo.setText("No active booking found.");
                    }
                } else {
                    // No booking found for this user
                    parkingInfo.setText("No parking information found.");
                    qrCodeImage.setVisibility(View.GONE);
                    uniqueId.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                parkingInfo.setText("Error fetching parking information.");
                qrCodeImage.setVisibility(View.GONE);
                uniqueId.setVisibility(View.GONE);
            }
        });
    }

    // Generate QR Code using parking information
    private void generateQRCode(String location, String checkInTime, String checkOutTime) {
        String qrContent = "Location: " + location + "\nCheck-in: " + checkInTime + "\nCheck-out: " + checkOutTime;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 400, 400);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    // Schedule QR code removal after the booking duration ends
    private void scheduleQRCodeRemoval(int duration) {
        long delayMillis = duration * 60 * 60 * 1000; // Convert hours to milliseconds

        // Remove QR code after the specified duration
        qrCodeRemover = () -> {
            qrCodeImage.setImageBitmap(null);
            uniqueId.setText("QR code expired");
        };

        handler.postDelayed(qrCodeRemover, delayMillis);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the QR code removal task if the fragment is destroyed
        if (qrCodeRemover != null) {
            handler.removeCallbacks(qrCodeRemover);
        }
    }
    }
