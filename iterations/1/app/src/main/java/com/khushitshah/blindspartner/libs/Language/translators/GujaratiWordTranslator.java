package com.khushitshah.blindspartner.libs.Language.translators;

import android.content.Context;
import android.util.Log;

public class GujaratiWordTranslator extends WordTranslator {

    private static final String filepath = "gujarati.csv";

    GujaratiWordTranslator(Context context) {
        super(context);
        loadMap(filepath, map);
    }

    @Override
    public String translate(String key) {
        String res = map.get(key);
        if (res == null) {
            Log.e("WordTranslator", "No translation found for! " + key);
            return key;
        }
        System.out.println("translation of " + key + " = " + res);
        return res;
    }
}
