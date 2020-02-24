package com.khushitshah.blindspartner.libs.Language.langs;

import android.util.Log;

import com.khushitshah.blindspartner.libs.Language.Languages;
import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

public abstract class LanguagesSentencesBluePrint {
    public String Locale = "";

    public static LanguagesSentencesBluePrint getLangaugesSentecesBluePrint(Languages languages) {
        if (languages == Languages.HINDI) {
            Log.e("LSB", "Hindi Not implemented yet");
        } else if (languages == Languages.ENGLISH) {
            Log.e("LSB", "English not implemented yet");
        } else if (languages == Languages.GUJARATI) {
            return new GujaratiSentencesBluePrint();
        }
        return null;
    }

    public abstract String getSentenceBluePrint(TypesOfSentences type);


}
