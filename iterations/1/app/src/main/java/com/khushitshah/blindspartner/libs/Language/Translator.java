package com.khushitshah.blindspartner.libs.Language;

import android.content.Context;
import android.util.Log;

import com.khushitshah.blindspartner.StaticSettings;
import com.khushitshah.blindspartner.libs.Audio.Output.AudioOutput;
import com.khushitshah.blindspartner.libs.Language.langs.LanguagesSentencesBluePrint;
import com.khushitshah.blindspartner.libs.Language.translators.WordTranslator;
import com.khushitshah.blindspartner.libs.Utils.FetchJsonFromUrl;
import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class Translator {

    public static void translateTo(Languages lang, TypesOfSentences type, String text, Context context, AudioOutput aout, TranslatorInterface ti) {
        if (StaticSettings.onlineTranslation) {
            if (type == TypesOfSentences.TYPE_VIDEO_PROCESSED) {
                offlineTranslation(lang, type, text, context, aout, ti);
                return;
            }
            onlineTranslation(lang, type, text, context, aout, ti);
        } else {
            offlineTranslation(lang, type, text, context, aout, ti);
        }
    }

    private static void onlineTranslation(Languages lang, TypesOfSentences type, String text, Context context, AudioOutput aout, TranslatorInterface ti) {
        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=$1&dt=t&q=$2";

        LanguagesSentencesBluePrint languagesSentencesBluePrint = LanguagesSentencesBluePrint.getLangaugesSentecesBluePrint(lang);
        if (languagesSentencesBluePrint == null) {
            Log.e("Translator", "no language blueprint for " + lang);
            ti.translated("");
            return;
        }

        try {
            String locale = languagesSentencesBluePrint.Locale;
            String requestUrl = url;
            requestUrl = requestUrl.replace("$1", locale);
            requestUrl = requestUrl.replace("$2", URLEncoder.encode(text, "UTF-8"));
            System.out.println("requestUrl = " + requestUrl);
            FetchJsonFromUrl.fetchArray(requestUrl, (json) -> {
                System.out.println(json);
                try {
                    if (json instanceof String) {
                        if (json.equals("error")) {
                            offlineTranslation(lang, type, text, context, aout, ti);
                            return;
                        }
                    }
                    if (json == null) {
                        offlineTranslation(lang, type, text, context, aout, ti);
                        return;
                    }
                    String translated = ((JSONArray) json).getJSONArray(0).getJSONArray(0).getString(0);
                    ti.translated(translated);
                } catch (JSONException e) {
                    offlineTranslation(lang, type, text, context, aout, ti);
                    e.printStackTrace();
                }
            });

        } catch (UnsupportedEncodingException e) {
            offlineTranslation(lang, type, text, context, aout, ti);
            e.printStackTrace();
        }
    }

    private static void offlineTranslation(Languages lang, TypesOfSentences type, String text, Context context, AudioOutput aout, TranslatorInterface ti) {
        if (lang == Languages.ENGLISH) {
            ti.translated(text);
            return;
        }

        if (type == TypesOfSentences.TYPE_HELP) {
            ti.translated(HelpString.getHelpString(lang));
            return;
        }

        LanguagesSentencesBluePrint languagesSentencesBluePrint = LanguagesSentencesBluePrint.getLangaugesSentecesBluePrint(lang);
        WordTranslator translator = WordTranslator.getTranslator(lang, context);

        if (languagesSentencesBluePrint == null) {
            Log.e("Translator", "no language blueprint for " + lang);
            ti.translated("");
            return;
        }
        aout.setLanguage(languagesSentencesBluePrint.Locale);

        ti.translated(LineSequencer.sequence(TypeOfSentencesToSentenceBluePrint.getBlueprint(type),
                languagesSentencesBluePrint.getSentenceBluePrint(type),
                text,
                translator));
    }

}
