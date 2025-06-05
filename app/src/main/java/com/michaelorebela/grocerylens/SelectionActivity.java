package com.michaelorebela.grocerylens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

// this screen lets the user review detected ingredients,
// search for new ones, apply filters, and generate recipes

public class SelectionActivity extends AppCompatActivity {

    private Button searchBtn, generateBtn;
    private ListView detectedIngredientsListView;
    private RadioGroup dietaryGroup;
    private List<CheckBox> cuisineBoxes;
    private List<String> detectedList;
    private List<String> selectedIngredients;
    private DatabaseReference ingredientsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        // connect UI buttons and views
        searchBtn = findViewById(R.id.searchAddIngredientsButton);
        generateBtn = findViewById(R.id.generateRecipesButton);
        detectedIngredientsListView = findViewById(R.id.detectedIngredientsListView);
        dietaryGroup = findViewById(R.id.dietary_group);

        // get reference to Firebase where all ingredients are stored
        ingredientsDatabase = FirebaseDatabase.getInstance().getReference("Ingredients");

        // create a list to track cuisine checkboxes
        cuisineBoxes = new ArrayList<>();
        cuisineBoxes.add(findViewById(R.id.italianCuisine));
        cuisineBoxes.add(findViewById(R.id.asianCuisine));
        cuisineBoxes.add(findViewById(R.id.mexicanCuisine));
        cuisineBoxes.add(findViewById(R.id.indianCuisine));
        cuisineBoxes.add(findViewById(R.id.mediterraneanCuisine));

        // get ingredients passed from camera or speech
        selectedIngredients = new ArrayList<>();
        detectedList = getIntent().getStringArrayListExtra("detectedIngredients");
        boolean fromDetection = getIntent().getBooleanExtra("fromDetection", false);

        if (detectedList == null) detectedList = new ArrayList<>();
        if (detectedList.isEmpty() && fromDetection) {
            // if nothing was detected, add placeholder
            detectedList.add("No ingredients detected");
        }

        // show detected ingredients in list view with checkboxes
        ArrayAdapter<String> detectedAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice, detectedList);
        detectedIngredientsListView.setAdapter(detectedAdapter);
        detectedIngredientsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // mark all detected items as selected by default
        for (int i = 0; i < detectedList.size(); i++) {
            detectedIngredientsListView.setItemChecked(i, true);
        }

        // make sure the placeholder isn’t selected
        if (detectedList.contains("No ingredients detected")) {
            int index = detectedList.indexOf("No ingredients detected");
            detectedIngredientsListView.setItemChecked(index, false);
        }

        // set up button actions
        searchBtn.setOnClickListener(v -> showSearchIngredientsDialog());
        generateBtn.setOnClickListener(v -> generateRecipes());
    }

    // opens popup to search and select ingredients from Firebase
    private void showSearchIngredientsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search & Add Ingredients");

        ingredientsDatabase.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> allIngredients = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String ingredient = data.getValue(String.class);
                    allIngredients.add(ingredient);
                }

                // build layout with search bar and list
                LinearLayout layout = new LinearLayout(SelectionActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setPadding(20, 20, 20, 20);

                SearchView searchView = new SearchView(SelectionActivity.this);
                searchView.setQueryHint("Search ingredients...");
                layout.addView(searchView);

                ListView listView = new ListView(SelectionActivity.this);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SelectionActivity.this,
                        android.R.layout.simple_list_item_multiple_choice, allIngredients);
                listView.setAdapter(adapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                // pre-select any already picked items
                for (int i = 0; i < allIngredients.size(); i++) {
                    if (selectedIngredients.contains(allIngredients.get(i))) {
                        listView.setItemChecked(i, true);
                    }
                }

                // live search filter
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override public boolean onQueryTextSubmit(String query) { return false; }
                    @Override public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return true;
                    }
                });

                layout.addView(listView);
                builder.setView(layout);

                // save selected ingredients
                builder.setPositiveButton("OK", (dialog, which) -> {
                    selectedIngredients.clear();
                    SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (checkedItems.get(i)) {
                            selectedIngredients.add(adapter.getItem(i));
                        }
                    }

                    Log.d("ManualIngredients", "Selected from Firebase: " + selectedIngredients);
                    Toast.makeText(SelectionActivity.this, "Ingredients Selected!", Toast.LENGTH_SHORT).show();
                });

                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SelectionActivity.this, "Failed to load ingredients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // combines selected ingredients and filters and fetches recipes
    private void generateRecipes() {
        List<String> finalIngredients = new ArrayList<>();

        // add ingredients that were checked in the list view
        SparseBooleanArray checked = detectedIngredientsListView.getCheckedItemPositions();
        for (int i = 0; i < detectedList.size(); i++) {
            if (checked.get(i)) {
                finalIngredients.add(detectedList.get(i));
            }
        }

        // add ingredients chosen through the search popup
        finalIngredients.addAll(selectedIngredients);

        // make sure placeholder doesn’t get sent to API
        finalIngredients.remove("No ingredients detected");

        Log.d("FinalIngredients", "Final ingredients sent: " + finalIngredients);

        if (finalIngredients.isEmpty()) {
            Toast.makeText(this, "Please select at least one ingredient!", Toast.LENGTH_SHORT).show();
            return;
        }

        // get dietary choice
        int selectedDietaryId = dietaryGroup.getCheckedRadioButtonId();
        String dietaryPreference = null;
        if (selectedDietaryId != -1) {
            RadioButton selectedDietaryButton = findViewById(selectedDietaryId);
            dietaryPreference = selectedDietaryButton.getText().toString().toLowerCase();
        }

        // check which cuisines were selected
        List<String> selectedCuisines = new ArrayList<>();
        for (CheckBox checkBox : cuisineBoxes) {
            if (checkBox.isChecked()) {
                selectedCuisines.add(checkBox.getText().toString().toLowerCase());
            }
        }

        // call Spoonacular API to fetch matching recipes
        SpoonacularAPI spoonacularAPI = new SpoonacularAPI(this);
        String finalDietaryPreference = dietaryPreference;

        spoonacularAPI.getRecipes(finalIngredients, selectedCuisines, finalDietaryPreference, new SpoonacularAPI.SpoonacularCallback() {
            @Override
            public void onSuccess(List<Recipe> recipes) {
                ArrayList<String> recipeTitles = new ArrayList<>();
                ArrayList<String> imageUrls = new ArrayList<>();

                for (Recipe recipe : recipes) {
                    recipeTitles.add(recipe.getTitle());
                    imageUrls.add(recipe.getImageUrl());
                }

                // pass the data to RecipesActivity
                Intent intent = new Intent(SelectionActivity.this, RecipesActivity.class);
                intent.putStringArrayListExtra("recipeTitles", recipeTitles);
                intent.putStringArrayListExtra("imageUrls", imageUrls);
                intent.putExtra("dietaryPreference", finalDietaryPreference);
                intent.putStringArrayListExtra("selectedCuisines", new ArrayList<>(selectedCuisines));
                intent.putStringArrayListExtra("selectedIngredients", new ArrayList<>(finalIngredients));
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("SpoonacularError", errorMessage);
                Toast.makeText(SelectionActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
