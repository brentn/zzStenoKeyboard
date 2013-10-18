package com.brentandjody.stenokeyboard;

import android.app.Application;
import android.content.Context;

/**
 * Created by brent on 18/10/13.
 */
public class SKApplication extends Application {
    private static Context context;

    public void onCreate(){
        super.onCreate();
        SKApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SKApplication.context;
    }
}
