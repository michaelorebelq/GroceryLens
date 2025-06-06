package com.michaelorebela.grocerylens;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * CameraActivity handles capturing an image from the camera,
 * running the YOLOv8 model, and sending the detected ingredients to the next activity.
 */
public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraView";

    private ImageView capturedImageView;
    private TextView detectingText;
    private Interpreter tflite;
    private List<String> labels;

    // Launchers
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        capturedImageView = findViewById(R.id.capturedImageView);
        detectingText = findViewById(R.id.detectingText);
        Button captureButton = findViewById(R.id.captureButton);

        // Load class labels
        labels = LabelLoader.loadLabels(this, "labels.txt");

        // Load the YOLOv8 model
        try {
            tflite = TFLiteModelLoader.loadModelFile(this, "best_float32.tflite");
            Log.d(TAG, "TFLite model loaded");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model: " + e.getMessage());
            Toast.makeText(this, " ERROR TFLite model", Toast.LENGTH_LONG).show();
            finish();
        }

        // Register permission request launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission required to use this feature", Toast.LENGTH_SHORT).show();
                    }
                });

        // Register camera intent launcher
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        capturedImageView.setImageBitmap(bitmap);

                        float[][][][] inputData = ImageProcessorUtil.preprocessImage(bitmap);

                        float[][][] outputData = new float[1][21][3549];

                        long startTime = SystemClock.elapsedRealtime();
                        tflite.run(inputData, outputData);
                        long endTime = SystemClock.elapsedRealtime();
                        long inferenceTime = endTime - startTime;
                        Log.d("InferenceTimeTest", "Inference time is " + inferenceTime + " ms");

                        List<String> detectedLabels = processTFLiteOutput(outputData, bitmap.getWidth(), bitmap.getHeight());
                        sendResults(detectedLabels);
                    }
                });

        // When capture button is clicked, check permission first
        captureButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    /**
     * Launches the device camera to capture an image.
     */
    private void openCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    /**
     * Processes model output using the BoundingBoxProcessor.
     */
    private List<String> processTFLiteOutput(float[][][] outputData,
                                             int imageWidth, int imageHeight) {
        List<String> detectedLabels = new ArrayList<>();

        List<BoundingBox> boxes = BoundingBoxProcessor.extractBoxes(
                outputData, labels, 0.15F, imageWidth, imageHeight);

        for (BoundingBox box : boxes) {
            for (String label : box.classLabels) {
                if (!detectedLabels.contains(label)) {
                    detectedLabels.add(label);
                }
            }
        }
        return detectedLabels;
    }

    /**
     * Sends the detected ingredients to SelectionActivity.
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
