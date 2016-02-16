/**
 * @File: Button.java
 * @Author: Abhi Gupta
 * @Description: This class is nearly identical to the Sprite class but instead renders the object as a node on to the
 *               scene graph rather than the GraphicsContext and removes the sprite animation features as a button doesn't require it.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Button extends Sprite {
    ImageView button;

    public Button(Image icon, double x, double y) {
        super();
        setImage(icon);
        button = new ImageView(image);
        button.setX(x); button.setY(y);
    }

    public void render(ObservableList<Node> children) {
        if (children.contains(button)) children.remove(button);
        children.add(button);
    }

}
