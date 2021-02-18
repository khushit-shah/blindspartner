package com.khushitshah.blindspartner.libs.Language.langs;

import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

import java.util.HashMap;

public class GujaratiSentencesBluePrint extends LanguagesSentencesBluePrint {
    private static final HashMap<TypesOfSentences, String> sentencesBluePrint = new HashMap<>();

    static {
        sentencesBluePrint.put(TypesOfSentences.TYPE_CALL_CONFIRM, "શું તમે $1 કૉલ કરવા માંગો છો?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MESSAGE_CONFIRM, "શું તમે $1 મેસેજ કરવા માંગો છો?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MESSAGE_ASK_CONTENT, "તમે શું મોકલવા માંગો છો?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_SOS_MSG_INFORM, "Messaging and calling emergency number");
        sentencesBluePrint.put(TypesOfSentences.TYPE_SOS_MSG_INFORM2, "સંદેશ મોકલ્યો! હવે કૉલ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_LOCATION, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_FETCHING, "$1 લાવી રહ્યા છીએ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CANT_FETCH, "$1 પ્રાપ્ત કરી શકતું નથી, પછીથી ફરી પ્રયાસ કરો!");
        sentencesBluePrint.put(TypesOfSentences.TYPE_HELP, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NOTIFICATION, "$1 દ્વારા, શીર્ષક $2 સાથે, ટેક્સ્ટ $3 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NOT_KNOWN_COMMAND, "માફ કરશો હું તે જાણતો નથી, વધુ માહિતી માટે મદદ બોલો!");
        sentencesBluePrint.put(TypesOfSentences.TYPE_IMAGE_PROCESSED_NO_OBJECTS, "તમારી સામેની કોઈપણ objects શોધી sakto નથી");
        sentencesBluePrint.put(TypesOfSentences.TYPE_VIDEO_PROCESSED, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CREDITS, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_TIME, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_IMAGE_PROCESSED_SINGLE_OBJECT, "તમારી સામે એક $2 $3 પગલાં દૂર છે.");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NEWS, "$1 ");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CALL_MESSAGE_CANCEL, "રદ.");
        sentencesBluePrint.put(TypesOfSentences.TYPE_STOP, "રોકી.");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CANT_FOUND_NUMBER, "તમારી સંપર્ક સૂચિમાં $1 નથી, કૃપા કરીને એક નંબર પ્રદાન કરો");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MULTIPLE_CONTACTS, "બહુવિધ સંપર્કો છે, તમે કયામાંથી $1 to કરવા માંગો છો?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_TEST, "આ એક પરીક્ષણ વાક્ય છે");
    }

    GujaratiSentencesBluePrint() {
        Locale = "gu_IN";
    }

    @Override
    public String getSentenceBluePrint(TypesOfSentences type) {
        return sentencesBluePrint.get(type);
    }
}
