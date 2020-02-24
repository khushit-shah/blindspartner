package com.khushitshah.blindspartner.libs.Audio.Output;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.khushitshah.blindspartner.StaticSettings;
import com.khushitshah.blindspartner.libs.Language.Languages;
import com.khushitshah.blindspartner.libs.Language.Translator;
import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

import java.util.HashMap;
import java.util.Locale;

public class AudioOutput implements TextToSpeech.OnInitListener {
    private final String language = "en_IN";
    private TextToSpeech tts;
    private Context context;
    private AudioOutputInterface aoi;

    private boolean alreadySent = false;

    /**
     * The constructor for the TextToSpeech class, using the default TTS engine.
     * This will also initialize the associated TextToSpeech engine if it isn't already running.
     *
     * @param context The context this instance is running in.
     *                \     *                 TextToSpeech engine has initialized. In a case of a failure the listener
     */
    public AudioOutput(Context context, float speechRate) {
        tts = new TextToSpeech(context, this);
        System.out.println(tts.getDefaultEngine());
        // System.out.println(tts.setEngineByPackageName(tts.getDefaultEngine()));
        System.out.println(tts.getEngines());

        // set up indian accent.
        tts.setLanguage(new Locale(language));
        tts.setSpeechRate(speechRate);
        this.context = context;
    }

    public void setSpeechRate(float speeachRate) {
        tts.setSpeechRate(speeachRate);
    }


    public void onInit(int status) {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {


                try {
                    if (!alreadySent) ((Activity) context).runOnUiThread(aoi::spoken);
                } catch (Exception e) {
                    aoi.spoken();
                }
            }

            @Override
            public void onError(String utteranceId) {
            }
        });
//        System.out.println(tts.getAvailableLanguages());
        if (status == TextToSpeech.SUCCESS) {

            int ttsLang = tts.setLanguage(new Locale(language));

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                    || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language is not supported!");
            } else {
                Log.i("TTS", "Language Supported.");
            }
            Log.i("TTS", "Initialization success.");
        } else {
            Toast.makeText(context, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * runs <code>AudioOutputInterface.spoken()</code> on ui thread after the engine is done speaking text.
     */
    public void speak(String text, TypesOfSentences type, int queueFlush, String id, AudioOutputInterface o1) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
        Languages lang = Languages.ENGLISH;
        if (StaticSettings.lang.equalsIgnoreCase("english"))
            lang = Languages.ENGLISH;
        else if (StaticSettings.lang.equalsIgnoreCase("hindi"))
            lang = Languages.HINDI;
        else if (StaticSettings.lang.equalsIgnoreCase("gujarati"))
            lang = Languages.GUJARATI;


        Translator.translateTo(lang, type, text, context, this, (str) -> {
            System.out.println(str);
            tts.setSpeechRate(StaticSettings.speechRate);
            tts.speak(str, queueFlush, params);
            this.alreadySent = false;
            this.aoi = o1;
        });
    }

    public void setLanguage(String locale) {
        tts.setLanguage(Locale.forLanguageTag(locale));
    }


}

