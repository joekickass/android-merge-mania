package se.otaino2.megemania;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MergeManiaActivity extends Activity {

    private GameBoard board;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_merge_mania);
        board = (GameBoard) findViewById(R.id.gameboard);
        board.setTextView((TextView) findViewById(R.id.label));
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        board.getThread().pause();
    }
}
