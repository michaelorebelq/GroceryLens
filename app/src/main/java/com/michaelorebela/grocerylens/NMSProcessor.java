package com.michaelorebela.grocerylens;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles Non-Maximum Suppression NMS which is used
 * to filter out overlapping bounding boxes after object detection.
 * It helps reduce duplicate detections by keeping only the most confident ones.
 */
public class NMSProcessor {

    /**
     * Applies Non-Maximum Suppression to a list of bounding boxes.
     * It sorts the boxes by confidence and removes any that overlap too much
     * with boxes already selected.
     *
     * @param boxes        the list of detected bounding boxes
     * @param iouThreshold the threshold to decide if boxes overlap too much set at 0.6
     * @return the list of boxes after suppression
     */
    public static List<BoundingBox> applyNMS(List<BoundingBox> boxes, float iouThreshold) {
        List<BoundingBox> selectedBoxes = new ArrayList<>();

        // sort boxes by highest confidence first
        boxes.sort((b1, b2) ->
                Float.compare(getMaxConfidence(b2), getMaxConfidence(b1)));

        // loop through boxes and remove ones that overlap with already selected ones
        while (!boxes.isEmpty()) {
            BoundingBox bestBox = boxes.remove(0);  // take the top one
            selectedBoxes.add(bestBox);

            // remove boxes that have high overlap with the current best box
            boxes.removeIf(box -> IoUCalculator.calculateIoU(bestBox, box) > iouThreshold);
        }

        return selectedBoxes;
    }

    /**
     * gets the highest confidence score from a bounding box.
     * since a box might have multiple class predictions, the max score is taken.
     *
     * @param box the bounding box to check
     * @return the highest confidence score from the box
     */
    private static float getMaxConfidence(BoundingBox box) {
        float maxConfidence = 0;
        for (float conf : box.classConfidences) {
            if (conf > maxConfidence) {
                maxConfidence = conf;
            }
        }
        return maxConfidence;
    }
}
