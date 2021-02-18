package com.khushitshah.blindspartner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaButtonIntentReceiv";
    private static boolean isListening = false;

    public MediaButtonIntentReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("NOT WORKING");
        String intentAction = intent.getAction();
        Toast.makeText(context, "BUTTON PRESSED!", Toast.LENGTH_SHORT).show();

        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }

        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN) {
            Log.e(TAG, "onReceive: ");
            Toast.makeText(context, "BUTTON PRESSED!", Toast.LENGTH_SHORT).show();
            isListening = true;
            // TODO: start listening here!

        } else if (action == KeyEvent.ACTION_UP) {
            if (isListening) {
                isListening = false;
                // TODO: stop listening here!
            }
        }
        abortBroadcast();
    }
}
