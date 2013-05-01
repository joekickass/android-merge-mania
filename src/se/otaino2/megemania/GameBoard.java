package se.otaino2.megemania;

import java.util.List;

import se.otaino2.megemania.model.Background;
import se.otaino2.megemania.model.Board;
import se.otaino2.megemania.model.Circle;
import se.otaino2.megemania.model.CircleFactory;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

public class GameBoard extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {

    private static final String TAG = "GameBoard";

    // Render thread
    private GameThread thread;
    
    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // SurfaceView must have focus to get touch events
        setFocusable(true);
        setOnTouchListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created, starting new thread...");
        thread = new GameThread(holder);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed, resetting game...");
        thread.setSurfaceSize(width, height);
        thread.reset();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed, trying to shut down thread...");
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Do nothing
            }
        }
        Log.d(TAG, "Thread's dead, baby. Thread's dead.");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch");
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                break;
        }
        return true;
    }

    public GameThread getThread() {
        return thread;
    }

    class GameThread extends Thread {

        // Other stuff 
        private SurfaceHolder surfaceHolder;
        private boolean running;
        private int canvasWidth;
        private int canvasHeight;
        private long lastTime;
        
        // Entities
        private Board board;
        private Background background;
        private List<Circle> circles;


        public GameThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public void reset() {
            synchronized (surfaceHolder) {
                resetEntities(canvasWidth, canvasHeight);               
            }
        }

        @Override
        public void run() {
            while (running) {
                Canvas c = null;
                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        updatePhysics();
                        // NOTE: In newer versions of Android (4+), it seems SurfaceHolder.lockCanvas() may return null whenever
                        // SurfaceHolder.Callback.surfaceDestroyed() has been invoked. In earlier versions, a canvas was always
                        // returned until SurfaceHolder.Callback.surfaceDestroyed() was FINISHED. See bug report:
                        // https://code.google.com/p/android/issues/detail?id=38658
                        if (c != null) {
                            doDraw(c);
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (surfaceHolder) {
                canvasWidth = width;
                canvasHeight = height;
                reset();
            }
        }

        private void resetEntities(int width, int height) {
            board = new Board(width, height);
            background = new Background(width, height);
            circles = CircleFactory.generateRandomCircles(board, 20);
        }

        // Update game entities for next iteration
        private void updatePhysics() {
            long now = System.currentTimeMillis();

            // Make sure we don't update physics unnecessary often
            if (lastTime > now)
                return;

            double elapsed = (now - lastTime) / 1000.0;
            
            lastTime = now;
        }

        // Draws game entities on canvas. Must be run in
        private void doDraw(Canvas c) {
            renderBackground(c);
            renderCircles(c);
        }

        private void renderCircles(Canvas c) {
            for (Circle circle : circles) {
                c.drawCircle(circle.getCx(), circle.getCy(), circle.getRadius(), circle.getPaint());
            }
        }

        // Render the board background.
        private void renderBackground(Canvas c) {
            c.drawRect(background.getRect(), background.getPaint());
        }
    }
}
