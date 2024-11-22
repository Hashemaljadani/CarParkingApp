package com.forksa.carparking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    private Button loginButton, sendEmailVerificationButton;
    private TextView createAccountTextView;
    private EditText emailEditText, passwordEditText;
    private ImageView passwordToggle;
    private CheckBox rememberMeCheckBox;
    private boolean isPasswordVisible = false;

    private FirebaseAuth auth;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // إعداد Firebase و SharedPreferences
        auth = FirebaseAuth.getInstance();
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // العثور على المشاهدات باستخدام ID
        loginButton = findViewById(R.id.loginButton);
        sendEmailVerificationButton = findViewById(R.id.sendEmailVerificationButton);
        createAccountTextView = findViewById(R.id.createAccountTextView);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        passwordToggle = findViewById(R.id.passwordToggle);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);

        // تحميل بيانات تسجيل الدخول المحفوظة إذا كانت خاصية "تذكرني" مفعلة
        loadLoginDetails();

        // تفعيل خاصية إظهار أو إخفاء كلمة المرور
        passwordToggle.setOnClickListener(view -> {
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

        // الاستماع لأحداث الضغط على زر تسجيل الدخول
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // التحقق من صحة البيانات المدخلة
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.setError("Please enter a valid email");
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }

            // تنفيذ عملية تسجيل الدخول
            loginUser(email, password);
        });

        // إرسال بريد التفعيل
        sendEmailVerificationButton.setOnClickListener(v -> sendEmailVerification());

        // الانتقال إلى التسجيل
        createAccountTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            String emailKey = email.replace(".", ",");
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(emailKey);

                            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        String dbPassword = snapshot.child("password").getValue(String.class);
                                        if (dbPassword != null && dbPassword.equals(password)) {
                                            String name = snapshot.child("name").getValue(String.class);

                                            // حفظ تفاصيل تسجيل الدخول إذا كانت خاصية "تذكرني" مفعلة
                                            if (rememberMeCheckBox.isChecked()) {
                                                saveLoginDetails(email, password);
                                            }

                                            // تعيين المستخدم الحالي كجلسة محلية
                                            Users.setCurrentUser(name, email, "", dbPassword);
                                            Users.setLoggedIn(true);

                                            // الانتقال إلى الصفحة الرئيسية
                                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Login.this, Home.class));
                                            finish();
                                        } else {
                                            Toast.makeText(Login.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(Login.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(Login.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(Login.this, "Please verify your email before logging in", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Login.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLoginDetails(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    private void loadLoginDetails() {
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);
        if (isRemembered) {
            String savedEmail = sharedPreferences.getString("email", "");
            String savedPassword = sharedPreferences.getString("password", "");
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
    }
}
