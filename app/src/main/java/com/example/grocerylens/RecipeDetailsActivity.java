package com.example.grocerylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * full details of a selected recipe are shown.
 * Users can view the image, steps, ingredients and also
 * save or remove it from their favorites.
 */
public class RecipeDetailsActivity extends AppCompatActivity {

    private TextView recipeTitle, recipeInstructions;
    private ImageView recipeImage;
    private Button btnSearchAgain, btnAddFavorite;
    private Recipe recipe;
    private boolean isFavorite = false;
    private DatabaseReference databaseReference;
    private DatabaseReference userDatabaseRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        // Get current user ID and set Firebase DB paths
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
                .child(userId).child("favorites");
        userDatabaseRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Link all UI elements
        recipeTitle = findViewById(R.id.recipeTitle);
        recipeInstructions = findViewById(R.id.recipeInstructions);
        recipeImage = findViewById(R.id.recipeImage);
        btnSearchAgain = findViewById(R.id.btnSearchAgain);
        btnAddFavorite = findViewById(R.id.btnAddFavorite);
        TextView recipeIngredients = findViewById(R.id.recipeIngredientsList);
        TextView recipeDetails = findViewById(R.id.recipeDetails);
        TextView recipeSteps = findViewById(R.id.recipeSteps);

        // Get the recipe object passed from the previous activity
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        // If valid recipe object is passed, show the data
        if (recipe != null) {
            recipeTitle.setText(recipe.getTitle());
            recipeInstructions.setText(recipe.getInstructions());
            Glide.with(this).load(recipe.getImageUrl()).into(recipeImage);

            recipeIngredients.setText("Ingredients:\n" + recipe.getIngredients());

            String detailText = "Time: " + recipe.getReadyInMinutes() + " mins | Servings: " +
                    recipe.getServings() + " | Calories: " + recipe.getCalories() + " kcal";
            recipeDetails.setText(detailText);

            recipeSteps.setText("Steps:\n" + recipe.getInstructions());

            checkIfFavourite();      // check if it's already saved
            incrementRecipeViews();  // increase user's viewed count for progress
        } else {
            Toast.makeText(this, "Error loading recipe!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Go back to home screen
        btnSearchAgain.setOnClickListener(v -> {
            Intent intent = new Intent(RecipeDetailsActivity.this, LandingPage.class);
            startActivity(intent);
            finish();
        });

        // Add or remove recipe from favorites
        btnAddFavorite.setOnClickListener(v -> {
            if (isFavorite) {
                removeFavourite();
            } else {
                addFavourite();
            }
        });
    }

    /**
     * Checks firebase to see if the recipe is already saved as favorite.
     */
    private void checkIfFavourite() {
        databaseReference.child(recipe.getId()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                isFavorite = true;
                btnAddFavorite.setText("Remove from Favorites");
            } else {
                isFavorite = false;
                btnAddFavorite.setText("Add to Favorites");
            }
        });
    }

    /**
     * saves the current recipe to the user's favorites in Firebase.
     */
    @SuppressLint("SetTextI18n")
    private void addFavourite() {
        databaseReference.child(recipe.getId()).setValue(recipe)
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    btnAddFavorite.setText("Remove from Favorites");
                    Toast.makeText(this, "Favorite added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add favorite",
                        Toast.LENGTH_SHORT).show());
    }

    /**
     * Removes the recipe from the user's favorites.
     */
    @SuppressLint("SetTextI18n")
    private void removeFavourite() {
        databaseReference.child(recipe.getId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    isFavorite = false;
                    btnAddFavorite.setText("Add to Favorites");
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove favorite",
                        Toast.LENGTH_SHORT).show());
    }

    /**
     * Increases the number of recipes viewed by the user.
     *  supporting the gamification features on the landing page.
     */
    private void incrementRecipeViews() {
        userDatabaseRef.child("recipesViewed").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Integer currentViews = task.getResult().getValue(Integer.class);
                        int updatedViews = (currentViews != null ? currentViews : 0) + 1;
                        userDatabaseRef.child("recipesViewed").setValue(updatedViews);
                    }
                });
    }
}
