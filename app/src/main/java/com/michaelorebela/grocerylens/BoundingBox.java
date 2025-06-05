package com.michaelorebela.grocerylens;

import java.util.List;

/**
 * The class stores bounding box data from object detection
 * (x1, y1, x2, y2), class labels, and their confidence scores
 */
public class BoundingBox {
    public float x1, y1, x2, y2;
    public List<String> classLabels;
    public List<Float> classConfidences;

    public BoundingBox(float x1, float y1, float x2, float y2, List<String>
            classLabels, List<Float> classConfidences) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.classLabels = classLabels;
        this.classConfidences = classConfidences;
    }
}
