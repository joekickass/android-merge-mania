package se.otaino2.megemania.model;

import java.util.Random;

import android.graphics.Color;
import android.graphics.Paint;

/**
 * Creates a list of circles for the specified board of different types.
 * 
 * @author otaino-2
 *
 */
public class CircleFactory {
    
    private static final float INITIAL_CIRCLE_RADIUS = 20.0f;
    
    private static final int[] AVAILABLE_CIRCLE_COLORS = {Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW, Color.MAGENTA, Color.CYAN};
    
    public static Circles generateRandomCircles(Board board, int nbrOfCircles) {
        Circles circles = new Circles();
        for (int i = 0; i < nbrOfCircles; i++) {
            Circle circle = generateRandomCircle(board);
            circle.startMoving();
            circles.addCircle(circle);
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
