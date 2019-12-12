package com.llk.pd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.ByteBuffer;

import static com.llk.pd.ScreenCap.HEIGHT;
import static com.llk.pd.ScreenCap.WIDTH;

public class MainActivity extends AppCompatActivity {

    private ScreenCap screenCap;

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
        if (img.getPlanes() == null) return;

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

        screenCap = new ScreenCap(this, new ScreenCap.ScreenCapCallback() {
            @Override
            public void onCap(Image image) {
                makeBitmap(image);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        screenCap.mp_onActivityResult(requestCode, resultCode, data);

    }

    public void dm_click(View view) {
        screenCap.dmScreenCap();
    }


    public void mp_click(View view) {
        screenCap.mpScreenCap();
    }

    public void cs_click(View view) {
        stopService(new Intent(this, PService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(screenCap != null){
            screenCap.reset();
            screenCap = null;
        }
    }
}
