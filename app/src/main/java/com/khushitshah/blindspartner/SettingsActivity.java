package com.khushitshah.blindspartner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import com.khushitshah.blindspartner.libs.Audio.Output.AudioOutput;
import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        MainActivity.isAppActive = true;
    }

    static float min = 0.3f;
    static float max = 2.0f;
    static float step = 0.1f;

    @Override
    protected void onResume() {
        MainActivity.isAppActive = true;
        stopService(new Intent(this, SpeechRecognizerBackGroundService.class));
        super.onResume();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> {
            if (key.equals("lang")) {
                StaticSettings.lang = sharedPreferences.getString("lang", "").toLowerCase();
            } else if (key.equals("emergency")) {
                StaticSettings.emergencyNumber = sharedPreferences.getString("emergency", "").toLowerCase();
            }
            Log.d("spchanged", "onSharedPreferenceChanged: " + key + " value = ");
//            StaticSettings.checkSettings(getContext());

        };

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            SeekBarPreference seekbar = getPreferenceManager().findPreference("speechRate1");
            seekbar.setMax((int) ((max - min) / step));
            seekbar.setOnPreferenceChangeListener(
                    (preference, newValue) -> {
                        float value = min + step * seekbar.getValue();
                        StaticSettings.speechRate = value;
                        System.out.println("speechRate = " + value);
                        StaticSettings.setSettings("speechRateCust", value, getContext());
                        return true;
                    }
            );

            CheckBoxPreference onlineTranslation = findPreference("trans");
            onlineTranslation.setOnPreferenceClickListener(preference -> {
                StaticSettings.onlineTranslation = onlineTranslation.isChecked();
                StaticSettings.setSettings("trans", StaticSettings.onlineTranslation, getContext());
                return true;
            });

            Preference button = findPreference("testBtn");
            button.setOnPreferenceClickListener(preference -> {
                new AudioOutput(getContext().getApplicationContext(), StaticSettings.speechRate).speak("This is a test sentence", TypesOfSentences.TYPE_TEST, TextToSpeech.QUEUE_FLUSH, "test", () -> {
                });
                return true;
            });
        }


        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }

    }

}


