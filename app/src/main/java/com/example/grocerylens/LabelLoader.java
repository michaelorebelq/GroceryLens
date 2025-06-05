package com.example.grocerylens;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
/**
 * LabelLoader class is used to
 * load class labels from a text file in the assets directory.
 */
public class LabelLoader {
    private static final String TAG = "LabelLoader";
    public static List<String> loadLabels(Context context, String fileName) {
        List<String> labels = new ArrayList<>();
        try {
            // Open the file from assets
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            // Reads each line, trims it, and then add to list
            while ((line = reader.readLine()) != null) {
                labels.add(line.trim());
            }
            reader.close();
            Log.d(TAG, "Labels have been loaded successfully - " + labels.size());
        } catch (IOException e) {
            // Log error if loading fails
            Log.e(TAG, "Error loading the labels  " + e.getMessage());
        }
        return labels;
    }
}
