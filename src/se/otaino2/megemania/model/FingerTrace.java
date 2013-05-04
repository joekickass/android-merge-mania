package se.otaino2.megemania.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

public class FingerTrace {
    
    private int id;
    
    private Path path;
    
    private Paint paint;

    private boolean isCompleted = false;

    private Region region;
    
    public FingerTrace(int id, float x, float y) {
        this.id = id;
        
        path = new Path();
        path.moveTo(x, y);
        
        paint = new Paint();
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
    }

    public synchronized void addPosition(float x, float y) {
        path.lineTo(x, y);
    }
    
    public synchronized void completeTrace() {
        path.close();
        isCompleted = true;
        createRegionForContainsCheck();
    }
    
    private void createRegionForContainsCheck() {
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
    }

    public void evaluateTrace(Circles circles) {
        
        List<Circle> encapsulatedCircles = new ArrayList<Circle>();
        
        // Find all circles of the same type within path
        for (Circle circle : circles.get()) {
            if(region.contains( (int) circle.getCx(), (int) circle.getCy() )) {
                if (!encapsulatedCircles.isEmpty()) {
                    if (!encapsulatedCircles.get(0).isSame(circle)) {
                        return;
                    }
                }
                encapsulatedCircles.add(circle);
            }
        }
        
        if (encapsulatedCircles.isEmpty()) {
            return;
        }
        
        // Remove the encapsulated circles from the list and create a new, fatter circle instead
        float newX = 0.0f;
        float newY = 0.0f;
        float newRadius = 0.0f;
        float newVx = 0.0f;
        float newVy = 0.0f;
        Paint paint = encapsulatedCircles.get(0).getPaint();
        for (Circle circle : encapsulatedCircles) {
            newX += circle.getCx();
            newY += circle.getCy();
            newRadius += circle.getRadius();
            newVx += circle.getVx();
            newVy += circle.getVy();
            circles.removeCircle(circle);
        }
        newX /= encapsulatedCircles.size();
        newY /= encapsulatedCircles.size();
        newVx /= newRadius;
        newVy /= newRadius;
        Circle fatCircle = new Circle(newX, newY, newRadius, paint);
        fatCircle.setVx(newVx);
        fatCircle.setVy(newVy);
        fatCircle.startMoving();
        circles.addCircle(fatCircle);
    }

    public int getId() {
        return id;
    }
    
    public synchronized void drawPositions(Canvas c) {
        c.drawPath(path, paint);
    }

    public boolean isCompleted() {
        return isCompleted;
    }
}
