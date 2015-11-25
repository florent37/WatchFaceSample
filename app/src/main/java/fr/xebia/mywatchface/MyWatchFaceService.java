package fr.xebia.mywatchface;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

/**
 * Created by florentchampigny on 25/11/2015.
 */
public class MyWatchFaceService extends CanvasWatchFaceService {

    static final long UPDATE_RATE_MS = 1000; //each second

    @Override public Engine onCreateEngine() {
        return new CanvasWatchFaceService.Engine() {

            //TODO 1. HANDLER

            boolean isRunning = false;
            Time time;

            //region background
            Paint backgroundPaint;
            Bitmap backgroundBitmap;
            float backgroundScale;
            //endregion

            //region hand
            Paint handPaint;
            float HAND_WIDTH = 4f;
            float handHourHeight;
            float handMinuteHeight;
            float handSecondHeight;
            //endregion

            //region surface
            int width, height;
            float centerX, centerY;
            Rect cardBounds = new Rect();

            //endregion

            final Handler updateHandler = new Handler() {
                @Override public void handleMessage(Message msg) {
                    if (msg.what == R.id.message_update) {
                        invalidate();
                        if (isRunning) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = UPDATE_RATE_MS - (timeMs % UPDATE_RATE_MS); //force to be exactly at the next second

                            updateHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                        }
                    }
                }
            };

            @Override public void onCreate(SurfaceHolder holder) {
                super.onCreate(holder);
                //TODO 2. WatchStyle

                setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFaceService.this)
                        .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                        .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                        .setShowSystemUiTime(false)
                        .build());

                //region background
                backgroundPaint = new Paint();
                backgroundPaint.setColor(Color.BLUE);
                backgroundBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.background);
                //endregion

                //region hand
                handPaint = new Paint();
                handPaint.setColor(Color.WHITE);
                handPaint.setStrokeWidth(4f);
                handPaint.setAntiAlias(true);
                handPaint.setStrokeCap(Paint.Cap.ROUND);
                handPaint.setShadowLayer(6f,0,0,Color.BLACK);
                handPaint.setStyle(Paint.Style.STROKE);
                //endregion

                time = new Time();
            }

            @Override public void onDestroy() {
                super.onDestroy();
                updateHandler.removeMessages(R.id.message_update);
            }

            @Override
            public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                super.onSurfaceChanged(holder, format, width, height);

                this.width = width;
                this.height = height;
                this.centerX = width/2f;
                this.centerY = height/2f;

                //region background
                this.backgroundScale = ((float)(width)) / (float)backgroundBitmap.getWidth();
                this.backgroundBitmap = Bitmap.createScaledBitmap(this.backgroundBitmap,
                        (int) (backgroundScale * (this.backgroundBitmap.getWidth())),
                        (int) (backgroundScale * (this.backgroundBitmap.getHeight())),
                        true);
                //endregion

                //region hand
                handHourHeight = centerX*0.3f;
                handMinuteHeight = centerX*0.6f;
                handSecondHeight = centerX*0.8f;
                //endregion
            }

            @Override public void onTimeTick() {
                super.onTimeTick();
                invalidate();
            }

            //region ambiant
            @Override public void onAmbientModeChanged(boolean inAmbientMode) {
                super.onAmbientModeChanged(inAmbientMode);
            }

            @Override public void onPropertiesChanged(Bundle properties) {
                super.onPropertiesChanged(properties);
            }

            @Override public void onVisibilityChanged(boolean visible) {
                super.onVisibilityChanged(visible);

                isRunning = visible;

                if(isRunning){
                    time.setToNow();
                    updateTimer();
                }
            }
            //endregion

            @Override public void onPeekCardPositionUpdate(Rect rect) {
                super.onPeekCardPositionUpdate(rect);
            }

            @Override public void onDraw(Canvas canvas, Rect bounds) {
                super.onDraw(canvas, bounds);
                time.setToNow();

                //region background
                canvas.drawBitmap(this.backgroundBitmap, 0, 0, backgroundPaint);
                //endregion

                //region hand

                /**
                 * 60 seconds per minute : 360/60 = 6
                 * 60 minutes per hour : 360/60 = 6
                 * (12h->) 12 hours per day : 360/(24/2) = 30
                 */
                final float secondsAngle = time.second * 6f;
                final float minutesAngle = time.minute * 6f;
                final float hourAngle = (time.hour/2f + 60f/time.minute) * 15f;

                canvas.save();{
                    canvas.rotate(hourAngle,centerX,centerY);
                    drawHand(canvas, handHourHeight);
                }canvas.restore();

                canvas.save();{
                    canvas.rotate(minutesAngle,centerX,centerY);
                    drawHand(canvas, handMinuteHeight);
                }canvas.restore();

                canvas.save();{
                    canvas.rotate(secondsAngle,centerX,centerY);
                    drawHand(canvas, handSecondHeight);
                }canvas.restore();
                //enregion
            }

            protected void drawHand(Canvas canvas, float height){
                float left = centerX - HAND_WIDTH;
                float right = centerX + HAND_WIDTH;
                float top = centerY - height;
                float bottom = centerY + HAND_WIDTH;
                canvas.drawRect(left,top,right,bottom,handPaint);
            }

            private void updateTimer(){
                updateHandler.removeMessages(R.id.message_update);
                if(isRunning){
                    updateHandler.sendEmptyMessage(R.id.message_update);
                }
            }
        };
    }

}
