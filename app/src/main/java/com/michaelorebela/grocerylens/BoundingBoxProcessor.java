package com.michaelorebela.grocerylens;

import java.util.ArrayList;
import java.util.List;

/**
 * Extraction of the bounding boxes from the model's output tensor
 *
 */
public class BoundingBoxProcessor {
    public static List<BoundingBox> extractBoxes(float[][][] outputData,
                                                 List<String> labels, float confidenceThreshold,
                                                 int imageWidth, int imageHeight) {
        List<BoundingBox> boxes = new ArrayList<>();
 // This loops through each of the 3549 predictions in the output tensor
        for (int i = 0; i < 3549; i++) {
            float x_centre = outputData[0][0][i];
            float y_centre = outputData[0][1][i];
            float width = outputData[0][2][i];
            float height = outputData[0][3][i];

            //creates lists to store class labels and their confidence scores for this box
            List<String> classLabels = new ArrayList<>();
            List<Float> classConfidences = new ArrayList<>();
            //starts from index 4 because of the x,y,w,h occupying first 4 classes
            //rest of classes is 17
            for (int j = 4; j < 21; j++) {
                float classConfidence = outputData[0][j][i];
                if (classConfidence > confidenceThreshold) {
                    int classIndex = j - 4; // match label list
                    if (classIndex < labels.size()) {
                        classLabels.add(labels.get(classIndex));
                        classConfidences.add(classConfidence);
                    }
                }
            }
            //box centre format converted to pixel coordinates
            if (!classLabels.isEmpty()) {
                float x1 = (x_centre - (width / 2)) * imageWidth;
                float y1 = (y_centre - (height / 2)) * imageHeight;
                float x2 = (x_centre + (width / 2)) * imageWidth;
                float y2 = (y_centre + (height / 2)) * imageHeight;

                // add the bounding box to the list
                BoundingBox box = new BoundingBox(x1, y1, x2, y2, classLabels,
                        classConfidences);
                boxes.add(box);
            }
        }
// This is where nms is applied with a IoU threshold of 0.6
        return NMSProcessor.applyNMS(boxes, 0.6F);
    }
}
