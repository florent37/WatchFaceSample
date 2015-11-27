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

    static final long UPDATE_RATE_MS = 1000; //each second

    @Override public Engine onCreateEngine() {
        return new MyEngine();
    }

    protected class MyEngine extends CanvasWatchFaceService.Engine {
        protected Calendar calendar;
        // receiver to update the time zone
        final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                calendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        //region background
        protected Paint backgroundPaint;
        protected Bitmap backgroundBitmap;
        protected int colorBackground;
        protected float backgroundScale;
        //region surface
        protected int width, height;
        //endregion
        protected float centerX, centerY;
        boolean isRunning = false;
        //endregion
        Handler updateHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                if (msg.what == R.id.message_update) {
                    invalidate();
                    if (isRunning && !ambiant) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = UPDATE_RATE_MS - (timeMs % UPDATE_RATE_MS); //force to be exactly at the next second

                        updateHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };
        //region hands
        Paint handPaintHours;
        Paint handPaintMinutes;
        Paint handPaintSeconds;
        float HAND_WIDTH_HOURS = 4f;
        float HAND_WIDTH_MINUTES = 2f;
        float HAND_WIDTH_SECONDS = 1f;
        float handHourHeight;
        float handMinuteHeight;
        //endregion
        float handSecondHeight;
        boolean registeredTimeZoneReceiver;
        boolean ambiant;

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
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.one);
            colorBackground = Color.parseColor("#66000000");
            //endregion

            //region hand
            handPaintHours = new Paint();
            handPaintHours.setColor(Color.parseColor("#DDFFFFFF"));
            handPaintHours.setStrokeWidth(HAND_WIDTH_HOURS);
            handPaintHours.setAntiAlias(true);
            handPaintHours.setStrokeCap(Paint.Cap.ROUND);
            handPaintHours.setShadowLayer(6f, 0, 0, Color.BLACK);
            handPaintHours.setStyle(Paint.Style.FILL);

            handPaintMinutes = new Paint();
            handPaintMinutes.setColor(Color.parseColor("#CCFFFFFF"));
            handPaintMinutes.setStrokeWidth(HAND_WIDTH_MINUTES);
            handPaintMinutes.setAntiAlias(true);
            handPaintMinutes.setStrokeCap(Paint.Cap.ROUND);
            handPaintMinutes.setShadowLayer(6f, 0, 0, Color.BLACK);
            handPaintMinutes.setStyle(Paint.Style.FILL);

            handPaintSeconds = new Paint();
            handPaintSeconds.setColor(Color.parseColor("#AAFFFFFF"));
            handPaintSeconds.setStrokeWidth(HAND_WIDTH_SECONDS);
            handPaintSeconds.setAntiAlias(true);
            handPaintSeconds.setStrokeCap(Paint.Cap.ROUND);
            handPaintSeconds.setShadowLayer(6f, 0, 0, Color.BLACK);
            handPaintSeconds.setStyle(Paint.Style.FILL);
            //endregion

            calendar = new GregorianCalendar();
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
            this.centerX = width / 2f;
            this.centerY = height / 2f;

            //region background
            this.backgroundScale = ((float) (width)) / (float) backgroundBitmap.getWidth();
            this.backgroundBitmap = Bitmap.createScaledBitmap(this.backgroundBitmap,
                    (int) (backgroundScale * (this.backgroundBitmap.getWidth())),
                    (int) (backgroundScale * (this.backgroundBitmap.getHeight())),
                    true);
            //endregion

            //region hand
            handHourHeight = centerX * 0.3f;
            handMinuteHeight = centerX * 0.6f;
            handSecondHeight = centerX * 0.8f;
            //endregion
        }

        //Called periodically to update the time shown by the watch face (each minute)
        @Override public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            isRunning = visible;

            if (isRunning) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                calendar.setTimeZone(TimeZone.getDefault());

                updateTimer();
            } else {
                unregisterReceiver();
            }
        }

        @Override public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            this.ambiant = inAmbientMode;

            this.backgroundPaint.setAntiAlias(!ambiant);
            this.handPaintHours.setAntiAlias(!ambiant);
            this.handPaintMinutes.setAntiAlias(!ambiant);
            this.handPaintSeconds.setAntiAlias(!ambiant);

            if(!ambiant){
                isRunning = true;
                updateTimer();
            }
        }

        private void registerReceiver() {
            if (!registeredTimeZoneReceiver) {
                registeredTimeZoneReceiver = true;
                MyWatchFaceService.this.registerReceiver(timeZoneReceiver, new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED));
            }
        }

        private void unregisterReceiver() {
            if (registeredTimeZoneReceiver) {
                registeredTimeZoneReceiver = false;
                MyWatchFaceService.this.unregisterReceiver(timeZoneReceiver);
            }
        }

        @Override public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            calendar.setTimeInMillis(System.currentTimeMillis());

            //region background
            if(ambiant)
                canvas.drawColor(Color.BLACK);
            else{
                canvas.drawBitmap(this.backgroundBitmap, 0, 0, backgroundPaint);
                canvas.drawColor(colorBackground);
            }
            //endregion

            //region hand

            /**
             * 60 seconds per minute : 360/60 = 6
             * 60 minutes per hour : 360/60 = 6
             * 12 hours per turn : 360/12 = 30
             */
            final float secondsAngle = calendar.get(Calendar.SECOND) * 6f;
            final float minutesAngle = calendar.get(Calendar.MINUTE) * 6f;
            final float hourAngle = (calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE) / 60f) * 30f;

            if (!ambiant) {
                for (int i = 0; i <= 11; i++) {
                    canvas.save();
                    {
                        canvas.rotate(360 / 12 * i, centerX, centerY);
                        canvas.drawRect(centerX - 1, height * 0.85f, centerX + 1, height * 0.9f, handPaintSeconds);
                    }
                    canvas.restore();
                }
            }

            canvas.save();
            {
                canvas.rotate(hourAngle, centerX, centerY);
                drawHand(canvas, handHourHeight, HAND_WIDTH_HOURS, handPaintHours);
            }
            canvas.restore();

            canvas.save();
            {
                canvas.rotate(minutesAngle, centerX, centerY);
                drawHand(canvas, handMinuteHeight, HAND_WIDTH_MINUTES, handPaintMinutes);
            }
            canvas.restore();

            if (!ambiant) {
                canvas.save();
                {
                    canvas.rotate(secondsAngle, centerX, centerY);
                    drawHand(canvas, handSecondHeight, HAND_WIDTH_SECONDS, handPaintSeconds);
                }
                canvas.restore();
            }
            //endregion
        }

        protected void drawHand(Canvas canvas, float height, float width, Paint paint) {
            float left = centerX - width;
            float right = centerX + width;
            float top = centerY - height;
            float bottom = centerY + width;
            canvas.drawRoundRect(left, top, right, bottom, width, width, paint);
        }

        private void updateTimer() {
            updateHandler.removeMessages(R.id.message_update);
            if (isRunning) {
                updateHandler.sendEmptyMessage(R.id.message_update);
            }
        }
    }
}