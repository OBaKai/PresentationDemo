package com.llk.pd;

import android.app.Presentation;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * author:
 * group:
 * createDate:
 * detail:
 */
public class LLkPresentation extends Presentation {

    public LLkPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.llk);
        Log.e("llk", "LLkPresentation onCreate");

        //将dialog的window提升到系统级
        //这样就可以使用ApplicationContext
        //android8.0以上无法使用TYPE_SYSTEM_ALERT
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            this.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
//        } else {
//            this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        }
    }
}
