package com.example.grocerylens;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * FavouritesActivity displays the user's saved recipes
 * by retrieving data from Firebase Realtime Database.
 */
public class FavouritesActivity extends AppCompatActivity {
    private ProgressBar loadingProgress;
    private TextView noFavoritesText;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> favouriteRecipes;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        RecyclerView favouritesRecyclerView = findViewById(R.id.favouritesRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        noFavoritesText = findViewById(R.id.noFavoritesText);

        favouritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favouriteRecipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, favouriteRecipes, true); // 'true' indicates favourite mode
        favouritesRecyclerView.setAdapter(recipeAdapter);

        // References the current user's saved recipes in Firebase
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("favorites");

        loadFavouriteRecipes();
    }

    /**
     * Saved recipes from Firebase are retrieved updating the view.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadFavouriteRecipes() {
        loadingProgress.setVisibility(View.VISIBLE);
        noFavoritesText.setVisibility(View.GONE);

        databaseReference.get().addOnCompleteListener(task -> {
            loadingProgress.setVisibility(View.GONE);

            if (task.isSuccessful() && task.getResult().exists()) {
                favouriteRecipes.clear();
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        favouriteRecipes.add(recipe);
                    }
                }
                if (favouriteRecipes.isEmpty()) {
                    noFavoritesText.setVisibility(View.VISIBLE);
                }
                recipeAdapter.notifyDataSetChanged();
            } else {
                noFavoritesText.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
