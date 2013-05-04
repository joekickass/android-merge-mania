package se.otaino2.megemania;

import se.otaino2.megemania.GameBoard.GameThread;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MergeManiaActivity extends Activity {

    private GameBoard board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_mania);
        board = (GameBoard) findViewById(R.id.gameboard);
        
        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            board.getThread().setState(GameThread.STATE_READY);
        } else {
            // we are being restored: resume a previous game
            //board.getThread().restoreState(savedInstanceState);
        }
    }
    
    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        board.getThread().pause();
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     *
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //board.getThread().saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.merge_mania, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_start:
            board.getThread().doStart();
            return true;
        case R.id.action_stop:
            board.getThread().doStop();
            return true;
        case R.id.action_pause:
            board.getThread().pause();
            return true;
        case R.id.action_resume:
            board.getThread().unpause();
            return true;
        }
        return false;
    }
}
