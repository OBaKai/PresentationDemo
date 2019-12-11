package com.llk.pd;

import android.app.Application;
import android.content.Context;

/**
 * author:
 * group:
 * createDate:
 * detail:
 */
public class App extends Application {

    private static Context mC = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mC = this;
    }

    public static Context getCtx(){
        return mC;
    }
}
