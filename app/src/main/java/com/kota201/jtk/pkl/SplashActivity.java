package com.kota201.jtk.pkl;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.onesignal.OneSignal;

import butterknife.BindString;

/**
 * Created by AdeFulki on 5/27/2017.
 */

public class SplashActivity extends Activity{
    private static int SPLASH_TIME_OUT = 2000;

    @BindString(R.string.my_prefs) String my_prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                            SharedPreferences prefs = getSharedPreferences(my_prefs, MODE_PRIVATE);
                            String restoredText = prefs.getString("text", null);
                            if (restoredText != null) {
                                int role = prefs.getInt("role", 0);
                                if (role == 0){
                                    //tampilan pedagang
                                }else if (role == 1){
                                    //tampilan pembeli
                                }
                            }else {
                                startActivity(new Intent(SplashActivity.this, LokasiPedagangActivity.class));
                                finish();
                            }

                    }
                }, SPLASH_TIME_OUT);
    }
}
