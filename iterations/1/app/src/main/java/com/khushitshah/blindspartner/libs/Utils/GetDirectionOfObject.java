package com.khushitshah.blindspartner.libs.Utils;

public class GetDirectionOfObject {
    public static String getDirectionOfObjectInImage(int imageWidth, int imageHeight, float objectX, float objectY, float objectWidth, float objectHeight) {
        int errorRate = 10; // allow error of 10px,

        int halfX = imageWidth / 2;
        int halfY = imageHeight / 2;

        boolean inTopLeftCorner = false;
        boolean inTopRightCorner = false;
        boolean inBottomRightCorner = false;
        boolean inBottomLeftCorner = false;


        if (objectX < halfX && objectY < halfY) {
            inTopLeftCorner = true;
        }

        if ((objectX > halfX && objectY > halfY) || ((objectX + objectWidth) > halfX && (objectY + objectHeight > halfY))) {
            inBottomRightCorner = true;
        }

        if ((objectX < halfX && objectY > halfY) || (objectX < halfX && (objectY + objectHeight > halfY))) {
            inBottomLeftCorner = true;
        }

        if ((objectX > halfX && objectY < halfY) || (objectX + objectWidth) > halfX && objectY < halfY) {
            inTopRightCorner = true;
        }

        if (inTopLeftCorner && inTopRightCorner && inBottomRightCorner && inBottomLeftCorner) {
            return "exact in front";
        } else if (inTopLeftCorner && inTopRightCorner) {
            return "far in front of you";
        } else if (inBottomLeftCorner && inBottomRightCorner) {
            return "in bottom front of you";
        } else if (inTopLeftCorner && inBottomLeftCorner) {
            return "to your left";
        } else if (inTopRightCorner && inBottomRightCorner) {
            return "to your right";
        } else if (inTopLeftCorner) {
            return "far in left of you";
        } else if (inTopRightCorner) {
            return "far in right of you";
        } else if (inBottomLeftCorner) {
            return "in your bottom left";
        } else if (inBottomRightCorner) {
            return "in your bottom right";
        }

        return "";
    }
}
