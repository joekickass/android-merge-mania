package se.otaino2.megemania;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.otaino2.megemania.model.Background;
import se.otaino2.megemania.model.Board;
import se.otaino2.megemania.model.Circle;
import se.otaino2.megemania.model.CircleFactory;
import se.otaino2.megemania.model.FingerTrace;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class GameBoard extends SurfaceView implements SurfaceHolder.Callback, OnTouchListener {

    private static final String TAG = "GameBoard";

    // Render thread
    private GameThread thread;

    // Status label
    private TextView labelView;

    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        thread = new GameThread(holder, getContext(), new LabelHandler(labelView));

        // SurfaceView must have focus to get touch events
        setFocusable(true);
        setOnTouchListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus)
            thread.pause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created! Starting thread...");
        if (thread.getState() == Thread.State.TERMINATED) {
            Log.d(TAG, "Had to create a new thread, old one was terminated!");
            thread = new GameThread(holder, getContext(), new LabelHandler(labelView));
            thread.setState(GameThread.STATE_READY);
        }
        thread.setRunning(true);
        thread.start(); // Starting thread, but won't activate until doStart is called...
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed, updating board size...");
        thread.setSurfaceSize(width, height);
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
        for (int i = 0; i < event.getPointerCount(); i++) {

            int id = event.getPointerId(i);
            if (event.getActionIndex() == i
                    && (event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN || event.getActionMasked() == MotionEvent.ACTION_DOWN)) {

                Log.d(TAG, "New finger detected! " + id + ", pos=" + event.getX(id) + ", " + event.getY(id));
                FingerTrace trace = new FingerTrace(id, event.getX(id), event.getY(id));
                thread.fingerFound(trace);

            } else if (event.getActionIndex() == i
                    && (event.getActionMasked() == MotionEvent.ACTION_POINTER_UP || event.getActionMasked() == MotionEvent.ACTION_UP
                            || event.getActionMasked() == MotionEvent.ACTION_OUTSIDE || event.getActionMasked() == MotionEvent.ACTION_CANCEL)) {

                Log.d(TAG, "Finger lost! " + id + ", pos=" + event.getX(id) + ", " + event.getY(id));
                thread.fingerLost(id);

            } else {

                Log.d(TAG, "Any of the existing fingers moving around! " + id + ", pos=" + event.getX(id) + ", " + event.getY(id));
                thread.fingerMoved(id, event.getX(id), event.getY(id));
            }
        }
        return true;
    }

    public GameThread getThread() {
        return thread;
    }

    public void setTextView(TextView statusLabel) {
        labelView = statusLabel;
    }

    // Static handler with a weak reference to the textview
    static class LabelHandler extends Handler {

        private final WeakReference<TextView> labelRef;

        public LabelHandler(TextView statusLabel) {
            this.labelRef = new WeakReference<TextView>(statusLabel);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d("Handler", "Received message");
            TextView label = labelRef.get();
            if (label != null) {
                Log.d("Handler", "Setting message: " + msg.getData().getString("text"));
                label.setVisibility(msg.getData().getInt("viz"));
                label.setText(msg.getData().getString("text"));
            }
        }
    }

    class GameThread extends Thread {

        // State constants
        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_WIN = 5;

        // Main objects
        private SurfaceHolder surfaceHolder;
        private Handler handler;
        private Context context;

        // State and runtime variables
        private int state;
        private boolean running;
        private long lastTime;
        private int nbrOfWinsInARow;

        // Entities
        private Board board;
        private Background background;
        private List<Circle> circles;
        private Map<Integer, FingerTrace> traces;

        public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
            this.handler = handler;
            this.traces = new ConcurrentHashMap<Integer, FingerTrace>();
        }

        public void fingerFound(FingerTrace fingerTrace) {
            traces.put(fingerTrace.getId(), fingerTrace);
        }

        public void fingerLost(int id) {
            FingerTrace trace = traces.get(id);
            trace.completeTrace();
        }

        public void fingerMoved(int id, float x, float y) {
            FingerTrace trace = traces.get(id);
            trace.addPosition(x, y);
        }

        public void doStart() {
            synchronized (surfaceHolder) {
                resetGame(board);
                setState(STATE_RUNNING);
            }
        }

        public void doStop() {
            synchronized (surfaceHolder) {
                clearGame();
                setState(STATE_LOSE, context.getText(R.string.message_stopped));
            }
        }

        public void pause() {
            synchronized (surfaceHolder) {
                if (state == STATE_RUNNING) {
                    setState(STATE_PAUSE);
                }
            }
        }

        public void unpause() {
            synchronized (surfaceHolder) {
                lastTime = System.currentTimeMillis();
                setState(STATE_RUNNING);
            }
        }

        public void setState(int state) {
            synchronized (surfaceHolder) {
                setState(state, null);
            }
        }

        public void setState(int mode, CharSequence message) {
            synchronized (surfaceHolder) {
                state = mode;

                if (state == STATE_RUNNING) {

                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", "");
                    b.putInt("viz", View.INVISIBLE);
                    msg.setData(b);
                    handler.sendMessage(msg);

                } else {

                    Resources res = context.getResources();
                    CharSequence str = "";
                    if (state == STATE_READY) {
                        str = res.getText(R.string.mode_ready);
                    } else if (state == STATE_PAUSE) {
                        str = res.getText(R.string.mode_pause);
                    } else if (state == STATE_LOSE) {
                        str = res.getText(R.string.mode_lose);
                    } else if (state == STATE_WIN) {
                        str = res.getString(R.string.mode_win_prefix) + nbrOfWinsInARow + " " + res.getString(R.string.mode_win_suffix);
                    }

                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    // Handle special state actions
                    if (state == STATE_READY) {
                        thread.clearGame();
                    } else if (state == STATE_LOSE) {
                        nbrOfWinsInARow = 0;
                    }

                    Message msg = handler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("text", str.toString());
                    b.putInt("viz", View.VISIBLE);
                    msg.setData(b);
                    handler.sendMessage(msg);
                }
            }
        }

        public void setRunning(boolean running) {
            this.running = running;
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
                resetBoard(width, height);
            }
        }

        private void resetBoard(int width, int height) {
            board = new Board(width, height);
            background = new Background(width, height);
        }

        private void resetGame(Board board) {
            circles = CircleFactory.generateRandomCircles(board, 20);
            lastTime = System.currentTimeMillis();
        }

        private void clearGame() {
            circles = Collections.emptyList();
        }

        // Update game entities for next iteration
        private void updatePhysics() {

            long now = System.currentTimeMillis();
            float elapsed = (now - lastTime) / 1000.0f;

            if (state == STATE_RUNNING) {
                for (Circle circle : circles) {
                    board.processCircle(circle, elapsed);
                }
                Iterator<FingerTrace> iter = traces.values().iterator();
                while(iter.hasNext()) {
                    FingerTrace trace = iter.next();
                    if (trace.isCompleted()) {
                        trace.evaluateTrace(circles);
                        traces.remove(trace.getId());
                    }
                }
            }

            lastTime = now;
        }

        private void doDraw(Canvas c) {
            renderBackground(c);
            renderCircles(c);
            renderFingers(c);
        }
        
        private void renderFingers(Canvas c) {
            for(FingerTrace trace : traces.values()) {
                trace.drawPositions(c);
            }
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
