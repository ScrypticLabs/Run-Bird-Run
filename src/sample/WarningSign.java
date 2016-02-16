/**
 * @File: WarningSign.java
 * @Author: Abhi Gupta
 * @Description: This class handles displaying each individual caution sign on its own. When the warning sign is rendered, it will
 *               alternate between two images every so often based on whether or not it is specified to flicker.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class WarningSign extends Sprite {
    private ImageView sign;

    public WarningSign(double x, double y) {
        super();
        setImage(Img.warningSignOff);
        setPosition(x, y);
        sign = new ImageView(image);
        sign.setX(positionX);
    }

    public void setY(double y) { sign.setY(y-5); }

    public ImageView getImageView() { return sign; }

    public void render(ObservableList<Node> children, boolean flicker) {
        if (children.contains(sign)) children.remove(sign);
        setImage(getSign(flicker));
        sign.setImage(image);
        children.add(sign);
    }

    private Image getSign(boolean n) {
        return n ? Img.warningSignOff : Img.warningSignOn;
    }

    public void hide(ObservableList<Node> children) {
        if (children.contains(sign))
            children.remove(sign);
    }
}
