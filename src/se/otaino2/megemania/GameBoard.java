package se.otaino2.megemania;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import se.otaino2.megemania.model.Background;
import se.otaino2.megemania.model.Board;
import se.otaino2.megemania.model.Circle;
import se.otaino2.megemania.model.CircleFactory;
import se.otaino2.megemania.model.Circles;
import se.otaino2.megemania.model.FingerTrace;
import android.content.Context;
import android.content.Intent;
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
        thread = new GameThread(holder, getContext(), new LabelHandler(labelView));
        thread.setState(GameThread.STATE_READY);
        thread.setRunning(true);
        thread.start(); // Starting thread, but won't activate until doStart is called...
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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

    /**
     * This is weird. It seems that event.getActionIndex() only returns a valid index for UP and DOWN events. When handling move events, one must instead loop
     * over available pointer indexes and get their pointer id. Note that indexes may vary between touch events, however event.getActionIndex will always return
     * the correct pointer index for UP and DOWN events.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        thread.handleGameState();

        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);

        switch (event.getActionMasked()) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            FingerTrace trace = new FingerTrace(pointerId, event.getX(pointerIndex), event.getY(pointerIndex));
            thread.fingerFound(trace);
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            thread.fingerLost(pointerId);
            return false;

        case MotionEvent.ACTION_MOVE:
            for (int i = 0; i < event.getPointerCount(); i++) {
                pointerId = event.getPointerId(i);
                thread.fingerMoved(pointerId, event.getX(i), event.getY(i));
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            break;
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
            TextView label = labelRef.get();
            if (label != null) {
                label.setVisibility(msg.getData().getInt("viz"));
                label.setText(msg.getData().getString("text"));
            }
        }
    }

    class GameThread extends Thread {

        //
        private static final int NBR_OF_BALLS = 40;

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
        private int startTime;

        // Entities
        private Board board;
        private Background background;
        private Circles circles;
        private Map<Integer, FingerTrace> traces;

        public GameThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            this.surfaceHolder = surfaceHolder;
            this.context = context;
            this.handler = handler;
            this.traces = new ConcurrentHashMap<Integer, FingerTrace>();
        }

        public void handleGameState() {

            switch (state) {

            case STATE_READY:
                doStart();
                break;

            case STATE_PAUSE:
                unpause();
                break;

            default:
                break;
            }
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

                    startTime = getTimeNow();
                    Log.d(TAG, "Starttime=" + startTime);

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
                        str = res.getString(R.string.mode_win);
                    }

                    if (message != null) {
                        str = message + "\n" + str;
                    }

                    // Handle special state actions
                    if (state == STATE_WIN) {
                        int finishTime = getTimeNow() - startTime;
                        Intent intent = new Intent(context, HighscorePage.class);
                        intent.putExtra(HighscorePage.FINISH_TIME, finishTime);
                        context.startActivity(intent);
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

        private int getTimeNow() {
            return (int) (System.currentTimeMillis() / 1000);
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
            circles = CircleFactory.generateRandomCircles(board, NBR_OF_BALLS);
            lastTime = System.currentTimeMillis();
        }

        private void clearGame() {
            if (circles != null) {
                circles.clear();
            }
        }

        // Update game entities for next iteration
        private void updatePhysics() {

            long now = System.currentTimeMillis();
            float elapsed = (now - lastTime) / 1000.0f;

            if (state == STATE_RUNNING) {

                // Update every circle with new position and speed
                if (circles != null) {
                    for (Circle circle : circles.get()) {
                        board.processCircle(circle, elapsed);
                    }
                }

                // Check if a trace was completed and if any circles were caught within
                Iterator<FingerTrace> iter = traces.values().iterator();
                while (iter.hasNext()) {
                    FingerTrace trace = iter.next();
                    if (trace.isCompleted()) {
                        trace.evaluateTrace(circles);
                        traces.remove(trace.getId());
                    }
                }

                // Check if game should end
                if (board.isGameFinished(circles)) {
                    setState(STATE_WIN);
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
            if (traces != null) {
                for (int id : traces.keySet()) {
                    traces.get(id).drawPositions(c);
                }
            }
        }

        private void renderCircles(Canvas c) {
            if (circles != null) {
                for (Circle circle : circles.get()) {
                    c.drawCircle(circle.getCx(), circle.getCy(), circle.getRadius(), circle.getPaint());
                }
            }
        }

        // Render the board background.
        private void renderBackground(Canvas c) {
            c.drawRect(background.getRect(), background.getPaint());
        }
    }
}
