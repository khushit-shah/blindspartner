package com.khushitshah.blindspartner.libs.travelling;

public interface TravellingInterface {
    void getTravelMode(String[][] modesWithDistance, TravelModeReturnInterface returnInterface);

    void speakUpdate(String updateSentence);
}
