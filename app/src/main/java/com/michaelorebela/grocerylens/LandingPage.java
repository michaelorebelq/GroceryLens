
// LandingPage.java
// this class acts as the home screen after login, displaying recipe progress,
// gamification elements like level and badges,
// and access to other app features
// (Adapted from ChatGPT ; firebase, 2024) modified to include custom gamification logic
// (source: https://firebase.google.com/docs/database/android/read-and-write)

package com.example.grocerylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * LandingPage is the main screen after logging in.
 * it displays the user's progress,
 * level, badges, and links to application features.
 */
public class LandingPage extends AppCompatActivity {

    private CircularProgressIndicator progressCircle;
    private TextView progressText, remainingText, welcomeText, userLevelText;
    private ImageView userBadge;
    private DatabaseReference databaseRef;
    private String userId;
    private int recipesViewed = 0;
    private int recipeGoal = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        // ui elements that display progress and user info
        progressCircle = findViewById(R.id.progress_circle);
        progressText = findViewById(R.id.progress_text);
        remainingText = findViewById(R.id.remaining_text);
        welcomeText = findViewById(R.id.welcome_text);
        userLevelText = findViewById(R.id.userLevelText);
        userBadge = findViewById(R.id.userBadge);

        // ui buttons (original)
        Button getScanBtn = findViewById(R.id.get_scanning_button);
        Button viewSavedFavouritesButton = findViewById(R.id.view_saved_favourites_button);
        Button speechBtn = findViewById(R.id.speech_to_recipe);
        ImageButton settingsButton = findViewById(R.id.settings_button);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            userId = auth.getCurrentUser().getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            // calls method to pull recipe progress and goal
            loadUserData();
        }
        // launches about us page
        findViewById(R.id.about_us_button).setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, AboutUsActivity.class);
            startActivity(intent);
        });

        // opens camera to scan ingredients
        getScanBtn.setOnClickListener(v -> startActivity(new Intent(LandingPage.this, CameraActivity.class)));

        // opens saved recipes
        viewSavedFavouritesButton.setOnClickListener(v -> startActivity(new Intent(LandingPage.this, FavouritesActivity.class)));

        // opens ingredient selector manually
        findViewById(R.id.search_skip_btn).setOnClickListener(v -> startActivity(new Intent(LandingPage.this, SelectionActivity.class)));

        // opens speech-based ingredient input
        speechBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, SpeechActivity.class);
            startActivity(intent);
        });

        // opens settings page
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(LandingPage.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
    /**
     * Loading user profile data from Firebase.
     */
    private void loadUserData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // retrieves name and progress data (handles nulls safely)
                    String name = snapshot.child("name").getValue(String.class);
                    recipesViewed = snapshot.child("recipesViewed").getValue(Integer.class) != null ?
                            snapshot.child("recipesViewed").getValue(Integer.class) : 0;
                    recipeGoal = snapshot.child("recipeGoal").getValue(Integer.class) != null ?
                            Math.max(snapshot.child("recipeGoal").getValue(Integer.class), 1) : 1;

                    // updates the circular progress bar + label text
                    updateProgressUI(name);
                    // sets level text and badge icon
                    updateUserLevelAndBadge();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read data", error.toException());
            }
        });
    }
    //(Adapted from ChatGPT) modified by importing progress circle
    //for visual tracking of progress
    // sets up progress bar, welcome message, and recipe remaining count
    @SuppressLint("SetTextI18n")
    private void updateProgressUI(String name) {
        int progress = (recipesViewed * 100) / recipeGoal;
        int remainingRecipes = Math.max(recipeGoal - recipesViewed, 0);

        progressCircle.setProgress(progress);
        progressText.setText(progress + "%");
        progressText.setVisibility(View.VISIBLE);


        // if name exists, include it in welcome message
        if (name != null && !name.isEmpty()) {
            welcomeText.setText(getString(R.string.welcome) + name + getString(R.string.to_grocerylens));
        } else {
            welcomeText.setText(R.string.welcome_to_grocerylens);
        }

        // display the recipes remaining or goal is complete message
        if (remainingRecipes > 0) {
            remainingText.setText(remainingRecipes + getString(R.string.more_recipes_to_hit_your_goal));
        } else {
            remainingText.setText(R.string.you_did_it_goal_reached);
        }
    }

    /**
     * Updates user level and the badge based on
     * number of recipes viewed.
     */
    @SuppressLint("SetTextI18n")
    private void updateUserLevelAndBadge() {
        int userLevel = 1;
        int badgeDrawable = R.drawable.badge_bronze; // default

        if (recipesViewed >= 10) {
            userLevel = 2;
            badgeDrawable = R.drawable.badge_silver;
        }
        if (recipesViewed >= 25) {
            userLevel = 3;
            badgeDrawable = R.drawable.badge_gold;
        }
        if (recipesViewed >= 50) {
            userLevel = 4;
            badgeDrawable = R.drawable.badge_platinum;
        }
        if (recipesViewed >= 100) {
            userLevel = 5;
            badgeDrawable = R.drawable.badge_diamond;
        }

        userLevelText.setText("Level " + userLevel);
        userBadge.setImageResource(badgeDrawable);
        userBadge.setVisibility(View.VISIBLE);
    }
}
