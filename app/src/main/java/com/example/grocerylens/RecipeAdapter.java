package com.example.grocerylens;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adapter class for displaying a list of recipes in a RecyclerView.
 * Handles item layout, favorite button actions, and navigation.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViews> {
    private final Context rContext;
    private final List<Recipe> recipeList;
    private final Set<String> favoriteRecipes = new HashSet<>();
    private final DatabaseReference databaseReference;
    private final boolean isFavoritesScreen;

    // constructor
    public RecipeAdapter(Context rContext, List<Recipe> recipeList, boolean isFavoritesScreen) {
        this.rContext = rContext;
        this.recipeList = recipeList;
        this.isFavoritesScreen = isFavoritesScreen;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        this.databaseReference = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId).child("favorites");
    }

    @NonNull
    @Override
    public RecipeViews onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(rContext)
                .inflate(R.layout.activity_recipe_adapter, parent, false);
        return new RecipeViews(view);
    }

    // Binds recipe data to each item view
    @Override
    public void onBindViewHolder(@NonNull RecipeViews holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeTitle.setText(recipe.getTitle());

        // Load recipe image into the image view
        Glide.with(rContext).load(recipe.getImageUrl()).into(holder.recipeImage);

        // Open full recipe page when "view" button is clicked
        holder.btnViewRecipe.setOnClickListener(v -> {
            Intent intent = new Intent(rContext, RecipeDetailsActivity.class);
            intent.putExtra("recipe", recipe);
            rContext.startActivity(intent);
        });

        // Check if this recipe is already a favorite
        updateFavoriteIcon(holder, recipe.getId());

        // Add or remove from favorites when star icon is clicked
        holder.btnFavorite.setOnClickListener(v -> {
            if (favoriteRecipes.contains(recipe.getId())) {
                removeFavorite(recipe.getId());
                holder.btnFavorite.setImageResource(R.drawable.ic_star_border);
                Toast.makeText(rContext, "Removed from favorites", Toast.LENGTH_SHORT).show();
            } else {
                addFavorite(recipe);
                holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
                Toast.makeText(rContext, "Favorite added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adds a recipe to the favorites in Firebase
    private void addFavorite(Recipe recipe) {
        databaseReference.child(recipe.getId()).setValue(recipe);
        favoriteRecipes.add(recipe.getId());
    }

    // Removes a recipe from Firebase favorites
    private void removeFavorite(String recipeId) {
        databaseReference.child(recipeId).removeValue();
        favoriteRecipes.remove(recipeId);

        // If on the favorites screen, also remove it from the current view
        if (isFavoritesScreen) {
            removeItem(recipeId);
        }
    }

    // Checks if a recipe is in the database and updates the star icon accordingly
    private void updateFavoriteIcon(RecipeViews holder, String recipeId) {
        databaseReference.child(recipeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                holder.btnFavorite.setImageResource(R.drawable.ic_star_filled);
                favoriteRecipes.add(recipeId);
            } else {
                holder.btnFavorite.setImageResource(R.drawable.ic_star_border);
            }
        });
    }

    // Removes a recipe from the local list and updates the UI
    private void removeItem(String recipeId) {
        int position = -1;
        for (int i = 0; i < recipeList.size(); i++) {
            if (recipeList.get(i).getId().equals(recipeId)) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            recipeList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recipeList.size());
        }
    }

    // Returns total number of items in the list
    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // ViewHolder class for holding references to each item view
    public static class RecipeViews extends RecyclerView.ViewHolder {
        TextView recipeTitle;
        ImageView recipeImage;
        Button btnViewRecipe;
        ImageButton btnFavorite;

        public RecipeViews(@NonNull View itemView) {
            super(itemView);
            recipeTitle = itemView.findViewById(R.id.recipeTitle);
            recipeImage = itemView.findViewById(R.id.recipeImage);
            btnViewRecipe = itemView.findViewById(R.id.btnViewRecipe);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}
