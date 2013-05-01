package se.otaino2.megemania.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Color;
import android.graphics.Paint;

public class CircleFactory {
    
    private static final float INITIAL_CIRCLE_RADIUS = 20.0f;
    
    private static final int[] AVAILABLE_CIRCLE_COLORS = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA, Color.CYAN};
    
    public static List<Circle> generateRandomCircles(Board board, int nbrOfCircles) {
        List<Circle> circles = new ArrayList<Circle>();
        for (int i = 0; i < nbrOfCircles; i++) {
            Circle circle = generateRandomCircle(board);
            circle.startMoving();
            circles.add(circle);
        }
        return circles;
    }
    
    public static Circle generateRandomCircle(Board board) {
        float x = randomNumber(INITIAL_CIRCLE_RADIUS, board.getWidth()-INITIAL_CIRCLE_RADIUS);
        float y = randomNumber(INITIAL_CIRCLE_RADIUS, board.getHeight()-INITIAL_CIRCLE_RADIUS);
        Paint paint = new Paint();
        paint.setColor(getRandomColor());
        return new Circle(x, y, INITIAL_CIRCLE_RADIUS, paint);
    }

    private static int getRandomColor() {
        int index = (int) (Math.random() * AVAILABLE_CIRCLE_COLORS.length);
        return AVAILABLE_CIRCLE_COLORS[index];
    }
    
    public static float randomNumber(float min, float max) {
        return min + new Random().nextInt((int)(max - min));
    }
}
