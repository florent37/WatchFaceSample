package fr.xebia.mywatchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by florentchampigny on 25/11/2015.
 */
public class MyWatchFaceService extends CanvasWatchFaceService {

    // region update rate

    public static final int UPDATE_RATE = 1000;

    //endregion

    //region engine

    public Engine engine = new Engine(){

        //region handler

        boolean isRunning;

        Handler updateHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                if (msg.what == R.id.message_update) {
                    invalidate();
                    if (isRunning) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = UPDATE_RATE - (timeMs % UPDATE_RATE); //force to be exactly at the next second

                        updateHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };

        protected void updateTimer(){
            updateHandler.removeMessages(R.id.message_update);
            if(isRunning){
                updateHandler.sendEmptyMessage(R.id.message_update);
            }
        }

        //endregion

    };

    //endregion

    @Override public Engine onCreateEngine() {
    }
}