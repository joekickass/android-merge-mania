package se.otaino2.megemania.model;

/**
 * The board class is responsible for keeping track of the size of the game board and making sure no circles venture outside its boundaries. It also decides
 * when a game is finished by counting the number of different circle types present on the board.
 * 
 * @author otaino-2
 * 
 */
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

    /**
     * Makes sure the circle stays within the game board.
     * 
     * @param circle - the circle to calculate the next position for
     * @param duration - the time the circle will travel
     */
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

    /**
     * Game ends when only one circle exists in any of the types (colors)
     * 
     * @param circles - all circles present on the board
     * @return true if the game is finished, false otherwise
     */
    public boolean isGameFinished(Circles circles) {
        for (Integer type : circles.getTypes()) {
            if (circles.getTypeCount(type) == 1) {
                return true;
            }
        }
        return false;
    }
}
