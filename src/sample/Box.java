/**
 * @File: Box.java
 * @Author: Abhi Gupta
 * @Description: This class defines the way a box moves through the game including the force of gravity acting on it. As the game progresses,
 *               the amount of acceleration of the box as it's falling also increases to make it more challenging for the user. Basic properties
 *               such as whether or not the box is in the air and if it is ready to be dropped are included in this class.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import java.util.Random;

public class Box extends Sprite {
    private Random random = new Random();
    public boolean readyToDrop;     // whether or not the box is ready to be dropped
    private final double G = 0.0481;
    private double STOP;            // the y-position threshold of where to stop the box
    private ImageView box;
    private boolean dropPositionAlreadySet = false;

    /**
     * Constructor
     * @param x the box's x-position (never changes)
     * @param y the box's y-position (from where it is dropped)
     * @param stop the y-position threshold of where the box should stop
     * @param dark whether the box should have a light or dark shade
     */
    public Box(double x, double y, double stop, boolean dark) {
        super();
        STOP = stop;
        setImage(dark ? Img.darkBox : Img.lightBox);
        setPosition(x, y);
        box = new ImageView(image);
        box.setX(positionX);
        if (random.nextBoolean())   box.setRotate(90);
        readyToDrop = random.nextBoolean();
    }

    public ImageView getImageView() { return box; }

    public boolean isReady() {
        return readyToDrop;
    }

    public void unprepare() { readyToDrop = false; }

    /**
     * Prepares the box to be dropped (the box is ready to be dropped when the random value turns out to be true)
     */
    public void prepare() {
        readyToDrop = random.nextBoolean();
    }

    public void prepare(boolean val) {
        readyToDrop = val;
    }

    /**
     * Drops the box by adding acceleration due to gravity and taking the amount of time the user has played the game into consideration
     */
    public void drop(double time) {
        addVelocity(0,G);
        update(time);
    }

    public void setDropPosition(double y) {
        if (!dropPositionAlreadySet) {
            setPosition(getX(), y);
            dropPositionAlreadySet = true;
        }
    }

    /**
     * Returns the height at which the box will stop accelerating
     */
    public double getSTOP() {
        return STOP;
    }

    /**
     * Returns whether or not the box is in motion
     */
    public boolean inMotion() {
        return positionY != STOP;
    }

    /**
     * Ensures the box is within the y-position threshold and updates its y-position
     */
    @Override
    public void update(double time) {
        super.update(time/100);
        positionY = Math.min(positionY,STOP);
        box.setY(positionY);
    }

    /**
     * Renders the box on to the screen by adding it as a node to scene graph. Once an entire row of boxes has filled up the screen, they are only
     * stored internally by javaFX's scene graph rather than being saved in a class.
     */
    public void render(ObservableList<Node> children) {
        if (children.contains(box)) children.remove(box);
        children.add(box);
    }
}
