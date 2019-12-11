package com.llk.pd;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.MediaRouter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * author:
 * group:
 * createDate:
 * detail:
 */
public class PService extends Service {

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Toast.makeText(App.getCtx(), (String) msg.obj, Toast.LENGTH_SHORT).show();
        }
    };

    private void aa(){
        DisplayManager displayManager = (DisplayManager) App.getCtx().getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = displayManager.getDisplays();

        Message msg = new Message();
        msg.what = 1;
        if(displays == null){
            msg.obj = "display is null";
            return;
        }

        String str = "";
        for (Display dp : displays){
            str += "dp=" + dp.getName() + " ";
        }

        msg.obj = str;
        handler.sendMessage(msg);
    }

    private void bb(){
        MediaRouter mediaRouter = (MediaRouter) App.getCtx().getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo info = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        Message msg = new Message();
        msg.what = 1;
        if(info == null){
            msg.obj = "RouteInfo is null";
            return;
        }

        String str;
        if(info.getPresentationDisplay() != null){
            str = "ri dp=" + info.getPresentationDisplay().getName();
        }else {
            str = "ri dp is null";
        }

        msg.obj = str;
        handler.sendMessage(msg);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("llk", "service onCreate");


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    aa();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    bb();
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("llk", "service onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
