package com.michaelorebela.grocerylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * the class shows a list of recipes using the Spoonacular API.
 *
 */
public class RecipesActivity extends AppCompatActivity {

    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private ProgressBar loadingProgress;
    private TextView noRecipesTxt;
    private SpoonacularAPI spoonacularApi;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        // Set up the Spoonacular API and UI components
        spoonacularApi = new SpoonacularAPI(this);
        RecyclerView recipesRecyclerView = findViewById(R.id.recipesRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        noRecipesTxt = findViewById(R.id.noRecipesText);

        // Set up RecyclerView with vertical scrolling and an empty recipe list
        recipesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(this, recipeList, false);
        recipesRecyclerView.setAdapter(recipeAdapter);

        // Get ingredient and filter data from previous screen
        Intent intent = getIntent();
        List<String> selectedIngredients = intent.getStringArrayListExtra("selectedIngredients");
        List<String> selectedCuisines = intent.getStringArrayListExtra("selectedCuisines");
        String dietaryPreference = intent.getStringExtra("dietaryPreference");

        // In case no filters were passed, default to empty lists
        if (selectedIngredients == null) selectedIngredients = new ArrayList<>();
        if (selectedCuisines == null) selectedCuisines = new ArrayList<>();

        // Call the API to fetch recipes based on selected options
        getRecipes(selectedIngredients, selectedCuisines, dietaryPreference);
    }

    /**
     * Calls the Spoonacular API and updates the UI with recipe results.
     */
    public void getRecipes(List<String> ingredients, List<String> cuisines, String dietaryPreference) {
        loadingProgress.setVisibility(View.VISIBLE);
        noRecipesTxt.setVisibility(View.GONE);

        spoonacularApi.getRecipes(ingredients, cuisines, dietaryPreference, new SpoonacularAPI.SpoonacularCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Recipe> recipes) {
                loadingProgress.setVisibility(View.GONE);
                if (recipes.isEmpty()) {
                    noRecipesTxt.setVisibility(View.VISIBLE);
                } else {
                    recipeList.clear();
                    recipeList.addAll(recipes);
                    recipeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String errorMessage) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(RecipesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("SpoonacularAPI", "Error fetching recipes: " + errorMessage);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, SelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
