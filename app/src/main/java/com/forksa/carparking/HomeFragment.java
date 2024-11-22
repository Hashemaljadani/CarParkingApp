package com.forksa.carparking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the button by its ID
        Button exploreNowButton = view.findViewById(R.id.explore_now_button);

        // Set an OnClickListener on the button
        exploreNowButton.setOnClickListener(v -> {
            try {
                navigateToMyParkingFragment(view);
            } catch (Exception e) {
                // Log the exception
                Log.e(TAG, "Error navigating to My Parking fragment: " + e.getMessage(), e);
            }
        });

        return view;
    }

    private void navigateToMyParkingFragment(View view) {
        // Navigate to the "My Parking" fragment
        if (getActivity() != null && getActivity().getSupportFragmentManager() != null) {
            Fragment myParkingFragment = new MyParking();

            // Perform the fragment transaction
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, myParkingFragment)
                    .addToBackStack(null)  // Allow back navigation to this fragment
                    .commit();

            // Update the navigation bar to highlight "My Parking"
            BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setSelectedItemId(R.id.navigation_booked);
            }
        } else {
            Log.e(TAG, "Error: FragmentManager or Activity is null");
        }
    }
}
