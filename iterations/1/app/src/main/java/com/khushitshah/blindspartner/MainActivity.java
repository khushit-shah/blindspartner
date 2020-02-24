package com.khushitshah.blindspartner;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.khushitshah.blindspartner.libs.Audio.Input.SpeechInput;
import com.khushitshah.blindspartner.libs.Audio.Output.AudioOutput;


interface NumberFromName {
    void done(String number);
}

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    public static ImageButton mSpeechInputButton = null;
    private static final Intent[] AUTO_START_INTENTS = {
            new Intent().setComponent(new ComponentName("com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(
                    Uri.parse("mobilemanager://function/entry/AutoStart"))
    };


    public static TextView mTextView;
    public static boolean isAppActive = false;
    SurfaceView view;
    private AudioOutput audioOutput;
    private SpeechInput mSpeechInput;
    private ParseInputAndTakeAction parseEngine;

    /**
     * Checks if given permission is given or not?
     *
     * @param context     Main activity context
     * @param permissions permissions String
     * @return
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isAppActive = true;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams layout = getWindow().getAttributes();
        layout.screenBrightness = 0.1F;
        getWindow().setAttributes(layout);
        try {
            this.getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());



        view = new SurfaceView(this);

//        view = new SurfaceView(this);


        audioOutput = new AudioOutput(this, 2f);
        setContentView(R.layout.activity_main);
        doPermALL();

        mTextView = findViewById(R.id.text);
        mSpeechInput = new SpeechInput(this);

        mSpeechInputButton = findViewById(R.id.speechInputButton);
        mSpeechInputButton.setOnTouchListener(this);
        audioOutput.setSpeechRate(1.5f);
        // Credits Button
//        Button button = findViewById(R.id.credits_button);
//        button.setOnClickListener(
//                view -> audioOutput.speak("Credits to Google Speech A P Is  and news a p i .org. Made with love by Khushit Shah!", TypesOfSentences.TYPE_CREDITS, TextToSpeech.QUEUE_FLUSH, "credits", () -> {
//                }));

        ImageButton setting = findViewById(R.id.settingsBtn);
        setting.setOnClickListener(view -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        });

        //        Set the priority of broadcast receiver!
        IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        MediaButtonIntentReceiver r = new MediaButtonIntentReceiver();
        filter.setPriority(1000);  //this line sets receiver priority
        registerReceiver(r, filter);
        StaticSettings.setSettingValues(getApplicationContext());
        parseEngine = new ParseInputAndTakeAction(this);

        Log.d("onCreate", "Stopiing!!!");
        Toast.makeText(this, "Stopping Speech listener", Toast.LENGTH_LONG).show();
        stopService(new Intent(this, SpeechRecognizerBackGroundService.class));
        mSpeechInput = new SpeechInput(this);
    }


    /**
     * Checks if all necessary permissions are present or not?
     */
    private void doPermALL() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                NOTIFICATION_SERVICE
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        for (Intent intent : AUTO_START_INTENTS) {
            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                startActivity(intent);
                break;
            }
        }
    }

//        for (Intent intent : Constants.AUTO_START_INTENTS) {
//            if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
//                new Builder(this).title(R.string.enable_autostart)
//                        .content(R.string.ask_permission)
//                        .theme(Resources.Theme.LIGHT)
//                        .positiveText(getString(R.string.allow))
//                        .onPositive((dialog, which) -> {
//                            try {
//                                for (Intent intent1 : Constants.AUTO_START_INTENTS)
//                                    if (getPackageManager().resolveActivity(intent1, PackageManager.MATCH_DEFAULT_ONLY)
//                                            != null) {
//                                        startActivity(intent1);
//                                        break;
//                                    }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        })
//                        .show();
//                break;
//            }
//        }
//    }


    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mSpeechInputButton.setImageDrawable(getDrawable(R.drawable.speech2));
                mSpeechInput.mSpeechRecognizer.stopListening();
                break;
            case MotionEvent.ACTION_DOWN:
                mSpeechInputButton.setImageDrawable(getDrawable(R.drawable.speech));
                mSpeechInput.startListening((s) -> {
                    mTextView.setText(s);
                    parseEngine.parseAudioInput(s);
                });
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Toast.makeText(this, "key code :" + keyCode, Toast.LENGTH_LONG).show();
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                mSpeechInput.startListening((s) -> {
                    mTextView.setText(s);
                    parseEngine.parseAudioInput(s);
                });
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        isAppActive = false;
        mSpeechInput.mSpeechRecognizer.stopListening();
        mSpeechInput.mSpeechRecognizer.destroy();
        startService(new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class));
        super.onStop();
    }


    @Override
    protected void onResume() {
        isAppActive = true;
        Log.d("OnResume", "Stopiing!!!");
        Toast.makeText(this, "Stopping Speech listener", Toast.LENGTH_LONG).show();
        stopService(new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class));
        mSpeechInput = new SpeechInput(this);
        super.onResume();

    }

    @Override
    protected void onPause() {
        isAppActive = false;
        mSpeechInput.mSpeechRecognizer.stopListening();
        mSpeechInput.mSpeechRecognizer.destroy();
//        startService(new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class));
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        isAppActive = false;
        parseEngine.destroy();
        mSpeechInput.mSpeechRecognizer.stopListening();
        mSpeechInput.mSpeechRecognizer.destroy();
        stopService(new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class));
        Toast.makeText(getApplicationContext(), "Starting Speech listener", Toast.LENGTH_LONG).show();
        startService(new Intent(getApplicationContext(), SpeechRecognizerBackGroundService.class));
        super.onDestroy();
    }
}
// TODO: Add Notification Listener!
