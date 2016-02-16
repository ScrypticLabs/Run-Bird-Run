/**
 * @File: Bird.java
 * @Author: Abhi Gupta
 * @Description: This class handles the movement of the left and right arrows near the bottom of the screen. When the user hits the arrow keys
 *               on the keyboard to move the bird, the corresponding key is highlighted on the screen to provide some visual feedback to the user.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;

import java.util.ArrayList;

public class Key extends Button {
    Keyboard direction;     // direction of the key
    double initialY;        // the initial y-position of the object on the scene graph

    /**
     * Constructor
     * @param direction the left or right arrow key
     * @param x the x-position of the arrow key on the screen
     * @param y the initial y-position of the arrow key on the screen (since the screen is always shifting down, an offset will be applied to maintain this y-position)
     */
    public Key(Keyboard direction, double x, double y) {
        super(Img.keyRightUp,x,y);
        if (direction == Keyboard.LEFT) {
            button.setRotate(180);
        }
        this.direction = direction;
        initialY = y;
    }

    /**
     * Renders the key that is being pressed on to the screen
     * @param input all of the keyboard events
     * @param offset the amount that the screen has shifted vertically
     */
    public void render(ObservableList<Node> children, ArrayList<String> input, double offset) {
        if (direction == Keyboard.LEFT) {
            if (input.contains("LEFT"))     // when the key is being pressed
                button.setImage(Img.keyRightDown);
            else
                button.setImage(Img.keyRightUp);
            button.setRotate(180);
        } else if (direction == Keyboard.RIGHT) {
            if (input.contains("RIGHT"))    // when the key is being pressed
                button.setImage(Img.keyRightDown);
            else
                button.setImage(Img.keyRightUp);
        }
        button.setY(initialY-offset);
        super.render(children);
    }

    /**
     * Removes the arrow keys from the display by removing the nodes from the scene graph
     */
    public void hide(ObservableList<Node> children) {
        if (children.contains(button))
            children.remove(button);
    }
}
