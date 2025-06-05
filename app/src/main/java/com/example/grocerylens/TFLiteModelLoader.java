package com.example.grocerylens;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


//method to use load tflite model into grocerylens
public class TFLiteModelLoader {
    private static final String TAG = "TFLiteModelLoader";

    // this class loads the tfLite model and creates an interpreter for inference
    public static Interpreter loadModelFile(Context context, String modelFileName)
            throws IOException {
        MappedByteBuffer modelBuffer
                = loadModelFromAssets(context, modelFileName);
        return new Interpreter(modelBuffer);
    }
    // maps the tfLite model file into memory for use
    private static MappedByteBuffer loadModelFromAssets(Context context, String modelFileName)
            throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(),
                fileDescriptor.getDeclaredLength());
    }
}
