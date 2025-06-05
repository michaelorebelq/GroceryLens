package com.michaelorebela.grocerylens;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * The class preprocesses images for input into the YOLOv8 model.
 * Then resizes the image to 416x416 and normalises RGB values to the range [0, 1].
 */
public class ImageProcessorUtil {

    /**
     * Converts a Bitmap image into a normalized 4D float tensor.
     * The output tensor shape is [1][416][416][3], as expected by the model.
     *
     * @param bitmap The input image from the camera
     * @return A 4D float array representing the preprocessed image
     */
    public static float[][][][] preprocessImage(Bitmap bitmap) {
        int inputSize = 416;

        // Resizes the image to 416x416
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);

        // Creating the input tensor [batch, height, width, channels]
        float[][][][] input = new float[1][inputSize][inputSize][3];

        // extract and normalise RGB values
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                int pixel = resizedBitmap.getPixel(j, i);
                input[0][i][j][0] = Color.red(pixel) / 255.0f;
                input[0][i][j][1] = Color.green(pixel) / 255.0f;
                input[0][i][j][2] = Color.blue(pixel) / 255.0f;
            }
        }

        return input;
    }
}
