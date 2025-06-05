package com.example.grocerylens;

/**
 * the IoUCalculator class for calculating the Intersection over Union (IoU)
 * between two bounding boxes.
 */
public class IoUCalculator {

    /**
     * Calculates the IoU score between two bounding boxes.
     *
     * @param box1 The first bounding box
     * @param box2 The second bounding box
     * @return IoU score as a float between 0 and 1
     */
    public static float calculateIoU(BoundingBox box1, BoundingBox box2) {
        //finds the overlapping region
        float x1 = Math.max(box1.x1, box2.x1);
        float y1 = Math.max(box1.y1, box2.y1);
        float x2 = Math.min(box1.x2, box2.x2);
        float y2 = Math.min(box1.y2, box2.y2);
        //calculates intersection and area
        float intersection = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);

        float area1 = (box1.x2 - box1.x1) * (box1.y2 - box1.y1);
        float area2 = (box2.x2 - box2.x1) * (box2.y2 - box2.y1);

        // compute the IoU
        return intersection / (area1 + area2 - intersection);
    }
}
