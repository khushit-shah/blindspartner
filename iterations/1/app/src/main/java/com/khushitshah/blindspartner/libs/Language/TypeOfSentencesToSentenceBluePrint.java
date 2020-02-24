package com.khushitshah.blindspartner.libs.Language;

import com.khushitshah.blindspartner.libs.Utils.TypesOfSentences;

import java.util.HashMap;

public class TypeOfSentencesToSentenceBluePrint {
    public static final HashMap<TypesOfSentences, String> sentencesBluePrint = new HashMap<>();

    static {
        sentencesBluePrint.put(TypesOfSentences.TYPE_CALL_CONFIRM, "Do you want to call $");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MESSAGE_CONFIRM, "Do you want to message $");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MESSAGE_ASK_CONTENT, "What do you want to send?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_SOS_MSG_INFORM, "Messaging and calling emergency number");
        sentencesBluePrint.put(TypesOfSentences.TYPE_SOS_MSG_INFORM2, "Message Sent! Now Calling");
        sentencesBluePrint.put(TypesOfSentences.TYPE_LOCATION, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_FETCHING, "fetching $");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CANT_FETCH, "can't fetch $ try again later!");
        sentencesBluePrint.put(TypesOfSentences.TYPE_HELP, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NOTIFICATION, "By $, $, $");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NOT_KNOWN_COMMAND, "Sorry I don't know that, speak help for more info!");
        sentencesBluePrint.put(TypesOfSentences.TYPE_IMAGE_PROCESSED_NO_OBJECTS, "Can't detect any objects in front of you");
        sentencesBluePrint.put(TypesOfSentences.TYPE_VIDEO_PROCESSED, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CREDITS, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_TIME, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_IMAGE_PROCESSED_SINGLE_OBJECT, "$ a $ , $ steps away in front of you.");
        sentencesBluePrint.put(TypesOfSentences.TYPE_NEWS, "$");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CALL_MESSAGE_CANCEL, "Cancelling.");
        sentencesBluePrint.put(TypesOfSentences.TYPE_STOP, "Stopped");
        sentencesBluePrint.put(TypesOfSentences.TYPE_CANT_FOUND_NUMBER, "Your Contact list don't have $ , Please provide a number");
        sentencesBluePrint.put(TypesOfSentences.TYPE_MULTIPLE_CONTACTS, "There are multiple contacts, Which one you want to $ ?");
        sentencesBluePrint.put(TypesOfSentences.TYPE_TEST, "This is a test sentence");
    }


    public static String getBlueprint(TypesOfSentences key) {
        return sentencesBluePrint.get(key);
    }
}
