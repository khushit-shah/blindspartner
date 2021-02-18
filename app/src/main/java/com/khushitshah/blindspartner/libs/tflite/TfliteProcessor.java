package com.khushitshah.blindspartner.libs.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.Toast;

import com.khushitshah.blindspartner.libs.Utils.GetDirectionOfObject;
import com.khushitshah.blindspartner.libs.Utils.GetDistanceOfObject;
import com.khushitshah.blindspartner.libs.Utils.GetFreeWalkableSpace;
import com.khushitshah.blindspartner.libs.Utils.ImageUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import static com.khushitshah.blindspartner.libs.Utils.GetFreeWalkableSpace.maxRow;

public class TfliteProcessor {
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;

    // Minimum detection confidence to track a detection.
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.5f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static HandlerThread handlerThread = new HandlerThread("inference");
    private static Handler handler;
    private static Classifier classifier;
    public static int cropSize = TF_OD_API_INPUT_SIZE;

    public TfliteProcessor(final Context mainActivity) {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        try {
            classifier = TFLiteObjectDetectionAPIModel.create(
                    mainActivity.getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_LABELS_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED);
        } catch (IOException e) {
            e.printStackTrace();
            Toast toast =
                    Toast.makeText(
                            mainActivity.getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT);
            toast.show();
        }
        cropSize = TF_OD_API_INPUT_SIZE;
    }

    private static void runInBackground(Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    public static Bitmap getResizedBitmap(final Bitmap bm, Matrix rotationMatrix) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, rotationMatrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public void processImage(byte[] imageData, int width, int height, float focal_length, TfliteProcessedCallback callback, boolean freespace) {
        runInBackground(() -> {
            // set options so image is loaded correctly.
            long time = System.currentTimeMillis();
            Matrix rotationMatrix = ImageUtils.getTransformationMatrix(width, height, cropSize, cropSize, 90, false);
            Bitmap rawBitmap;
            boolean video = imageData.length > width * height;
            if (video) {
                int[] output = new int[width * height];
                ImageUtils.convertYUV420SPToARGB8888(imageData, width, height, output);
                rawBitmap = Bitmap.createBitmap(output, width, height, Bitmap.Config.ARGB_8888);
            } else
                rawBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

            if (rawBitmap == null) {
                callback.processed("");
                return;
            }
            System.out.println(rawBitmap.getWidth() + " " + rawBitmap.getHeight());

            // Crop the image to cropSize
            final Bitmap croppedBitmp = getResizedBitmap(rawBitmap, rotationMatrix);
            if (croppedBitmp == null) {
                System.out.println("raw bitmap is null");
            }
            System.out.println("Cropping image took " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
            final List<Classifier.Recognition> results = classifier.recognizeImage(croppedBitmp);
            System.out.println("Classifier took " + (System.currentTimeMillis() - time));
            StringBuilder result = new StringBuilder();
            String prev = "";
            Canvas drawCanvas = new Canvas(croppedBitmp);
            Paint myPaint = new Paint();
            myPaint.setColor(Color.rgb(0, 0, 0));
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(5);
            if (!freespace) {
                for (Classifier.Recognition r : results) {
                    if (r.getConfidence() > MINIMUM_CONFIDENCE_TF_OD_API) {
                        int distance = (int) GetDistanceOfObject.distanceToObject(focal_length, (int) r.getLocation().width(), (int) r.getLocation().height(), 400, 500, cropSize, cropSize, 5500);

                        if (!video) {
                            result.append("There is a ").append(r.getTitle()).append(" ,").append(distance).append(" steps away ").append(GetDirectionOfObject.getDirectionOfObjectInImage(cropSize, cropSize, r.getLocation().left, r.getLocation().top, r.getLocation().width(), r.getLocation().height())).append(".##");
                        } else {
                            result.append(r.getTitle()).append(" ").append(distance).append(" steps ").append(GetDirectionOfObject.getDirectionOfObjectInImage(cropSize, cropSize, r.getLocation().left, r.getLocation().top, r.getLocation().width(), r.getLocation().height())).append("##");
                        }
                        prev = r.getTitle();
                    }
                }
            } else {
                for (Classifier.Recognition r : results) {
                    if (r.getConfidence() > 0.33f) {
                        myPaint.setColor(Color.BLACK);
                        drawCanvas.drawLine(0, cropSize / 2, cropSize, cropSize / 2, myPaint);
                        for (int i = 1; i < maxRow; i++) {
                            drawCanvas.drawLine(((cropSize / (maxRow - 1)) * i), 0, ((cropSize / (maxRow - 1)) * i), cropSize, myPaint);
                        }
                        myPaint.setColor(Color.RED);
                        drawCanvas.drawRect(r.getLocation().left, r.getLocation().top, r.getLocation().right, r.getLocation().bottom, myPaint);
                        myPaint.setColor(Color.YELLOW);
                        Paint text = new Paint();
                        text.setColor(Color.BLACK);
                        text.setStrokeWidth(0.3f);
                        drawCanvas.drawText(r.getTitle(), (r.getLocation().left + r.getLocation().width()) / 2, (r.getLocation().left + r.getLocation().height()) / 2, text);
                    }
                }

                result.delete(0, result.length());
                ImageUtils.saveBitmap(croppedBitmp);
                HashSet<String> freeSpace = GetFreeWalkableSpace.getFreeWalkableSpace(results, cropSize, cropSize);
                if (freeSpace.size() <= 0) {
                    result.append(" blockage ");
                    callback.processed(result.toString());
                    return;
                } else if (freeSpace.size() == 1) {
                    for (String s : freeSpace) {
                        result.append(s);
                        callback.processed(result.toString());
                        return;
                    }
                } else {
                    int count = 0;
                    for (String s : freeSpace) {
                        if (count == 0)
                            result.append(s).append(" or ");
                        else
                            result.append(s).append(".");
                        count++;
                    }
                }
                callback.processed(result.toString());
                return;
            }
            if (!video && result.toString().equals("There is")) {
                result = new StringBuilder();
                result.append("Can't detect any objects in front of you");
            }
            callback.processed(result.toString());
        });
    }
}
