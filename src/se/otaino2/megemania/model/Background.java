package se.otaino2.megemania.model;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * A simple domain object representing the background
 * 
 * @author otaino-2
 *
 */
public class Background {
    private Rect rect;
    private Paint paint;
    public Background(int width, int height) {
        rect = new Rect(0, 0, width, height);
        paint = new Paint();
        paint.setColor(Color.WHITE);
    }
    
    public Rect getRect() {
        return rect;
    }

    public Paint getPaint() {
        return paint;
    }
}
