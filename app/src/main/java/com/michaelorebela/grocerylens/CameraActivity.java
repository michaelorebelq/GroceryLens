package com.michaelorebela.grocerylens;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CameraActivity handles capturing an image from the camera
 * then running the YOLOv8 model, and sending the detected ingredients to the next
 * activity.
 */

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraView";
    private ImageView capturedImageView;
    private TextView detectingText;
    private Interpreter tflite;
    private List<String> labels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        capturedImageView = findViewById(R.id.capturedImageView);
        detectingText = findViewById(R.id.detectingText);
        Button captureButton = findViewById(R.id.captureButton);

        // Uses LabelLoader class to load class labels
        labels = LabelLoader.loadLabels(this, "labels.txt");

        //loads .tflite file into memory using TFLiteModelLoader
        try {

            tflite = TFLiteModelLoader.loadModelFile(this, "best_float32.tflite");
            Log.d(TAG, "TFLite model loaded");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model: " + e.getMessage());
            Toast.makeText(this, " ERROR TFLite model", Toast.LENGTH_LONG).show();
            finish();
        }

        // Opens device camera on button click
        captureButton.setOnClickListener(v -> openCamera());
    }

    /**
     * Launches the device camera to capture an image.
     */
    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }
    /**
     * Results from the camera intent is handled here.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            capturedImageView.setImageBitmap(bitmap);

            //preprocess the image and convert into a 4d tensor
            float[][][][] inputData = ImageProcessorUtil.preprocessImage(bitmap);

            // output 3d tensor format
            float[][][] outputData = new float[1][21][3549];
            //  time the inference for performance tests
            long startTime = SystemClock.elapsedRealtime();
            //Run inference
            tflite.run(inputData, outputData);

            long endTime = SystemClock.elapsedRealtime();
            long inferenceTime = endTime - startTime;
            Log.d("InferenceTimeTest", "Inference time is " + inferenceTime + " ms");

            // Extracting the detected labels
            List<String> detectedLabels = processTFLiteOutput(outputData, bitmap.getWidth(), bitmap.getHeight());

            sendResults(detectedLabels);
        }
    }

    /**
     * logic for post processing using the BoundingBoxProcessor
     *
     */
    private List<String> processTFLiteOutput(float[][][] outputData,
                                             int imageWidth, int imageHeight) {
        //create empty list to store labels
        List<String> detectedLabels = new ArrayList<>();

        List<BoundingBox> boxes = BoundingBoxProcessor.extractBoxes(
                // confidence threshold set at 0.15
                outputData, labels, 0.15F, imageWidth, imageHeight);
        for (BoundingBox box : boxes) {
            for (String label : box.classLabels) {
                //add the label only if its not already in the list

                if (!detectedLabels.contains(label)) {
                    detectedLabels.add(label);
                }
            }
        }
        return detectedLabels;
    }


    /**
     * passes the detected ingredients to selection activity
     */
    @SuppressLint("SetTextI18n")
    public void sendResults(List<String> detectedLabels) {
        if (detectedLabels.isEmpty()) {
            detectingText.setText(R.string.no_ingredients_detected);
            return;
        }
        detectingText.setText("Detected: " + String.join(", ", detectedLabels));
        Intent intent = new Intent(this, SelectionActivity.class);
        intent.putStringArrayListExtra("detectedIngredients",
                new ArrayList<>(detectedLabels));
        startActivity(intent);
    }
}
