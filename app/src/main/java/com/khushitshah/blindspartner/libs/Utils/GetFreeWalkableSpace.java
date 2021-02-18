package com.khushitshah.blindspartner.libs.Utils;

import com.khushitshah.blindspartner.libs.tflite.Classifier;

import java.util.HashSet;
import java.util.List;

import static com.khushitshah.blindspartner.ParseInputAndTakeAction.focal_length;
import static com.khushitshah.blindspartner.libs.tflite.TfliteProcessor.cropSize;

public class GetFreeWalkableSpace {
    public static int maxRow = 7;
    public static int maxColumn = 3;
    public static int margin = 5;

    public static HashSet<String> getFreeWalkableSpace(List<Classifier.Recognition> recognition, int imageWidth, int imageHeight) {

        Classifier.Recognition[][] inRegion = new Classifier.Recognition[maxRow][maxColumn];
        int regionWidth = imageWidth / (maxRow - 1);
        int regionHeight = imageHeight / (maxColumn - 1);

        for (Classifier.Recognition r : recognition) {
            if (r.getConfidence() > 0.33f) {
                float objX = r.getLocation().left;
                float objY = r.getLocation().top;
                float objWidth = r.getLocation().width();
                float objHeight = r.getLocation().height();
                if (objX < 0) {
                    objX = 0;
                }
                if (objY < 0) {
                    objY = 0;
                }
                if (objWidth > imageWidth) {
                    objWidth = imageWidth;
                }
                if (objHeight > imageHeight) {
                    objHeight = imageHeight;
                }

                int regionX = (int) (objX / regionWidth) + 1;
                if (objX % regionWidth <= margin) {
                    regionX++;
                    if (regionX >= maxRow) {
                        regionX = maxRow - 1;
                    }
                }
                int regionX1 = (int) ((objX + objWidth) / regionWidth) + 1;
                if (regionX1 >= maxRow) {
                    regionX1 = maxRow - 1;
                }
                if ((objX + objWidth) % regionWidth <= margin) {
                    regionX1--;
                    if (regionX1 < 0) {
                        regionX1 = 0;
                    }
                }
                int regionY = (int) ((objY / regionHeight)) + 1;
                if (objY % regionHeight <= margin) {
                    regionY++;
                    if (regionY >= maxColumn) {
                        regionY = maxColumn - 1;
                    }
                }

                int regionY1 = (int) ((objY + objHeight) / regionHeight) + 1;
                if (regionY1 >= maxColumn) {
                    regionY1 = maxColumn - 1;
                }
                if ((objY + objHeight) % regionHeight <= margin) {
                    regionY1--;
                    if (regionY1 < 0) {
                        regionY1 = 0;
                    }
                }

                inRegion[regionX][regionY] = r;
                inRegion[regionX1][regionY1] = r;
                for (int i = regionX; i <= regionX1; i++) {
                    inRegion[i][regionY] = r;
                }
                for (int j = regionX; j <= regionX1; j++) {
                    inRegion[j][regionY1] = r;
                }
            }
        }

        HashSet<String> freeRegion = new HashSet<>();
        StringBuilder str = new StringBuilder();
        boolean allNull = true;
        for (int i = 1; i < maxRow; i++) {
            for (int j = 1; j < maxColumn; j++) {
                if (inRegion[i][j] != null) {
                    allNull = false;
                }
            }
        }
        if (allNull) {
            return new HashSet<>();
        }
        for (int i = 1; i < maxRow; i++) {
            for (int j = 1; j < maxColumn; j++) {
                if (inRegion[i][j] == null) {
                    // free
                    // detect which part is it.
                    if (i == (maxRow - 1) / 2 || i == (maxRow - 1) / 2 + 1) {
                        if (j > (maxColumn - 1) / 2) {
                            String distance = "";
                            for (int k = 1; k <= (maxColumn - 1) / 2; k++) {
                                if (inRegion[i][k] == null) continue;
                                float dis = GetDistanceOfObject.distanceToObject(focal_length, (int) inRegion[i][k].getLocation().width(), (int) inRegion[i][k].getLocation().height(), 400, 500, cropSize, cropSize, 5500);
                                if (dis > 3.5f) {
                                    dis = 3f;
                                }
                                distance = "" + dis;
                            }
                            if (distance.equals("")) {
                                distance = "3 steps";
                            } else {
                                distance = distance + " steps";
                            }
                            if (str.indexOf("straight") == -1) {
                                str.append("straight ").append(distance);


                                // return if straight is found!!.
                                freeRegion.clear();
                                freeRegion.add("straight " + distance);
                                return freeRegion;
                            }
                        }
                    } else if (i < (maxRow - 1) / 2) {
                        if (j > (maxColumn - 1) / 2) {
                            String distance = "";
                            for (int k = 1; k <= (maxColumn - 1) / 2; k++) {
                                if (inRegion[i][k] == null) continue;

                                float dis = GetDistanceOfObject.distanceToObject(focal_length, (int) inRegion[i][k].getLocation().width(), (int) inRegion[i][k].getLocation().height(), 400, 500, cropSize, cropSize, 5500);
                                if (dis > 3.5f) {
                                    dis = 3f;
                                }
                                distance = "" + dis;
                            }
                            if (distance.equals("")) {
                                distance = "3 steps";
                            } else {
                                distance = distance + " steps";
                            }
                            if (str.indexOf("left ") == -1) {
                                str.append("left ").append(distance);
                                freeRegion.add("left  " + distance);
                            }
                        }
                    } else {
                        if (j > (maxColumn - 1) / 2) {
                            String distance = "";
                            for (int k = 1; k <= (maxColumn - 1) / 2; k++) {
                                if (inRegion[i][k] == null) continue;
                                float dis = GetDistanceOfObject.distanceToObject(focal_length, (int) inRegion[i][k].getLocation().width(), (int) inRegion[i][k].getLocation().height(), 400, 500, cropSize, cropSize, 5500);
                                if (dis > 3.5f) {
                                    dis = 3f;
                                }
                                distance = "" + dis;
                            }
                            if (distance.equals("")) {
                                distance = "3 steps";
                            } else {
                                distance = distance + " steps";
                            }
                            if (str.indexOf("right") == -1) {
                                str.append("right ").append(distance);
                                freeRegion.add("right " + distance);
                            }
                        }
                    }
                }

            }

        }

        return freeRegion;
    }
}
