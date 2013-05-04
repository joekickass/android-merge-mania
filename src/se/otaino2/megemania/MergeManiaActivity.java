package se.otaino2.megemania;

import se.otaino2.megemania.GameBoard.GameThread;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MergeManiaActivity extends Activity {

    private GameBoard board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_merge_mania);
        board = (GameBoard) findViewById(R.id.gameboard);
        board.setTextView((TextView) findViewById(R.id.label));
        
        // we were just launched: set up a new game
        board.getThread().setState(GameThread.STATE_READY);
    }
    
    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        board.getThread().pause();
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
