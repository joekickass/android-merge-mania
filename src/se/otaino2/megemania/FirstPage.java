package se.otaino2.megemania;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * The first page shown to the user. Simply presents the user with the option to start a new game.
 * 
 * @author otaino-2
 * 
 */
public class FirstPage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first_page);
        Button b = (Button) findViewById(R.id.startButton);
        b.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstPage.this, MergeManiaActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
