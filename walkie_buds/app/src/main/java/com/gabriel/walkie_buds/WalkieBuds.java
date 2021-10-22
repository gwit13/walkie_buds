package com.gabriel.walkie_buds;

import android.app.Application;
import android.content.Context;

public class WalkieBuds extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        WalkieBuds.context = getApplicationContext();
    }

    public static Context getAppContext(){
        return WalkieBuds.context;
    }
}
