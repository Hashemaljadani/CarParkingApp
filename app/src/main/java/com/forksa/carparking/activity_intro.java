package com.forksa.carparking;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;
import java.util.List;

public class activity_intro extends AppCompatActivity {
    // Main class for the intro activity
    // الفئة الرئيسية لنشاط المقدمة

    private List<IntroScreen> screens;
    // List to store the intro screens
    // قائمة لتخزين الشاشات التمهيدية

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        // Initialize ViewPager2, TabLayout, and Skip Button
        // تهيئة ViewPager2 و TabLayout وزر التخطي
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        Button btnSkip = findViewById(R.id.btnSkip);

        // Create intro screens with their text and images
        // إنشاء الشاشات التمهيدية مع النصوص والصور
        screens = Arrays.asList(
                new IntroScreen(R.drawable.screen1, "Welcome to our app!"),
                new IntroScreen(R.drawable.screen2, "Find parking spots easily"),
                new IntroScreen(R.drawable.screen3, "Book in advance"),
                new IntroScreen(R.drawable.screen4, "Secure payment options"),
                new IntroScreen(R.drawable.screen5, "24/7 customer support"),
                new IntroScreen(R.drawable.screen6, "Get started now!")
        );

        // Set up the adapter for the ViewPager
        // إعداد المحول (Adapter) لعرض الشاشات في ViewPager
        IntroAdapter adapter = new IntroAdapter(screens);
        viewPager.setAdapter(adapter);

        // Link TabLayout to ViewPager2
        // ربط TabLayout مع ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Tab customization can be added here
            // يمكن تخصيص التبويبات هنا
        }).attach();

        // Set up Skip button to navigate to MainActivity
        // إعداد زر التخطي للانتقال إلى MainActivity
        btnSkip.setOnClickListener(v -> {
            startActivity(new Intent(activity_intro.this, MainActivity.class));
            finish(); // Close the current activity
            // إنهاء النشاط الحالي
        });
    }

    // Method to expose screens for testing
    // طريقة لإتاحة الوصول إلى الشاشات لأغراض الاختبار
    public List<IntroScreen> getScreens() {
        return screens;
    }

    // Adapter class for managing intro screens
    // الفئة المحولة (Adapter) لإدارة الشاشات التمهيدية
    public static class IntroAdapter extends RecyclerView.Adapter<IntroAdapter.ViewHolder> {
        private final List<IntroScreen> screens; // List of intro screens
        // قائمة بالشاشات التمهيدية

        public IntroAdapter(List<IntroScreen> screens) {
            this.screens = screens;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the layout for each intro screen
            // إنشاء تخطيط لكل شاشة تمهيدية
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_item_intro_screen, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // Bind data to the ViewHolder
            // ربط البيانات بـ ViewHolder
            IntroScreen screen = screens.get(position);
            holder.imageView.setImageResource(screen.imageResId);
            holder.textView.setText(screen.text);
        }

        @Override
        public int getItemCount() {
            return screens.size(); // Return the number of screens
            // إرجاع عدد الشاشات
        }

        // ViewHolder class to manage individual screen elements
        // فئة ViewHolder لإدارة عناصر الشاشة الفردية
        public static class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView; // Image view for the screen
            // عرض الصورة للشاشة
            TextView textView; // Text view for the screen description
            // عرض النص لوصف الشاشة

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
                textView = itemView.findViewById(R.id.textView);
            }
        }
    }

    // Class to represent each intro screen
    // فئة لتمثيل كل شاشة تمهيدية
    public static class IntroScreen {
        int imageResId; // Resource ID for the image
        // معرف المورد للصورة
        String text; // Text description for the screen
        // النص الوصفي للشاشة

        public IntroScreen(int imageResId, String text) {
            this.imageResId = imageResId;
            this.text = text;
        }
    }
}
