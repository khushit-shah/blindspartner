package com.khushitshah.blindspartner.libs.Language;

public enum Languages {
    HINDI,
    ENGLISH,
    GUJARATI,
    ;

    public static boolean hasLang(String lang) {
        switch (lang) {
            case "gujarati":
            case "hindi":
            case "english":
                return true;
        }

        return false;
    }
}