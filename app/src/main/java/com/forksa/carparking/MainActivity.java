package com.forksa.carparking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText;
    private Button signUpButton;
    private TextView termsTextView, privacyPolicyTextView, loginTextView;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase and ProgressDialog
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        auth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        termsTextView = findViewById(R.id.termsTextView);
        privacyPolicyTextView = findViewById(R.id.privacyPolicyTextView);
        loginTextView = findViewById(R.id.loginTextView);
        passwordToggle = findViewById(R.id.passwordToggle);

        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        signUpButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!isValidName(name)) {
                nameEditText.setError("Name must be between 3 and 10 alphabetic characters");
                return;
            }

            if (!isValidEmail(email)) {
                emailEditText.setError("Invalid email address");
                return;
            }

            if (!isValidPassword(password)) {
                passwordEditText.setError("Password must have at least 1 capital letter, 1 small letter, and 1 number");
                return;
            }

            sendOtpByEmail(email, name, password);
        });

        termsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, terms_conditions.class);
            startActivity(intent);
        });

        privacyPolicyTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, privacy_policy.class);
            startActivity(intent);
        });

        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });
    }

    private void sendOtpByEmail(String email, String name, String password) {
        progressDialog.show(); // عرض ProgressDialog

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                progressDialog.dismiss(); // إخفاء ProgressDialog
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                    saveUserToFirebase(name, email, password);

                                    // الانتقال إلى شاشة تسجيل الدخول بعد التسجيل الناجح
                                    Intent intent = new Intent(MainActivity.this, Login.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(MainActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        progressDialog.dismiss(); // إخفاء ProgressDialog
                        Toast.makeText(MainActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirebase(String name, String email, String password) {
        String sanitizedEmail = email.replace(".", ",");
        Map<String, String> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("password", password);

        Users.setCurrentUser(name, email, "", password);

        databaseReference.child(sanitizedEmail).setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Failed to register user", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidName(String name) {
        // التحقق من الأحرف الأبجدية فقط وطول الاسم
        return name != null && name.matches("[a-zA-Z]{3,10}");
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6) {
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}
