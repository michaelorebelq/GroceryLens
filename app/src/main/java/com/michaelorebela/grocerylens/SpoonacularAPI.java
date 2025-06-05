package com.michaelorebela.grocerylens;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// This class handles interaction with the Spoonacular API to fetch recipes based on filters
public class SpoonacularAPI {

    private static final String API_KEY = "a1e77a617ce1468ab939e989083e3cd5";
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/complexSearch";
    private RequestQueue requestQueue;

    public SpoonacularAPI(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface SpoonacularCallback {
        void onSuccess(List<Recipe> recipes);
        void onError(String errorMessage);
    }

    public void getRecipes(List<String> ingredients, List<String> cuisines, String dietary, final SpoonacularCallback callback) {
        if (ingredients == null || ingredients.isEmpty()) {
            callback.onError("No ingredients selected.");
            return;
        }

        if (cuisines == null) cuisines = new ArrayList<>();

        String ingredientQuery = String.join(",", ingredients);
        String cuisineFilter = cuisines.isEmpty() ? "" : String.join(",", cuisines);

        String url = BASE_URL
                + "?includeIngredients=" + ingredientQuery
                + "&number=10"
                + "&apiKey=" + API_KEY
                + "&addRecipeInformation=true"
                + "&instructionsRequired=true"
                + "&fillIngredients=true"
                + "&addRecipeNutrition=true";

        if (!cuisineFilter.isEmpty()) {
            url += "&cuisine=" + cuisineFilter;
        }

        if (dietary != null && !dietary.isEmpty()) {
            url += "&diet=" + dietary;
        }

        Log.d("SpoonacularAPI", "URL = " + url);

        long startRecipeRequestTime = SystemClock.elapsedRealtime();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    long endRecipeResponseTime = SystemClock.elapsedRealtime();
                    long recipeResponseDuration = endRecipeResponseTime - startRecipeRequestTime;
                    Log.d("RecipeTimeTest", "Spoonacular API response time is " + recipeResponseDuration + " ms");

                    try {
                        List<Recipe> recipes = new ArrayList<>();
                        JSONArray recipesArray = response.getJSONArray("results");

                        for (int i = 0; i < recipesArray.length(); i++) {
                            JSONObject recipeJson = recipesArray.getJSONObject(i);

                            String id = recipeJson.getString("id");
                            String title = recipeJson.getString("title");
                            String imageUrl = recipeJson.getString("image");

                            String instructions = "No instructions available.";
                            if (recipeJson.has("analyzedInstructions") &&
                                    recipeJson.getJSONArray("analyzedInstructions").length() > 0) {
                                JSONArray stepsArray = recipeJson.getJSONArray("analyzedInstructions")
                                        .getJSONObject(0)
                                        .getJSONArray("steps");

                                StringBuilder instructionsBuilder = new StringBuilder();
                                for (int j = 0; j < stepsArray.length(); j++) {
                                    JSONObject step = stepsArray.getJSONObject(j);
                                    instructionsBuilder.append(step.getInt("number"))
                                            .append(". ")
                                            .append(step.getString("step"))
                                            .append("\n\n");
                                }
                                instructions = instructionsBuilder.toString();
                            }

                            StringBuilder ingredientsBuilder = new StringBuilder();
                            if (recipeJson.has("extendedIngredients")) {
                                JSONArray ingredientsArray = recipeJson.getJSONArray("extendedIngredients");
                                for (int j = 0; j < ingredientsArray.length(); j++) {
                                    JSONObject ingredient = ingredientsArray.getJSONObject(j);
                                    ingredientsBuilder.append("• ")
                                            .append(ingredient.getString("original"))
                                            .append("\n");
                                }
                            }
                            String ingredientsStr = ingredientsBuilder.toString();

                            int servings = recipeJson.optInt("servings", 0);
                            int readyInMinutes = recipeJson.optInt("readyInMinutes", 0);

                            double calories = 0.0;
                            if (recipeJson.has("nutrition")) {
                                JSONArray nutrients = recipeJson.getJSONObject("nutrition").optJSONArray("nutrients");
                                if (nutrients != null) {
                                    for (int j = 0; j < nutrients.length(); j++) {
                                        JSONObject nutrient = nutrients.getJSONObject(j);
                                        if ("Calories".equalsIgnoreCase(nutrient.optString("name"))) {
                                            calories = nutrient.optDouble("amount", 0.0);
                                            break;
                                        }
                                    }
                                }
                            }

                            Recipe recipe = new Recipe(id, title, imageUrl, instructions,
                                    servings, readyInMinutes, calories, ingredientsStr);

                            recipes.add(recipe);
                        }

                        callback.onSuccess(recipes);
                    } catch (JSONException e) {
                        callback.onError("Error in extracting recipe data");
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        callback.onError("API issue " + error.networkResponse.statusCode);
                    } else {
                        callback.onError("Check your connection");
                    }
                });

        request.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }
}
