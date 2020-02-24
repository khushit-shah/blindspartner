package com.khushitshah.blindspartner.libs.processing.image;

import android.content.Context;

import com.khushitshah.blindspartner.libs.Utils.JsonToString;
import com.khushitshah.blindspartner.libs.tflite.TfliteProcessor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;

/**
 * Process the input Image.
 * If online mode, It will request a server to do it.
 * If offline mode, It will do it offline with tensorflowlite.
 */
public class ProcessImage {
    private static TfliteProcessor tfliteProcessor;
    private Context activity;
    private byte[] data;
    private boolean online;
    private int width, height;
    private float focal_length;
    private String url = "http://192.168.43.160:3000";

    public ProcessImage(Context activity, byte[] path, float focal_length, int width, int height, boolean online) throws Exception {
        this.data = path;
        this.online = online;
        this.focal_length = focal_length;
        this.width = width;
        this.height = height;
        this.activity = activity;
        if (!online) {
            if (tfliteProcessor == null) {
                tfliteProcessor = new TfliteProcessor(activity);
            }
        }
    }

    public ProcessImage(Context activity, float focal_length, int width, int height, boolean online) throws Exception {
        this.online = online;
        this.focal_length = focal_length;
        this.width = width;
        this.height = height;
        this.activity = activity;
        System.out.println(" width = " + width + " height " + height);
        if (!online) {
            if (tfliteProcessor == null) {
                tfliteProcessor = new TfliteProcessor(activity);
            }
        }
    }

    public void startProcessing(ImageProcessedInterface callback) {
        if (online) {
            Thread t = new Thread(() -> {
                String output = "";
                try {
                    output = JsonToString.jsonToString(getJSONArrayFromUrl(), focal_length, width, height);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    output += "can't fetch response try again later!";
                    System.err.println("output : " + e.getMessage());
                }
                callback.imageProcessed(output);
            });
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        } else {
            tfliteProcessor.processImage(data, width, height, focal_length, callback::imageProcessed, false);
        }
    }

    public void startProcessing(byte[] data, ImageProcessedInterface callback, boolean freespace) {
        if (online) {
            Thread t = new Thread(() -> {
                String output = "";
                try {
                    output = JsonToString.jsonToString(getJSONArrayFromUrl(), focal_length, width, height);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    output += "can't fetch response try again later!";
                    System.err.println("output : " + e.getMessage());
                }
                callback.imageProcessed(output);
            });
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        } else {
            tfliteProcessor.processImage(data, width, height, focal_length, callback::imageProcessed, freespace);
        }
    }


    private JSONArray getJSONArrayFromUrl() throws IOException, JSONException {
        String res = getResponseFromServer();
        return new JSONArray(res);
    }

    private String getResponseFromServer() throws IOException {
        System.out.println("Requesting server for response");
        long time = System.currentTimeMillis();
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // Rotate from landscape to portrait.
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//        Matrix matrix = new Matrix();
//        matrix.postRotate(90);

        //        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//
//        width = bitmap.getWidth();
//        height = bitmap.getHeight();
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost postRequest = new HttpPost(url);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//        reqEntity.addPart("name", new StringBody("img"));
//        reqEntity.addPart("Id", new StringBody("img"));
        System.out.println(time - System.currentTimeMillis());
        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos);
//            byte[] data = bos.toByteArray();
            ByteArrayBody bab = new ByteArrayBody(data, "1.jpg");
            reqEntity.addPart("img", bab);
            System.out.println(time - System.currentTimeMillis());
        } catch (Exception e) {
            //Log.v("Exception in Image", ""+e);
            reqEntity.addPart("img", new StringBody(""));
        }
        postRequest.setEntity(reqEntity);
        HttpResponse response = httpClient.execute(postRequest);
        response.getEntity().getContent().read(data);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
//        String sResponse;
//        StringBuilder s = new StringBuilder();
//        while ((sResponse = reader.readLine()) != null) {
//            s = s.append(sResponse);
//        }
        System.out.println(time - System.currentTimeMillis());

        return new String(data);
    }
}
