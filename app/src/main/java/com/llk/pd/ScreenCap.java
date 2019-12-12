package com.llk.pd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;

/**
 * author:
 * group:
 * createDate:
 * detail:
 */
public class ScreenCap implements ImageReader.OnImageAvailableListener{

    private ImageReader imageReader;
    private Surface mSurface;
    private DisplayManager mDisplayManager;

    public static final int WIDTH = 480;
    public static final int HEIGHT = 720;

    private DisplayMetrics metrics;

    private LLkPresentation presentation;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;

    private VirtualDisplay virtualDisplay;

    private Activity context;

    private ScreenCapCallback screenCapCallback;

    public interface ScreenCapCallback{
        void onCap(Image image);
    }

    public ScreenCap(Activity ctx){
        context = ctx;

        metrics = context.getResources().getDisplayMetrics();
        mDisplayManager = (DisplayManager)context.getSystemService(Context.DISPLAY_SERVICE);
    }

    public ScreenCap(Activity ctx, ScreenCapCallback capCallback){
        this(ctx);
        screenCapCallback = capCallback;
    }

    /**
     * 通过DisplayManager创建VirtualDisplay
     * 该方法无需弹出权限框提醒
     * 但是只能截取到自己进程的东西，比如自己写的presentation。（该逻辑应该可以实现双屏异显的截屏吧）
     */
    public void dmScreenCap(){
        reset();

        //使用DisplayManager创建VirtualDisplay，可以规避弹框询问。
        imageReader = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(this, null);
        mSurface = imageReader.getSurface();
        virtualDisplay = mDisplayManager.createVirtualDisplay("dm_vd",
                WIDTH,
                HEIGHT,
                metrics.densityDpi,
                mSurface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                vDCallback,
                null);

        presentation = new LLkPresentation(context, virtualDisplay.getDisplay());
        presentation.show();
    }

    /**
     * MediaProjection创建VirtualDisplay
     * 该方法需弹出权限框提醒
     * 可以实现系统截屏
     */
    public void mpScreenCap(){
        reset();

        //使用MediaProjection创建VirtualDisplay，需要弹框询问。
        projectionManager = (MediaProjectionManager) App.getCtx().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        context.startActivityForResult(captureIntent, 110);
    }

    public void mp_onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if(requestCode == 110){
            Log.e("llk", "onActivityResult: 110");
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            //利用imageReader截屏
            imageReader = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 2);
            imageReader.setOnImageAvailableListener(this, null);
            //传imageReader的Surface给display,就可以收到截屏数据
            mSurface = imageReader.getSurface();
            VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("mp_vd",
                    WIDTH,
                    HEIGHT,
                    metrics.densityDpi,
                    DisplayMetrics.DENSITY_MEDIUM,
                    mSurface,
                    vDCallback,
                    null);
//            presentation = new LLkPresentation(this, virtualDisplay.getDisplay());
//            presentation.show();
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
//        if(image == null || image.getPlanes() == null){
//            Log.e("llk", "onImageAvailable fail!!!");
//            return;
//        }
//        Log.e("llk", "onImageAvailable: planes size=" + image.getPlanes().length);
//        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
//        byte[] bytes = new byte[byteBuffer.remaining()];
//        byteBuffer.get(bytes);

        //do something
        if(screenCapCallback != null){
            screenCapCallback.onCap(image);
        }


        image.close();
    }

    private VirtualDisplay.Callback vDCallback = new VirtualDisplay.Callback() {
        @Override
        public void onPaused() {
            super.onPaused();
            Log.e("llk", "VirtualDisplay onPaused: ");
        }

        @Override
        public void onResumed() {
            super.onResumed();
            Log.e("llk", "VirtualDisplay onResumed: ");
        }

        @Override
        public void onStopped() {
            super.onStopped();
            Log.e("llk", "VirtualDisplay onStopped: ");
        }
    };

    public void reset(){
        if(presentation != null){
            presentation.dismiss();
            presentation = null;
        }

        if (virtualDisplay != null){
            virtualDisplay.release();
            virtualDisplay = null;
        }

        if(imageReader != null){
            imageReader.close();
            imageReader = null;
            mSurface = null;
        }
    }
}
