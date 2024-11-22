package com.forksa.carparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class privacy_policy extends AppCompatActivity {

    private CheckBox agreeCheckBox;
    private Button continueButton;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Find the views in the layout
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        continueButton = findViewById(R.id.continueButton);
        errorTextView = findViewById(R.id.errorTextView);

        // Enable the button only when the CheckBox is checked
        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            continueButton.setEnabled(isChecked);
            if (isChecked) {
                errorTextView.setVisibility(View.GONE); // Hide the error message when checked
            }
        });

        // Set up the continueButton click listener
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agreeCheckBox.isChecked()) {
                    // Navigate to the MainActivity
                    Intent intent = new Intent(privacy_policy.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Optionally close the current activity
                } else {
                    // Show the error message if not checked
                    errorTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
