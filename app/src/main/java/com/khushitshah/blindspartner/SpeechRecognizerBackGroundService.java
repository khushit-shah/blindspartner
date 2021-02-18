package com.khushitshah.blindspartner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.widget.Toast;

import com.khushitshah.blindspartner.libs.Audio.Input.SpeechInput;
import com.khushitshah.blindspartner.libs.Audio.Input.SpeechInputInterface;

import java.util.Objects;
import java.util.Random;

public class SpeechRecognizerBackGroundService extends Service implements SpeechInputInterface {

    private SpeechInput speechInput;
    private ParseInputAndTakeAction parseInputAndTakeAction;
    private boolean stop = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (speechInput != null) {
            speechInput.mSpeechRecognizer.stopListening();
            speechInput.mSpeechRecognizer.destroy();
        }

        speechInput = new SpeechInput(this);
        //TODO do something useful
        try {
            ((AudioManager) Objects.requireNonNull(
                    getSystemService(Context.AUDIO_SERVICE))).setStreamMute(AudioManager.STREAM_SYSTEM, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        muteBeepSoundOfRecorder();
        speechInput.startListening(this);
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    @Override
    public void onDestroy() {
        stop = true;
        if (speechInput != null) {
            speechInput.mSpeechRecognizer.stopListening();
            speechInput.mSpeechRecognizer.destroy();
        }
        Toast.makeText(getApplicationContext(), "Stoping it!!", Toast.LENGTH_LONG).show();
        unmuteBeepSoundOfRecorder();
        super.onDestroy();
    }

    /**
     * Function to remove the beep sound of voice recognizer.
     */
    private void muteBeepSoundOfRecorder() {
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (amanager != null) {
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    private void unmuteBeepSoundOfRecorder() {
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (amanager != null) {
            amanager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, amanager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), AudioManager.FLAG_SHOW_UI);
            amanager.setStreamVolume(AudioManager.STREAM_ALARM, amanager.getStreamMaxVolume(AudioManager.STREAM_ALARM), AudioManager.FLAG_SHOW_UI);
            amanager.setStreamVolume(AudioManager.STREAM_MUSIC, amanager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_SHOW_UI);
            amanager.setStreamVolume(AudioManager.STREAM_RING, amanager.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_SHOW_UI);
            amanager.setStreamVolume(AudioManager.STREAM_SYSTEM, amanager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), AudioManager.FLAG_SHOW_UI);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
//        Restarting the service if it is removed.
//        Only Restart service if MainActivity is closed or paused;
        if (!MainActivity.isAppActive) {
            Toast.makeText(getApplicationContext(), "Restarting Background Listening Service", Toast.LENGTH_LONG).show();
            PendingIntent service =
                    PendingIntent.getService(getApplicationContext(), new Random().nextInt(),
                            new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class), PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            assert alarmManager != null;
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, service);
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void SpeechResults(String result) {
        if (stop) return;
        if (result.toLowerCase().contains("open blind")) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
//      parseInputAndTakeAction.parseAudioInput(result);
        Toast.makeText(getApplicationContext(), "Result + " + result, Toast.LENGTH_LONG).show();
        muteBeepSoundOfRecorder();
        speechInput.startListening(this);

    }
}