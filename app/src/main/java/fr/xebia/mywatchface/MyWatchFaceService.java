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

/**
 * Created by florentchampigny on 25/11/2015.
 */
public class MyWatchFaceService extends CanvasWatchFaceService {

    static final long UPDATE_RATE_MS = 1000; //each second

    protected class AbstractEngine extends CanvasWatchFaceService.Engine {
        boolean isRunning = false;
        Handler updateHandler = new Handler() {
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

        protected Time time;
        //region background
        protected Paint backgroundPaint;
        protected Bitmap backgroundBitmap;
        //endregion
        protected float backgroundScale;
        //region surface
        protected int width, height;
        protected float centerX, centerY;
        //endregion

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
            backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
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
            this.centerX = width / 2f;
            this.centerY = height / 2f;

            //region background
            this.backgroundScale = ((float) (width)) / (float) backgroundBitmap.getWidth();
            this.backgroundBitmap = Bitmap.createScaledBitmap(this.backgroundBitmap,
                    (int) (backgroundScale * (this.backgroundBitmap.getWidth())),
                    (int) (backgroundScale * (this.backgroundBitmap.getHeight())),
                    true);
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
        //endregion

        @Override public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            isRunning = visible;

            if (isRunning) {
                time.setToNow();
                updateTimer();
            }
        }

        @Override public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            time.setToNow();

            //region background
            canvas.drawBitmap(this.backgroundBitmap, 0, 0, backgroundPaint);
            //endregion
        }

        private void updateTimer() {
            updateHandler.removeMessages(R.id.message_update);
            if (isRunning) {
                updateHandler.sendEmptyMessage(R.id.message_update);
            }
        }
    }

    protected class CadranEngine extends AbstractEngine {
        float HAND_WIDTH_HOURS = 4f;
        float HAND_WIDTH_MINUTES = 2f;
        float HAND_WIDTH_SECONDS = 1f;
        float HAND_WIDTH = 4f;

        //region hand
        Paint handPaintHours;
        Paint handPaintMinutes;
        Paint handPaintSeconds;

        float handHourHeight;
        float handMinuteHeight;
        float handSecondHeight;
        //endregion

        protected void drawHand(Canvas canvas, float height, float width, Paint paint) {
            float left = centerX - width;
            float right = centerX + width;
            float top = centerY - height;
            float bottom = centerY + width;
            canvas.drawRoundRect(left, top, right, bottom, width, width, paint);
        }

        @Override public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

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
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            //region hand
            handHourHeight = centerX * 0.3f;
            handMinuteHeight = centerX * 0.6f;
            handSecondHeight = centerX * 0.8f;
            //endregion
        }

        @Override public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            //region hand

            /**
             * 60 seconds per minute : 360/60 = 6
             * 60 minutes per hour : 360/60 = 6
             * 12 hours per turn : 360/12 = 30
             */
            final float secondsAngle = time.second * 6f;
            final float minutesAngle = time.minute * 6f;
            final float hourAngle = (time.hour + time.minute / 60f) * 30f;

            for(int i=0;i<=11;i++){
                canvas.save();
                {
                    canvas.rotate(360/12*i, centerX, centerY);
                    canvas.drawRect(centerX - 1, height*0.85f,centerX+1,height*0.9f,handPaintSeconds);
                }
                canvas.restore();
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

            canvas.save();
            {
                canvas.rotate(secondsAngle, centerX, centerY);
                drawHand(canvas, handSecondHeight, HAND_WIDTH_SECONDS, handPaintSeconds);
            }
            canvas.restore();
            //endregion
        }
    }

    @Override public Engine onCreateEngine() {
        return new CadranEngine();
    }
}