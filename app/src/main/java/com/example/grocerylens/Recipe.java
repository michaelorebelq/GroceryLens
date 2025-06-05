package com.example.grocerylens;

import java.io.Serializable;
import java.util.UUID;

/**
 * Model class for holding recipe data.
 * This class stores information returned from the Spoonacular API,
 * and is also used for saving recipes in Firebase.
 */
public class Recipe implements Serializable {

    private String id;
    private String title;
    private String imageUrl;
    private String instructions;
    private int servings;
    private int readyInMinutes;
    private double calories;
    private String ingredients; // stores ingredients as a comma-separated string

    /**
     * Default constructor for Firebase data mapping.
     */
    public Recipe() {}

    /**
     * Constructor for creating a recipe object with all fields.
     * if there is no ID is provided, it auto-generates one using a UUID.
     *
     * @param id             recipe ID (optional)
     * @param title          name of the recipe
     * @param imageUrl       image link for the recipe
     * @param instructions   preparation steps
     * @param servings       number of servings
     * @param readyInMinutes time needed to prepare
     * @param calories       estimated calories
     * @param ingredients    ingredients as a comma-separated string
     */
    public Recipe(String id, String title, String imageUrl, String instructions,
                  int servings, int readyInMinutes, double calories, String ingredients) {
        this.id = (id != null) ? id : generateId();
        this.title = title;
        this.imageUrl = imageUrl;
        this.instructions = instructions;
        this.servings = servings;
        this.readyInMinutes = readyInMinutes;
        this.calories = calories;
        this.ingredients = ingredients;
    }

    /**
     * generation of a unique ID if none is provided.
     *
     * @return a UUID string
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    // Getters to access each field

    public String getId() { return id; }

    public String getTitle() { return title; }

    public String getImageUrl() { return imageUrl; }

    public String getInstructions() { return instructions; }

    public int getServings() { return servings; }

    public int getReadyInMinutes() { return readyInMinutes; }

    public double getCalories() { return calories; }

    public String getIngredients() { return ingredients; }
}
