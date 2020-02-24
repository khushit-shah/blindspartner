package com.khushitshah.blindspartner.libs.Audio.Input;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.khushitshah.blindspartner.MainActivity;
import com.khushitshah.blindspartner.R;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechInput {

    private static String result = null;
    private final Intent mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    public final SpeechRecognizer mSpeechRecognizer;
    private SpeechInputInterface sii;
    private Context context;
    public SpeechInput(Context activity) {
        this.context = activity;
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        Log.d("Locale", Locale.getDefault().toLanguageTag());
        mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {
//                sii.SpeechResults("" + i);
                runOnUITHREAD(() -> {
                    if (MainActivity.mSpeechInputButton != null)
                        MainActivity.mSpeechInputButton.setImageDrawable(context.getDrawable(R.drawable.speech2));
                });
            }

            @Override
            public void onResults(Bundle bundle) {
                runOnUITHREAD(() -> {
                    if (MainActivity.mSpeechInputButton != null)
                        MainActivity.mSpeechInputButton.setImageDrawable(context.getDrawable(R.drawable.speech2));
                });
                ArrayList<String> matches = bundle
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                result = matches.get(0);

                try {
                    sii.SpeechResults(result);
                } catch (Exception e) {
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });
    }

    public void startListening(SpeechInputInterface sii) {
        this.sii = sii;

        runOnUITHREAD(() -> {
            if (MainActivity.mSpeechInputButton != null)
                MainActivity.mSpeechInputButton.setImageDrawable(context.getDrawable(R.drawable.speech));
        });
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
    }


    private void runOnUITHREAD(Runnable r) {
        try {
            ((Activity) context).runOnUiThread(r);
        } catch (Exception e) {

        }
    }

}