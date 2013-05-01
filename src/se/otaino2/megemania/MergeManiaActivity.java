package se.otaino2.megemania;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MergeManiaActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_mania);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.merge_mania, menu);
        return true;
    }

}
