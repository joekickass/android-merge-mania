package se.otaino2.megemania;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class HighscorePage extends Activity {

    public static final String FINISH_TIME = "extra_finish_time";

    private static final String PREF_HIGHSCORE = "pref_highscore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_highscore_page);
        Button button = (Button) findViewById(R.id.resumeButton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ListView list = (ListView) findViewById(R.id.list);
        TextView text = (TextView) findViewById(R.id.your_score);

        Bundle bundle = getIntent().getExtras();
        int result = (int) (bundle.getInt(FINISH_TIME));
        text.setText(getResources().getString(R.string.your_score) + " " + result);
        addResultToHighscore(result);

        List<String> highscores = getHighScoreList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        adapter.addAll(highscores);
        list.setAdapter(adapter);
    }

    private void addResultToHighscore(int result) {

        List<Integer> scores = getHighScores();

        scores.add(result);
        Collections.sort(scores);

        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        for (int i = 0; i < scores.size(); i++) {
            editor.putInt(PREF_HIGHSCORE + i, scores.get(i));
        }
        editor.commit();
    }

    private List<Integer> getHighScores() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        List<Integer> scores = new ArrayList<Integer>();
        for (int i = 0; i < 5; i++) {
            int score = preferences.getInt(PREF_HIGHSCORE + i, -1);
            if (score != -1)
                scores.add(score);
        }
        return scores;
    }

    private List<String> getHighScoreList() {
        List<String> ret = new ArrayList<String>();
        List<Integer> scores = getHighScores();
        for (int i : scores) {
            String s = getResources().getString(R.string.highscore_prefix) + " " + i + " " + getResources().getString(R.string.highscore_postfix);
            ret.add(s);
        }
        return ret;
    }
}
