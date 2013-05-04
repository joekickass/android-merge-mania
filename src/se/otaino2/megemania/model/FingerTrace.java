package se.otaino2.megemania.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;

public class FingerTrace {

    private int id;

    private Path path;

    private List<PointF> points;

    private Paint paint;

    private boolean isCompleted = false;
    
    private boolean isCanceled = false;

    private Region region;

    public FingerTrace(int id, float x, float y) {
        this.id = id;

        path = new Path();
        points = new ArrayList<PointF>();

        path.moveTo(x, y);
        PointF point = new PointF(x, y);
        points.add(point);

        paint = new Paint();
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
    }

    public synchronized boolean addPosition(float x, float y) {
        if (!isPathIntersectingWithItself(x, y)) {
            path.lineTo(x, y);
            PointF point = new PointF(x, y);
            points.add(point);
            return true;
        }
        return false;
    }

    private boolean isPathIntersectingWithItself(float x, float y) {

        float x1 = points.get(points.size()-1).x;
        float x2 = x;
        float y1 = points.get(points.size()-1).y;
        float y2 = y;

        for (int i = 1; i < points.size()-1; i++) {
            float x3 = points.get(i-1).x;
            float x4 = points.get(i).x;
            float y3 = points.get(i-1).y;
            float y4 = points.get(i).y;
            
            if (isIntersecting(x1, y1, x2, y2, x3, y3, x4, y4)) {
                return true;
            }
        }

        return false;
    }

    public synchronized void completeTrace() {
        path.close();
        points.add(points.get(0));
        isCompleted = true;
        createRegionForContainsCheck();
    }
    

    public void cancelTrace() {
        isCanceled = true;
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
            if (region.contains((int) circle.getCx(), (int) circle.getCy())) {
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
        if (!isCanceled) {
            if (points.size() > 1) {
                for (int i = 1; i < points.size(); i++) {
                    c.drawLine(points.get(i - 1).x, points.get(i - 1).y, points.get(i).x, points.get(i).y, paint);
                }
            }
        }
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    
    public static boolean isIntersecting(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
     // Check if lines are parallel
        
        float denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if(denom == 0) { 
         // Lines are parallel, this sucks...
        }

        float a = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        float b = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;

        // Check for intersection
        if( a >= 0.0f && a <= 1.0f && b >= 0.0f && b <= 1.0f) {
            return true;
        }
        return false;
    }
}
