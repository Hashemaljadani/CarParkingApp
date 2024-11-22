package com.forksa.carparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class terms_conditions extends AppCompatActivity {


    private CheckBox agreeCheckBox;
    private Button acceptButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions); // Reference to the XML layout

        // Find the CheckBox and Button in the layout
        agreeCheckBox = findViewById(R.id.agreeCheckBox);
        acceptButton = findViewById(R.id.acceptButton);

        // Enable the button only when the CheckBox is checked
        agreeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            acceptButton.setEnabled(isChecked);
        });

        // Set up the acceptButton click listener
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agreeCheckBox.isChecked()) {
                    // Navigate to the next activity or perform another action
                    Intent intent = new Intent(terms_conditions.this, MainActivity.class);
                    startActivity(intent);
                    finish(); // Optionally finish this activity so it can't be returned to
                }
            }
        });
    }
}