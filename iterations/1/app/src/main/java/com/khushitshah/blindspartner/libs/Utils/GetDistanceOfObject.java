package com.khushitshah.blindspartner.libs.Utils;

public class GetDistanceOfObject {
    /**
     * Returns approx. distance to a object calculated from given parameters.
     * It's no way correct.
     * Take a look at
     * https://photo.stackexchange.com/questions/12434/how-do-i-calculate-the-distance-of-an-object-in-a-photo
     * and,
     * https://www.pyimagesearch.com/2015/01/19/find-distance-camera-objectmarker-using-python-opencv/
     *
     * @param focal_length  Focal Length of camera.
     * @param pixels_width  pixels width of the object in image.
     * @param pixels_height pixels height of the object in image.
     * @param actual_width  actual width of object in real world.
     * @param actual_height actual height of object in real world.
     * @param image_width   image width,
     * @param image_height  image height.
     * @param camera_height height of the camera or height at which the photo was taken.
     * @return distance of object from camera.
     */
    public static float distanceToObject(float focal_length, int pixels_width,
                                         int pixels_height, int actual_width, int actual_height, int image_width, int image_height, int camera_height) {
        /*
          res1 for complex algorithm, res for simple algorithm.
         */
        float res, res1;
        // Simple algorithm

        res = (actual_width * focal_length) / pixels_width;

        res1 = (focal_length * actual_height * image_height) / (pixels_height * camera_height);

        System.out.println("res = " + res + " res1 =  " + res1);


        return (float) Math.ceil(res1);
    }


}
