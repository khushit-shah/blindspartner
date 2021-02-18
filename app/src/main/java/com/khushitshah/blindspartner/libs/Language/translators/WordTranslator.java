package com.khushitshah.blindspartner.libs.Language.translators;

import android.content.Context;
import android.util.Log;

import com.khushitshah.blindspartner.libs.Language.Languages;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class WordTranslator {
    static HashMap<String, String> map = new HashMap<>();
    Context context;
    static GujaratiWordTranslator gujaratiWordTranslator = null;

    WordTranslator(Context context) {
        this.context = context;
    }

    public static WordTranslator getTranslator(Languages languages, Context context) {
        if (languages == Languages.HINDI) {
            Log.e("LSB", "Hindi Not implemented yet");
        } else if (languages == Languages.ENGLISH) {
            Log.e("LSB", "English not implemented yet");
        } else if (languages == Languages.GUJARATI) {
            if (gujaratiWordTranslator == null)
                gujaratiWordTranslator = new GujaratiWordTranslator(context);
            return gujaratiWordTranslator;
        }
        return null;
    }

    protected void loadMap(String filePath, HashMap<String, String> map) {
        try {
            InputStream file = context.getAssets().open(filePath);
            InputStreamReader reader = new InputStreamReader(file, StandardCharsets.UTF_8);
            StringBuilder fileContent = new StringBuilder();

            int data;

            while ((data = reader.read()) != -1) {
                fileContent.append((char) data);
            }

            String[] lines = fileContent.toString().split("\n");

            for (String line : lines) {
                System.out.println(line);
                map.put(line.split(",")[0], line.split(",")[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract String translate(String key);


}
