package com.llk.pd;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {


    private ImageReader imageReader;
    private Surface mSurface;
    private DisplayManager mDisplayManager;

    private static final int WIDTH = 480;
    private static final int HEIGHT = 720;

    private DisplayMetrics metrics;

    private LLkPresentation presentation;

    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;

    private VirtualDisplay virtualDisplay;
    
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            
            Bitmap bitmap = (Bitmap) msg.obj;
            if(bitmap != null){
                iv.setImageBitmap(bitmap);
            }
        }
    };

    private void makeBitmap(Image img){
        Image.Plane[] planes = img.getPlanes();
        if (planes[0].getBuffer() == null) {
            return;
        }
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * WIDTH;

        Bitmap bitmap = Bitmap.createBitmap(WIDTH + rowPadding / pixelStride, HEIGHT, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        Message message = new Message();
        message.obj = bitmap;
        message.what = 1;
        handler.sendMessage(message);
    }

    private ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv = findViewById(R.id.iv);

        Intent intent = new Intent(this, PService.class);
        startService(intent);

        metrics = getResources().getDisplayMetrics();
        mDisplayManager = (DisplayManager)App.getCtx().getSystemService(Context.DISPLAY_SERVICE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 110){
            Log.e("llk", "onActivityResult: 110");
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            //利用imageReader截屏
            imageReader = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 2);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.e("llk", "onImageAvailable");
                    //acquireLatestImage拿到最后一张截屏数据
                    try (Image image = reader.acquireLatestImage()) {
                        if (image != null) {
                            //将image转化成bitmap
                            makeBitmap(image);
                        }

                    } catch (Exception e) {
                        Log.e("llk", "onImageAvailable: error=" + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                }
            }, null);
            //传imageReader的Surface给display,就可以收到截屏数据
            mSurface = imageReader.getSurface();
            VirtualDisplay virtualDisplay = mediaProjection.createVirtualDisplay("mp_vd",
                    WIDTH,
                    HEIGHT,
                    metrics.densityDpi,
                    DisplayMetrics.DENSITY_MEDIUM,
                    mSurface,
                    vc,
                    null);
//            presentation = new LLkPresentation(this, virtualDisplay.getDisplay());
//            presentation.show();
        }
    }

    private void reset(){
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

    /**
     * 通过DisplayManager创建VirtualDisplay
     * 该方法无需弹出权限框提醒
     * 但是只能截取到自己进程的东西，比如自己写的presentation。（该逻辑应该可以实现双屏异显的截屏吧）
     */
    public void dm_click(View view) {
        reset();

        //使用DisplayManager创建VirtualDisplay，可以规避弹框询问。
        imageReader = ImageReader.newInstance(WIDTH, HEIGHT, PixelFormat.RGBA_8888, 2);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.e("llk", "onImageAvailable");
                try (Image image = reader.acquireLatestImage()) {
                    if (image != null) {
                        makeBitmap(image);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, null);
        mSurface = imageReader.getSurface();
        virtualDisplay = mDisplayManager.createVirtualDisplay("dm_vd",
                WIDTH,
                HEIGHT,
                metrics.densityDpi,
                mSurface,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                vc,
                null);
        presentation = new LLkPresentation(this, virtualDisplay.getDisplay());
        presentation.show();
    }

    /**
     * MediaProjection创建VirtualDisplay
     * 该方法需弹出权限框提醒
     * 可以实现系统截屏
     */
    public void mp_click(View view) {
        reset();

        //使用MediaProjection创建VirtualDisplay，需要弹框询问。
        projectionManager = (MediaProjectionManager) App.getCtx().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = projectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, 110);
    }

    public void cs_click(View view) {
        stopService(new Intent(this, PService.class));
    }

    private VirtualDisplay.Callback vc = new VirtualDisplay.Callback() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reset();
    }
}
