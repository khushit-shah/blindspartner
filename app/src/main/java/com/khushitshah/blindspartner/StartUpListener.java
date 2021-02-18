package com.khushitshah.blindspartner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class StartUpListener extends BroadcastReceiver {

    /**
     * It runs when device boots up. It will run Blind's Partner on startup!.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Starting Blind's Partner", Toast.LENGTH_LONG).show();
//        Intent i = new Intent(context, MainActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(i);

        context.startService(new Intent(context.getApplicationContext(), SpeechRecognizerBackGroundService.class));
    }
}
