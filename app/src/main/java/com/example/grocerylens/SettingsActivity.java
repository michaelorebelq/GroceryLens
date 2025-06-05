package com.example.grocerylens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// settings screen where user can see or update their progress,
// reset recipe stats, or delete their account

public class SettingsActivity extends AppCompatActivity {

    private TextView recipeGoalText, recipesViewedText;
    private EditText editRecipeGoal;
    private DatabaseReference databaseRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get current user and set up Firebase reference
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            databaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            loadUserData(); // pull progress values
        }

        // connect UI elements to layout
        recipeGoalText = findViewById(R.id.recipeGoalText);
        recipesViewedText = findViewById(R.id.recipesViewedText);
        editRecipeGoal = findViewById(R.id.editRecipeGoal);
        Button updateGoalButton = findViewById(R.id.updateGoalButton);
        Button resetRecipesButton = findViewById(R.id.resetRecipesButton);
        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        Button backButton = findViewById(R.id.back_button);

        // update goal value in database
        updateGoalButton.setOnClickListener(v -> {
            String newGoalStr = editRecipeGoal.getText().toString().trim();
            if (!newGoalStr.isEmpty()) {
                int newGoal = Integer.parseInt(newGoalStr);
                databaseRef.child("recipeGoal").setValue(newGoal)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(SettingsActivity.this, "Goal Updated!", Toast.LENGTH_SHORT).show();
                            loadUserData(); // refresh screen
                        })
                        .addOnFailureListener(e -> Log.e("Firebase", "Failed to update", e));
            }
        });

        // reset recipe count to zero
        resetRecipesButton.setOnClickListener(v -> databaseRef.child("recipesViewed").setValue(0)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SettingsActivity.this, "Recipes Viewed Reset!", Toast.LENGTH_SHORT).show();
                    loadUserData();
                })
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to reset recipes viewed", e)));

        // call method to delete account when clicked
        deleteAccountButton.setOnClickListener(v -> deleteAccount());

        // go back to main screen
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, LandingPage.class);
            startActivity(intent);
            finish();
        });
    }

    // pulls user's recipe goal and view count from the database
    private void loadUserData() {
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer recipeGoal = snapshot.child("recipeGoal").getValue(Integer.class);
                    Integer recipesViewed = snapshot.child("recipesViewed").getValue(Integer.class);
                    recipeGoalText.setText(String.valueOf(recipeGoal != null ? recipeGoal : 0));
                    recipesViewedText.setText(String.valueOf(recipesViewed != null ? recipesViewed : 0));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load data", error.toException());
            }
        });
    }

    // prompts user for password, then re authenticates, and deletes their account
    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Account Deletion");
            builder.setMessage("Enter your password to permanently delete your account:");

            final EditText passwordInput = new EditText(this);
            passwordInput.setHint("Password");
            builder.setView(passwordInput);

            builder.setPositiveButton("Delete", (dialog, which) -> {
                String password = passwordInput.getText().toString();

                if (password.isEmpty()) {
                    Toast.makeText(SettingsActivity.this, "Password required", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                // first re-auth, then remove data, then delete user
                user.reauthenticate(credential)
                        .addOnSuccessListener(aVoid -> databaseRef.removeValue().addOnSuccessListener(dbVoid ->
                                user.delete().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SettingsActivity.this, "Account Deleted", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(SettingsActivity.this, LoginPage.class));
                                        finish();
                                    } else {
                                        Toast.makeText(SettingsActivity.this, "Failed to delete account", Toast.LENGTH_SHORT).show();
                                    }
                                })))
                        .addOnFailureListener(e -> {
                            Log.e("ReauthError", "Re-authentication failed: " + e.getMessage());
                            Toast.makeText(SettingsActivity.this, "Incorrect password or session expired", Toast.LENGTH_SHORT).show();
                        });
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        }
    }
}
