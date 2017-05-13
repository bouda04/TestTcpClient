package com.example.bouda04.testtcpclient;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 * Created by bouda04 on 20/2/2017.
 */

public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
        Log.d("TctTest", "MyApplication-onCreate");
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        Log.d("TctTest", "MyApplication-onTerminate");
        super.onTerminate();
    }

    public static Context getContext(){
        return context;
    }
}
