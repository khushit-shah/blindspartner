package com.khushitshah.blindspartner;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.SurfaceView;

import androidx.core.content.PermissionChecker;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.khushitshah.blindspartner.libs.Audio.Input.SpeechInput;
import com.khushitshah.blindspartner.libs.Audio.Output.AudioOutput;
import com.khushitshah.blindspartner.libs.Language.Languages;
import com.khushitshah.blindspartner.libs.Location.Location;
import com.khushitshah.blindspartner.libs.News.FetchAsyncNews;
import com.khushitshah.blindspartner.libs.News.NewsTypes;
import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;
import com.khushitshah.blindspartner.libs.notification.NotificationListener;
import com.khushitshah.blindspartner.libs.processing.image.ProcessImage;
import com.khushitshah.blindspartner.libs.travelling.TravelModeReturnInterface;
import com.khushitshah.blindspartner.libs.travelling.Travelling;
import com.khushitshah.blindspartner.libs.travelling.TravellingInterface;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.PermissionChecker.checkSelfPermission;

public class ParseInputAndTakeAction {
    public static float focal_length = 3.3f;
    private static final String helpString = "Playing Help Message. Blind's Partner is app aimed to help blinds to live an independent life." +
            "Following are the commands supported in blinds partner with their syntax, usage and description." +
            " CALL: CALL or C A L L command is used to call a number or contact, provide number or contact name after call command. example, call Mom" +
            " Message: Message or M E S S A G E command is used to message a contact or number, provide number or contact. Then provide message to sent when asked. example message mom" +
            " location: Location or L O C A T I O N command speaks up current location to user." +
            " time: Time or T I M E command speaks current phone time to user" +
            " news: New or N E W S Speaks up 10 most recent indian news to user" +
            " sos: sos or S O S Sends message to emergency number with current time and current location, Also calls emergency number in speaker mode." +
            " image: Image or I M A G E Captures a Image and speaks detected objects with their approximate distance in meters" +
            " video: Video or V I D E O captures video and speaks detected objects, speak stop to stop" +
            " travel: travel or T R A V E L helps user navigate to other places, pass destination after travel command, Then click on lower left corner after few seconds, then it will start your navigation. example traavel to kkv chowk rajkot" +
            " stop: Stop or S T O P command stops currently executing command." +
            " help: help or H E L P command speaks up help message.";

    private static final long TIME_TO_WAIT_AFTER_OPENING_MAP = 20000;

    private static int TIME_TO_WAIT_BETWEEN_IMAGES_IN_VIDEO = 300;
    private static Camera c = null;
    private static int CUR_TASK_ID = 0;
    private final String[] mostRecentDetection = new String[]{""};
    int id;
    private SurfaceView view;
    private AudioOutput audioOutput;
    private SpeechInput mSpeechInput;
    private boolean running = false;
    private Context context;
    private Travelling travelling;
    private int curVideoPrecessingIndex = 0;

    ParseInputAndTakeAction(Context context) {
        this.context = context;
        // TODO: Add method to vary the speech rate too low in priority!.
        audioOutput = new AudioOutput(context, 2f);
        mSpeechInput = new SpeechInput(context);
        travelling = new Travelling();
    }

    void parseAudioInput(String s) {
        int cur_task_id = ++CUR_TASK_ID;
        String sLowerCasedTrimmed = s.toLowerCase().trim();
        System.out.println(s);
        // loop through all words.
        boolean containsKeyword = false;
        outer:
        for (String sCase : sLowerCasedTrimmed.split(" ")) {
            if ("call".equals(sCase)) {
                containsKeyword = true;
                s = sLowerCasedTrimmed.substring(sLowerCasedTrimmed.indexOf("call"));
                s = s.replace("call", " ");
                System.out.println(s);
                // lets check if it contains a number instead of name.
                if (s.trim().replaceAll(" ", "").matches("^[0-9+]+$")) {
                    // Number is provided call it.
                    System.out.println(s.trim().replaceAll(" ", ""));
                    callNumberWithConfirm(s.trim().replace(" ", ""));
                } else {
                    System.out.println(s + "==1");
                    getNumberFromName(s.toLowerCase().trim(), "call", (number) -> {
                        System.out.println(number);
                        if (number.equals("")) return;
                        callNumberWithConfirm(number);
                    });
                }
                break;
            } else if ("message".equals(sCase)) {
                containsKeyword = true;
                s = s.substring(s.indexOf("message"));
                s = s.replace("message", " ");
                if (s.trim().replace(" ", "").matches("^[0-9 +]+$")) {
                    // Number is provided call it.
                    messageNumberWithConfirm(s.trim().replace(" ", ""));
                } else {
                    getNumberFromName(s.toLowerCase().trim(), "message", (String number) -> {
                        if (number.equals("")) return;
                        // Get the message body from user.
                        messageNumberWithConfirm(number);
                    });
                }
                break;
            } else if ("location".equals(sCase)) {
                containsKeyword = true;
                Location location = new Location(context);
                audioOutput.speak("fetching Location!", TypesOfSentences.TYPE_FETCHING, TextToSpeech.QUEUE_ADD, "_", () -> location.getLocation((data) -> audioOutput.speak(data, TypesOfSentences.TYPE_LOCATION, TextToSpeech.QUEUE_ADD, "location", () -> {
                })));
                break;
            } else if ("time".equals(sCase)) {
                containsKeyword = true;
                Date currentTime = Calendar.getInstance().getTime();
                audioOutput.speak(currentTime.toLocaleString(), TypesOfSentences.TYPE_TIME, TextToSpeech.QUEUE_ADD, "date", () -> {
                });
                break;
            } else if ("news".equals(sCase)) {
                containsKeyword = true;
                // a77b9f63b67642738c7679e9ecc646d0 NEWS API KEY!!
                audioOutput.speak("fetching news!", TypesOfSentences.TYPE_FETCHING, TextToSpeech.QUEUE_ADD, "fetch_news", () -> FetchAsyncNews.fetchNews("in", "", "", NewsTypes.HEADLINES, (String res) -> {
                    if (cur_task_id == CUR_TASK_ID) {
                        audioOutput.speak(res, TypesOfSentences.TYPE_NEWS, TextToSpeech.QUEUE_ADD, "News", () -> {
                        });
                    }
                }));
                break;
            } else if ("stop".equals(sCase)) {
                containsKeyword = true;
                audioOutput.speak("Stopped", TypesOfSentences.TYPE_STOP, TextToSpeech.QUEUE_FLUSH, "stop", () -> {
                });
                break;
            } else if ("sos".equals(sCase)) {
                containsKeyword = true;
                audioOutput.speak("Messaging and calling emergency number", TypesOfSentences.TYPE_SOS_MSG_INFORM, TextToSpeech.QUEUE_ADD, "stop12", () -> {
                    final String[] mesgToSend = {""};
                    Location location = new Location(context);
                    location.getLocation((data) -> {
                        mesgToSend[0] += data;
                    });

                    mesgToSend[0] = "  " + Calendar.getInstance().getTime().toLocaleString() + mesgToSend[0];
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(StaticSettings.emergencyNumber, null, mesgToSend[0], null, null);
                    audioOutput.speak("Message Sent! Now Calling", TypesOfSentences.TYPE_SOS_MSG_INFORM2, TextToSpeech.QUEUE_ADD, "122", () -> {
                    });

                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse("tel:" + StaticSettings.emergencyNumber.trim().replace(" ", "")));
                    if (checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PermissionChecker.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        return;
                    }
                    context.startActivity(i);

                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                    audioManager.setSpeakerphoneOn(true);
                });
                break;
            } else if ("image".equals(sCase)) {
                containsKeyword = true;
                captureAndProcessImage(false);
                break outer;
            } else if ("travel".equals(sCase)) {
                containsKeyword = true;
                String temp;
                String destination;
                if (sLowerCasedTrimmed.contains("travel to")) {
                    temp = sLowerCasedTrimmed.substring(s.indexOf("travel to") + "travel to".length());
                } else {
                    temp = sLowerCasedTrimmed.substring(s.indexOf("travel") + "travel".length());
                }

                System.out.println(temp);
                destination = temp;
                Location location = new Location(context);
                android.location.Location loc = location.getCurLocation();
                travelling.getAllModeTime(loc, destination, new TravellingInterface() {
                    @Override
                    public void getTravelMode(String[][] modesWithDistance, TravelModeReturnInterface call) {
                        if (modesWithDistance.length == 1) {
                            audioOutput.speak("Do you want to travel by " + modesWithDistance[0][0] + " takes time " + modesWithDistance[0][1], TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_ADD, "tr", () -> {
                                mSpeechInput.startListening((ans) -> {
                                    if (ans.contains("yes") || ans.contains("yea") || ans.contains("sure")) {
                                        call.accept(modesWithDistance[0][0]);
                                    }
                                });
                            });
                        } else if (modesWithDistance.length == 2) {
                            audioOutput.speak("Do you want to travel by one " + modesWithDistance[0][0] + " takes time " + modesWithDistance[0][1] + " or two " + modesWithDistance[1][0] + " takes time " + modesWithDistance[1][1], TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_ADD, "tr", () -> {
                                mSpeechInput.startListening((ans) -> {
                                    System.out.println("user answer :" + ans);
                                    if (ans.contains("1") || ans.contains("walk")) {
                                        call.accept(modesWithDistance[0][0]);
                                    } else if (ans.contains("2") || ans.contains("transit")) {
                                        call.accept(modesWithDistance[1][0]);
                                    }
                                });
                            });
                        } else {
                            audioOutput.speak("no travelling information found", TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_ADD, "tr", () -> {
                            });
                        }
                    }

                    @Override
                    public void speakUpdate(String updateSentence) {
                        audioOutput.speak(updateSentence, TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_ADD, "tr", () -> {
                        });
                    }
                }, ((Activity) context));

//                String url = "https://www.google.com/maps/dir/?api=1&destination=" + destination + mode=walking&dir_action=navigate";
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//
//                // TODO: Open our own app.
//                // Get Back to top after 15 seconds maybe???.
//                new Handler().postDelayed(
//                        () -> {
//                            Intent selfIntent = new Intent(context, MainActivity.class);
//                            selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            context.startActivity(selfIntent);
//                            System.exit(0);
//                        }, TIME_TO_WAIT_AFTER_OPENING_MAP);
                break;
            } else if ("video".equals(sCase)) {
                containsKeyword = true;
                captureAndProcessVideo(cur_task_id, true);
                break;
            } else if ("notification".equals(sCase) || "notify".equals(sCase)) {
                containsKeyword = true;
                getNotificationsAndSpeak();
            } else if ("help".equals(sCase)) {
                containsKeyword = true;
                audioOutput.setSpeechRate(1.5f);
                audioOutput.speak(helpString, TypesOfSentences.TYPE_HELP, TextToSpeech.QUEUE_ADD, "help", () -> {
                    audioOutput.setSpeechRate(2f);
                });
                break;
            } else if ("test".equals(sCase) || "best".equals(sCase)) {
                containsKeyword = true;
                captureAndProcessImage(true);
                break;
            } else if ("setting".equals(sCase)) {
                containsKeyword = true;
                Intent settingsActivity = new Intent(context, SettingsActivity.class);
                context.startActivity(settingsActivity);
                break;
            } else if ("set".equals(sCase)) {
                containsKeyword = true;
                String lang = sLowerCasedTrimmed.replace("set language to ", "");
                if (Languages.hasLang(lang)) {
                    StaticSettings.lang = lang;
                    StaticSettings.setSettings("lang", lang, context);
                }
                break;
            } else if ("find".equals(sCase)) {
                containsKeyword = true;
                String labels = sLowerCasedTrimmed.substring(sLowerCasedTrimmed.indexOf("find") + 4);
                System.out.println("labels:" + labels);
                captureAndProcessVideoFind(cur_task_id, labels);
                break;
            }
        }
        if (!containsKeyword) {
            audioOutput.speak("Sorry I don't know that, speak help for more info!", TypesOfSentences.TYPE_NOT_KNOWN_COMMAND, TextToSpeech.QUEUE_ADD, "help", () -> {
            });
        }
    }

    private void captureAndProcessVideoFind(int cur_task_id, String labels) {
        curVideoPrecessingIndex = 0;
        safeCameraOpen(false);
        setCFrameSizeTo720p();
        int w = c.getParameters().getPreviewSize().width;
        int h = c.getParameters().getPreviewSize().height;
        focal_length = c.getParameters().getFocalLength();
        final boolean[] processingImage = {false};
        final int[] processRate = {2};
        final long[] count = {0};
        ProcessImage processImage = null;

        try {
            processImage = new ProcessImage(context, focal_length, w, h, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ProcessImage finalProcessImage = processImage;
        final long[] prevTime = new long[]{System.currentTimeMillis()};
        c.setPreviewCallback((data, camera) -> {
            System.out.println(data.length + " = " + (w * h));
            if (CUR_TASK_ID == cur_task_id) {
                if (count[0] % processRate[0] == 0) {
                    if (!processingImage[0]) {
                        try {
                            if ((System.currentTimeMillis() - prevTime[0]) > TIME_TO_WAIT_BETWEEN_IMAGES_IN_VIDEO) {
                                processingImage[0] = true;
                                prevTime[0] = System.currentTimeMillis();
                                if (finalProcessImage != null)
                                    finalProcessImage.startProcessing(data, (s) -> {
                                        processingImage[0] = false;
                                        mostRecentDetection[0] = s;
                                        if (!running) {
                                            running = true;
                                            speakMostRecentVideoDetection(labels);
                                        }
                                    }, false);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                count[0]++;
            } else {
                releaseCameraAndPreview();
                System.out.println("Releasing camera as other task is assigned");
                running = false;
            }
            if (running)
                camera.addCallbackBuffer(data);
        });

        c.startPreview();
        c.addCallbackBuffer(new byte[w * h]);

    }

    private void getNotificationsAndSpeak() {
        Intent notificationServiceIntent = new Intent(context, NotificationListener.class);

        BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String[] notifications = intent.getStringExtra("toSpeak").split("###");
                speakAllNotifications(notifications, 0, CUR_TASK_ID);
                context.stopService(notificationServiceIntent);
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(
                mMessageReceiver, new IntentFilter("NotificationParsed"));
        context.startService(notificationServiceIntent);
    }

    private void speakMostRecentVideoDetection(String labels) {
        if (running) {
            String[] allObjects = mostRecentDetection[0].split("##");
            int dectedIndex = 0;
            String[] detectedObjects = new String[allObjects.length];
            for (int i = 0; i < detectedObjects.length; i++) {
                if (allObjects[i].length() < 2) continue;
                String label = allObjects[i].substring(0, allObjects[i].indexOf(" "));
                label = label.trim();
                System.out.println("label: " + label + "labels " + labels + " contains " + labels.contains(label));
                if (labels.trim().equals("") || labels.contains(label)) {
                    detectedObjects[dectedIndex++] = allObjects[i];
                }
            }
            if (dectedIndex == 0) {

                if (MainActivity.mTextView != null)
                    MainActivity.mTextView.setText("no " + labels + " found");

                audioOutput.speak("no " + labels + " found", TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_ADD, "video",
                        () -> speakMostRecentVideoDetection(labels));
                return;
            }
            if (curVideoPrecessingIndex < dectedIndex)
                audioOutput.speak(detectedObjects[curVideoPrecessingIndex++], TypesOfSentences.TYPE_VIDEO_PROCESSED, TextToSpeech.QUEUE_ADD, "video", () -> speakMostRecentVideoDetection(labels));
            else {
                curVideoPrecessingIndex = 0;
                audioOutput.speak(detectedObjects[curVideoPrecessingIndex++], TypesOfSentences.TYPE_VIDEO_PROCESSED, TextToSpeech.QUEUE_ADD, "video", () -> speakMostRecentVideoDetection(labels));
            }
            if (MainActivity.mTextView != null)
                MainActivity.mTextView.setText(detectedObjects[curVideoPrecessingIndex - 1]);
        }
    }

    /**
     * Captures and process Image from Camera.
     */
    private void captureAndProcessImage(boolean freespace) {

        // width height to pass to Image Processor.
        int width, height;
        try {
            releaseCameraAndPreview();
            safeCameraOpen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (c != null) {
            setCFrameSizeTo720p();
            Camera.Parameters par = c.getParameters();
            try {
                par.set("orientation", "90");
                c.setParameters(par);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                par.set("rotation", "90");
                c.setParameters(par);
            } catch (Exception e) {
                e.printStackTrace();
            }
            c.setDisplayOrientation(90);
            width = c.getParameters().getPictureSize().width;
            height = c.getParameters().getPictureSize().height;
            c.startPreview();
            System.out.println(width + " x " + height);
            focal_length = c.getParameters().getFocalLength();

            System.out.println(focal_length);

            c.takePicture(null, null, (bytes, camera) -> {
                try {
                    new ProcessImage(context, bytes, focal_length, width, height, false).startProcessing(bytes, (str) -> {
                        if (MainActivity.mTextView != null)
                            MainActivity.mTextView.setText(str);

                        String[] strs = str.split("##");
                        int index = 0;
                        speakAllDetectedObjects(strs, index, CUR_TASK_ID);
                    }, freespace);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                c.release();
                System.out.println("Image Taken");
            }); // , 300);
        }
    }

    private void speakAllDetectedObjects(String[] strs, int index, int cur_task_id) {
        if (index >= strs.length) return;
        if (cur_task_id != CUR_TASK_ID) return;
        final int finalI = index;
        audioOutput.speak(strs[index],
                strs[index].contains("can't") ?
                        TypesOfSentences.TYPE_IMAGE_PROCESSED_NO_OBJECTS :
                        TypesOfSentences.TYPE_IMAGE_PROCESSED_SINGLE_OBJECT, TextToSpeech.QUEUE_ADD, "image_pr", () -> {
                    speakAllDetectedObjects(strs, finalI + 1, cur_task_id);
                });

    }

    private void speakAllNotifications(String[] strs, int index, int cur_task_id) {
        if (index >= strs.length) return;
        if (cur_task_id != CUR_TASK_ID) return;
        final int finalI = index;
        System.out.println(strs[finalI]);
        audioOutput.speak(strs[index],
                TypesOfSentences.TYPE_NOTIFICATION, TextToSpeech.QUEUE_ADD, "notify_pr", () -> {
                    speakAllNotifications(strs, finalI + 1, cur_task_id);
                });

    }

    // 1 2 4 8 9 11 14 15 17
    private void speakMostRecentVideoDetection() {
        if (running) {
            String[] detectedObjects = mostRecentDetection[0].split("##");
            if (curVideoPrecessingIndex < detectedObjects.length)
                audioOutput.speak(detectedObjects[curVideoPrecessingIndex++], TypesOfSentences.TYPE_VIDEO_PROCESSED, TextToSpeech.QUEUE_ADD, "video", this::speakMostRecentVideoDetection);
            else {
                curVideoPrecessingIndex = 0;
                audioOutput.speak(detectedObjects[curVideoPrecessingIndex++], TypesOfSentences.TYPE_VIDEO_PROCESSED, TextToSpeech.QUEUE_ADD, "video", this::speakMostRecentVideoDetection);
            }
            if (MainActivity.mTextView != null)
                MainActivity.mTextView.setText(detectedObjects[curVideoPrecessingIndex - 1]);
        }
    }

    /**
     * Starts capturing video and processing every 3rd frame.
     * ssd mobile net is predicting one image in 300ms. that's great time.
     */
    private void captureAndProcessVideo(int cur_task_id, boolean freespace) {
        curVideoPrecessingIndex = 0;
        safeCameraOpen(false);
        setCFrameSizeTo720p();
        int w = c.getParameters().getPreviewSize().width;
        int h = c.getParameters().getPreviewSize().height;
        focal_length = c.getParameters().getFocalLength();
        final boolean[] processingImage = {false};
        final int[] processRate = {2};
        final long[] count = {0};
        ProcessImage processImage = null;

        try {
            processImage = new ProcessImage(context, focal_length, w, h, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ProcessImage finalProcessImage = processImage;
        final long[] prevTime = new long[]{System.currentTimeMillis()};
        c.setPreviewCallback((data, camera) -> {
            System.out.println(data.length + " = " + (w * h));
            if (CUR_TASK_ID == cur_task_id) {
                if (count[0] % processRate[0] == 0) {
                    if (!processingImage[0]) {
                        try {
                            if ((System.currentTimeMillis() - prevTime[0]) > TIME_TO_WAIT_BETWEEN_IMAGES_IN_VIDEO) {
                                processingImage[0] = true;
                                prevTime[0] = System.currentTimeMillis();
                                if (finalProcessImage != null)
                                    finalProcessImage.startProcessing(data, (s) -> {
                                        processingImage[0] = false;
                                        mostRecentDetection[0] = s;
                                        if (!running) {
                                            running = true;
                                            speakMostRecentVideoDetection();
                                        }
                                    }, freespace);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                count[0]++;
            } else {
                releaseCameraAndPreview();
                System.out.println("Releasing camera as other task is assigned");
                running = false;
            }
            if (running)
                camera.addCallbackBuffer(data);
        });

        c.startPreview();
        c.addCallbackBuffer(new byte[w * h]);

    }

    private void setCFrameSizeTo720p() {
        if (c != null) {
            Camera.Parameters par = c.getParameters();

            // set picture size:
            for (Camera.Size size : par.getSupportedPictureSizes()) {
                // 640 480
                // 960 720 <- Use This Resolution If not present use default.
                // 1024 768
                // 1280 720
                // 1600 1200
                // 2560 1920
                // 3264 2448
                // 2048 1536
                // 3264 1836
                // 2048 1152
                // 3264 2176
                if (size.height == 720) {
                    par.setPictureSize(size.width, size.height);
                    par.setPreviewSize(size.width, size.height);
                    break;
                }
            }
            c.setParameters(par);
        }
    }

    /**
     * Gets Number from Name.
     *
     * @param name     String name
     * @param action   String action
     * @param callback NumberFromName callback.
     */
    private void getNumberFromName(String name, String action, NumberFromName callback) {
        System.out.println(name);
        if (name.trim().replaceAll(" ", "").matches("^[0-9+]+$")) {
            // Number is provided call it.
            System.out.println(name.trim().replaceAll(" ", ""));
            callNumberWithConfirm(name.trim().replace(" ", ""));
        }

        name = name.toLowerCase().trim().replaceAll(" ", "");

        String contactname, contactNumber;
        HashMap<String, String> hs = new HashMap<>();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);


        assert cursor != null;
        int idxName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int idxNumber = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        if (cursor.moveToFirst()) {
            do {
                contactname = cursor.getString(idxName);
                contactNumber = cursor.getString(idxNumber);

                if (contactname.toLowerCase().trim().replaceAll(" ", "").contains(name.toLowerCase())) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        hs.putIfAbsent(contactname, contactNumber);
                    } else {
                        if (!hs.containsKey(contactname)) {
                            hs.put(contactname, contactNumber);
                        }
                    }
                    Log.d("CONTACT", "Contact Name -> " + contactname + " Contact Number -> " + contactNumber);
                }
            } while (cursor.moveToNext());
        }
        System.out.println(Arrays.toString(hs.entrySet().toArray()));
        if (hs.size() == 0) {
            audioOutput.speak("Your Contact list don't have " + name + ", Please provide a number", TypesOfSentences.TYPE_CANT_FOUND_NUMBER, TextToSpeech.QUEUE_ADD, null, () -> {
            });
            callback.done("");
        } else if (hs.size() == 1) {
            for (String s : hs.keySet()) {
                callback.done(hs.get(s));
                return;
            }
        } else {
            int i = 1;
            StringBuilder toSpeak = new StringBuilder("There are multiple contacts, Which one you want to " + action + "? ");
            for (String s : hs.keySet()) {
                toSpeak.append(i).append(". ").append(s).append(". ");
                ++i;
            }
            System.out.println(toSpeak);

            audioOutput.speak(String.valueOf(toSpeak), TypesOfSentences.TYPE_MULTIPLE_CONTACTS, TextToSpeech.QUEUE_ADD, "multiple1", () -> mSpeechInput.startListening(name1 -> {
                System.out.println(name1);
                getNumberFromName(name1.toLowerCase().trim(), action, callback);
            }));
        }
        cursor.close();
    }

    private void safeCameraOpen(boolean setPreviewDisplay) {
        try {
            releaseCameraAndPreview();
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            if (setPreviewDisplay) {
                c.setPreviewDisplay(view.getHolder());
                c.setPreviewTexture(new SurfaceTexture(MODE_PRIVATE));
            }
        } catch (Exception e) {
            Log.e("blindpartner", "failed to open Camera");
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (c != null) {
            c.release();
            c = null;
            running = false;
        }
    }

    private void callNumberWithConfirm(String number) {
        audioOutput.speak("Do you want to call " + number.replace("", " "), TypesOfSentences.TYPE_CALL_CONFIRM, TextToSpeech.QUEUE_ADD, "confirm1", () -> {
            mSpeechInput.startListening((s1) -> {
                try {
                    if (s1.toLowerCase().trim().contains("yes") || s1.toLowerCase().trim().contains("yep")) {
                        Intent i = new Intent(Intent.ACTION_CALL);
                        i.setData(Uri.parse("tel:" + number.trim().replace(" ", "")));
                        if (checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PermissionChecker.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    Activity#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for Activity#requestPermissions for more details.
                            return;
                        }
                        context.startActivity(i);

                        new android.os.Handler().postDelayed(
                                () -> {
                                    Intent selfIntent = new Intent(context, MainActivity.class);
                                    selfIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(selfIntent);
                                },
                                1000);

                    } else {
                        audioOutput.speak("Canceling.", TypesOfSentences.TYPE_CALL_MESSAGE_CANCEL, TextToSpeech.QUEUE_ADD, "exit1", () -> {
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private void messageNumberWithConfirm(String number) {
        audioOutput.speak("What do you want to send?", TypesOfSentences.TYPE_MESSAGE_ASK_CONTENT, TextToSpeech.QUEUE_ADD, "msg_body", () -> mSpeechInput.startListening((data) -> audioOutput.speak("Do you want to message " + number.replace("", " "), TypesOfSentences.TYPE_MESSAGE_CONFIRM, TextToSpeech.QUEUE_ADD, "confirm1", () -> mSpeechInput.startListening((s1) -> {
            if (s1.toLowerCase().trim().contains("yes") || s1.toLowerCase().trim().contains("yep") || s1.toLowerCase().trim().contains("sure")) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(number, null, data, null, null);
            } else {
                audioOutput.speak("Cancelling.", TypesOfSentences.TYPE_CALL_MESSAGE_CANCEL, TextToSpeech.QUEUE_ADD, "exit1", () -> {
                });
            }
        }))));
    }

    public void destroy() {
        releaseCameraAndPreview();
        mSpeechInput.mSpeechRecognizer.stopListening();
        mSpeechInput.mSpeechRecognizer.destroy();
    }
}
