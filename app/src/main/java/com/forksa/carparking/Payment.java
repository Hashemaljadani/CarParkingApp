package com.forksa.carparking;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Payment extends Fragment {

    private EditText amountInput;
    private EditText cardNumberInput;
    private EditText expiryDateInput;
    private EditText cvvInput;
    private Button payButton;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the payment layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        // Initialize the input fields and button
        amountInput = view.findViewById(R.id.amount_input);
        cardNumberInput = view.findViewById(R.id.card_number_input);
        expiryDateInput = view.findViewById(R.id.expiry_date_input);
        cvvInput = view.findViewById(R.id.cvv_input);
        payButton = view.findViewById(R.id.pay_button);

        // Initialize Firebase reference for the user's data
        String emailKey = Users.getEmail().replace(".", ",");  // Sanitize email to use as Firebase key
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(emailKey);

        // Fetch and display the parking charge from Firebase
        fetchParkingChargeFromFirebase();

        // Handle the Pay button click event
        payButton.setOnClickListener(v -> processPayment(view));

        return view;
    }

    // Fetch the parking charge from Firebase and set it in the amount input field
    private void fetchParkingChargeFromFirebase() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer charges = snapshot.child("parkingCharge").getValue(Integer.class);
                    if (charges != null) {
                        amountInput.setText(String.valueOf(charges));  // Set the amount in the EditText
                    } else {
                        Toast.makeText(getActivity(), "Parking charge not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "No booking found for the user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void processPayment(View view) {
        // Get the input values
        String amount = amountInput.getText().toString();
        String cardNumber = cardNumberInput.getText().toString();
        String expiryDate = expiryDateInput.getText().toString();
        String cvv = cvvInput.getText().toString();

        // Basic validation of input values
        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(getActivity(), "Please enter an amount", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cardNumber) || cardNumber.length() != 16) {
            Toast.makeText(getActivity(), "Please enter a valid card number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(expiryDate)) {
            Toast.makeText(getActivity(), "Please enter the expiry date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(cvv) || cvv.length() != 3) {
            Toast.makeText(getActivity(), "Please enter a valid CVV", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate a payment process
        Toast.makeText(getActivity(), "Payment processed successfully!", Toast.LENGTH_SHORT).show();

        // Update the payment status in Firebase
        updatePaymentStatusInFirebase();

        // Navigate to MyParking fragment and update the navigation bar
        navigateToMyParking(view);
    }

    // Method to update the payment status in Firebase after successful payment
    private void updatePaymentStatusInFirebase() {
        String sanitizedEmail = Users.getEmail().replace(".", ",");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(sanitizedEmail);

        // Set paymentStatus to true
        userRef.child("paymentStatus").setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Payment status updated successfully!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to update payment status.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToMyParking(View view) {
        // Navigate to the MyParking fragment
        MyParking myParkingFragment = new MyParking();

        // Perform the fragment transaction to navigate to MyParking fragment
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, myParkingFragment);  // Replace fragment_container with your container ID
        transaction.addToBackStack(null);  // Allow back navigation
        transaction.commit();

        // Update the navigation bar to highlight "My Parking"
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_my_parking);
        }
    }
}
