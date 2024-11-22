package com.forksa.carparking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class welcome extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 1000; // 4000 milliseconds = 4 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // تأخير لمدة 4 ثوانٍ قبل الانتقال تلقائيًا إلى IntroActivity
        new Handler().postDelayed(() -> {
            Intent introIntent = new Intent(welcome.this, activity_intro.class);
            startActivity(introIntent);
            finish(); // إنهاء WelcomeActivity بعد الانتقال
        }, SPLASH_DISPLAY_LENGTH);
    }
}
