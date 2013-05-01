package se.otaino2.megemania.model;

public class Board {
    private int width;
    private int height;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public void processCircle(Circle circle, float duration) {
        
        float radius = circle.getRadius();
        
        float x = circle.getCx();
        float dx = circle.getVx() * duration;
        float newX = x + dx;
        if (newX < radius) {
            circle.setVx(-circle.getVx());
            circle.setCx(2 * radius - newX);
        } else if (newX > width - radius) {
            circle.setVx(-circle.getVx());
            circle.setCx(2 * (width - radius) - newX);
        } else {
            circle.setCx(newX);
        }

        float y = circle.getCy();
        float dy = circle.getVy() * duration;
        float newY = y + dy;
        if (newY < radius) {
            circle.setVy(-circle.getVy());
            circle.setCy(2 * radius - newY);
        } else if (newY > height - radius) {
            circle.setVy(-circle.getVy());
            circle.setCy(2 * (height - radius) - newY);
        } else {
            circle.setCy(newY);
        }
    }
}
