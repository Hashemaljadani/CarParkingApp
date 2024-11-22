package com.forksa.carparking;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class OTP extends AppCompatActivity {

    private EditText pinDigit1, pinDigit2, pinDigit3, pinDigit4, pinDigit5, pinDigit6;
    private Button btnVerifyCode;
    private FirebaseAuth auth;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);

        verificationId = getIntent().getStringExtra("verificationId");
        if (verificationId == null) {
            Toast.makeText(this, "Verification ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        auth = FirebaseAuth.getInstance();

        pinDigit1 = findViewById(R.id.pinDigit1);
        pinDigit2 = findViewById(R.id.pinDigit2);
        pinDigit3 = findViewById(R.id.pinDigit3);
        pinDigit4 = findViewById(R.id.pinDigit4);
        pinDigit5 = findViewById(R.id.pinDigit5);
        pinDigit6 = findViewById(R.id.pinDigit6);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupEditTextListeners();

        btnVerifyCode.setOnClickListener(v -> {
            String otp = getOtpFromEditTexts();
            if (TextUtils.isEmpty(otp)) {
                Toast.makeText(OTP.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            signInWithPhoneAuthCredential(credential);
        });
    }

    private void setupEditTextListeners() {
        pinDigit1.addTextChangedListener(new OTPTextWatcher(pinDigit1, pinDigit2));
        pinDigit2.addTextChangedListener(new OTPTextWatcher(pinDigit2, pinDigit3));
        pinDigit3.addTextChangedListener(new OTPTextWatcher(pinDigit3, pinDigit4));
        pinDigit4.addTextChangedListener(new OTPTextWatcher(pinDigit4, pinDigit5));
        pinDigit5.addTextChangedListener(new OTPTextWatcher(pinDigit5, pinDigit6));
    }

    private String getOtpFromEditTexts() {
        return pinDigit1.getText().toString().trim() +
                pinDigit2.getText().toString().trim() +
                pinDigit3.getText().toString().trim() +
                pinDigit4.getText().toString().trim() +
                pinDigit5.getText().toString().trim() +
                pinDigit6.getText().toString().trim();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OTP.this, "Verification successful", Toast.LENGTH_SHORT).show();

                        // Retrieve user details from intent extras
                        String name = getIntent().getStringExtra("name");
                        String phone = getIntent().getStringExtra("phone");
                        String email = getIntent().getStringExtra("email");
                        String password = getIntent().getStringExtra("password");

                        // Save user data to Firebase
                        saveUserToFirebase(name, phone, email, password);

                        // Navigate to Home
                        Intent intent = new Intent(OTP.this, Home.class);
                        startActivity(intent);
                        finish(); // End OTP activity
                    } else {
                        Toast.makeText(OTP.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirebase(String name, String phone, String email, String password) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        String sanitizedEmail = email.replace(".", ",");
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("phone", phone);
        userData.put("email", email);
        userData.put("password", password);

        Users.setCurrentUser(name, email, phone, password);

        databaseReference.child(sanitizedEmail).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(OTP.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OTP.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class OTPTextWatcher implements TextWatcher {
        private EditText currentEditText;
        private EditText nextEditText;

        public OTPTextWatcher(EditText currentEditText, EditText nextEditText) {
            this.currentEditText = currentEditText;
            this.nextEditText = nextEditText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1) {
                nextEditText.requestFocus(); // Move to the next input field
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }
    }
}
