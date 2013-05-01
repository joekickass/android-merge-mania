package se.otaino2.megemania.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class CircleFactory {
    
    private static final float INITIAL_CIRCLE_RADIUS = 20.0f;
    
    private static final int[] AVAILABLE_CIRCLE_COLORS = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA, Color.CYAN};
    
    public static List<Circle> generateRandomCircles(Board board, int nbrOfCircles) {
        List<Circle> circles = new ArrayList<Circle>();
        for (int i = 0; i < nbrOfCircles; i++) {
            circles.add(generateRandomCircle(board));
        }
        return circles;
    }
    
    public static Circle generateRandomCircle(Board board) {
        float x = (float) (Math.random() * board.getWidth());
        float y = (float) (Math.random() * board.getHeight());
        Paint paint = new Paint();
        paint.setColor(getRandomColor());
        return new Circle(x, y, INITIAL_CIRCLE_RADIUS, paint);
    }
    
    private static int getRandomColor() {
        int index = (int) (Math.random() * AVAILABLE_CIRCLE_COLORS.length);
        return AVAILABLE_CIRCLE_COLORS[index];
    }
}
