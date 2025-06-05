package com.example.grocerylens;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class NLP {

    // the method goes through the JSON Array from the NLP API
    // and turns it into a list of strings we can use
    public static List<String> getIngredientsFromJson(JSONArray foodItemsArray) {
        List<String> ingredients = new ArrayList<>();
        try {
            for (int i = 0; i < foodItemsArray.length(); i++) {
                // add each item to the list
                ingredients.add(foodItemsArray.getString(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ingredients;
    }
}
