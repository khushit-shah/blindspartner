package com.khushitshah.blindspartner.libs.notification;

import java.io.Serializable;

public interface NotificationCallback extends Serializable {
    void callback(String toSpeak);
}
