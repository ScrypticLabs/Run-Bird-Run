/**
 * @File: Bird.java
 * @Author: Abhi Gupta
 * @Description: This class defines the way a bird moves through the game and checks for collision with other boxes in its environment.
 *               It also contains some general properties of the bird such as its colour, size and state of well-being.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Bird extends Sprite {
    // Motion Thresholds
    private final double A = 1;
    private final double MAX_VELOCITY = 8;
    private final double G = 0.45;
    public double GForce = A+G;
    // Position Thresholds
    private double lowerBoundX = -20;
    private double lowerBoundY = 80;
    private double upperBoundX;
    private double upperBoundY = 439;
    // Stats
    private boolean slowed = false;
    private ImageView bird;
    private boolean alive = true;
    private boolean checkAgain = true;
    // Data about Transporter
    private final int NUM_OF_BOXES;
    private final int COLUMN_OF_BOXES;
    private int rowHeight;
    private final int boxWidth;
    private int groundPosY = 435;       // the first set of boxes will rest at this level (ground level)
    private int stackNum = 0;           // the number of stacks of boxes on the screen

    /**
     * Constructor
     * @param bird the bird image to be rendered on the screen
     * @param transporterInfo basic properties of the boxes that will fall down to allow for accurate collision detection
     */
    public Bird(Image bird, int[] transporterInfo) {
        super();
        setImage(bird);
        this.bird = new ImageView(image);
        upperBoundX = 400-image.getWidth()+20;   // 20 is padding for beak
        NUM_OF_BOXES = transporterInfo[0];
        COLUMN_OF_BOXES = NUM_OF_BOXES;
        rowHeight = transporterInfo[1];
        boxWidth = transporterInfo[2];
    }

    /**
     * Disables gravity acting on the bird in the scene graph
     */
    public void deactivateG() {
        GForce = 0;
    }

    /**
     * Activates gravity acting on the bird in the scene graph
     */
    public void activateG() {
        GForce = 1.6*(A+G);
    }

    /**
     * Moves the bird in the specified direction or path
     * @param path the direction that the bird is moving in
     */
    public void move(Path path) {
        switch (path) {
            case RIGHT:
                addVelocity(A,0);
                velocityX = Math.min(MAX_VELOCITY,velocityX);
                bird.setImage(Img.birdRight);
                break;
            case LEFT:
                addVelocity(-A,0);
                velocityX = Math.max(-MAX_VELOCITY,velocityX);
                bird.setImage(Img.birdLeft);
                break;
            case UP:
                addVelocity(0,G-A);
                break;
            case DOWN:
                addVelocity(0,GForce);
                break;
            default:
                // the bird progressively slows down rather than an abrupt hault
                if (velocityX > 0) {
                    addVelocity(-A,0);
                } else if (velocityX < 0) {
                    addVelocity(A,0);
                } else {
                    velocityY = 0;
                }
        }
    }

    /**
     * Sets the highest y-position that the bird can move up to.
     * @param y the y-position threshold
     */
    public void setLowerBoundsY(double y) { lowerBoundY = y; }

    /**
     * Sets the lowest y-position that the bird can move up to.
     * @param y the y-position threshold
     */
    public void setUpperBoundsY(double y) { upperBoundY = y; }

    /**
     * Sets the left-most x-position that the bird can move to.
     * @param x the x-position threshold
     */
    public void setLowerBoundsX(double x) { lowerBoundX = x; }

    /**
     * Sets the right-most x-position that the bird can move to.
     * @param x the x-position threshold
     */
    public void setUpperBoundsX(double x) { upperBoundX = x; }

    public double getLowerBoundsY() { return lowerBoundY; }

    public double getUpperBoundsY() { return upperBoundY; }

    public double getLowerBoundsX() { return lowerBoundX; }

    public double getUpperBoundsX() { return upperBoundX; }

    /**
     * Rotates the bird to the specified angle
     * @param angle the amount of rotation
     */
    public void rotate(int angle) {
        bird.setRotate(angle);
    }

    /**
     * Updates the bird's position ensuring that it is within the position thresholds
     */
    @Override
    public void update() {
        super.update();
        positionX = Math.min(Math.max(positionX,lowerBoundX),upperBoundX);
        positionY = Math.min(Math.max(positionY,lowerBoundY),upperBoundY);
        bird.setX(positionX); bird.setY(positionY);
    }

    /**
     * Determines if the bird is colliding with a box on the screen
     * @param boxes all of the stationary boxes on the screen
     * @param boxBounds the left and right most position thresholds of the boxes that the bird is in between
     * @return whether or not the bird is colliding with a box
     */
    public boolean checkCollision(int[][] boxes, int[] boxBounds) {
        double y = positionY+image.getHeight()-boxWidth + (stackNum*350);   // the bird's y position if it were treated as a box
        int index = COLUMN_OF_BOXES - ((int) (rowHeight - y) / boxWidth);   // maps the y-position to an index that will be used to interface with the game's internal map of the boxes (2d array)
        // bird doesnt go up issue here
        if (index >= 0) {
            int[] row = boxes[index];        // the row index in boxes (how high up)
            // Now that u know where each box is occurring and its location
            // You must map the bird's x position to a corresponding index and set an independent ground level for each box
            applyNormalForce(boxes);        // ensures the bird doesn't fall through the boxes by setting a position-y threshold

            // Collision detection between active boxes to the left and right of the bird
            int leftBoxBounds = boxBounds[0];
            int rightBoxBounds = boxBounds[1];
            if (leftBoxBounds != -1) {                   // -1 implies there is no box in motion to the left of the bird
                if (positionX + 8 <= leftBoxBounds) {    // 8 is padding for the tail
                    lowerBoundX = leftBoxBounds - 8;
                }
            }
            if (rightBoxBounds != -1) {                 // 9 is padding for the beak
                if (positionX + image.getWidth() - 9 >= rightBoxBounds) {
                    upperBoundX = rightBoxBounds - image.getWidth() + 9;
                }
            }
            // Collision detection between stationary boxes to the left and right of the bird
            for (int i = 0; i < NUM_OF_BOXES; i++) {
                int box = row[i];
                double boxPositionX = i * 50;

                if (box == 1) {
                    // Bird Right of Box
                    if (positionX + image.getWidth() > boxPositionX + boxWidth) {
                        if (positionX < boxPositionX + boxWidth) {
                            lowerBoundX = boxPositionX + boxWidth - 8;                  // the x position should start from the body of the bird, not the tail
                            return true;
                        } else if (positionX > boxPositionX + boxWidth) {               // avoid jittery movement by doing > instead of >=
                            lowerBoundX = -20;
                        }
                    } // Bird Left of Box
                    else if (positionX + 9 < boxPositionX) {
                        if (positionX + image.getWidth() > boxPositionX) {            // the width shouldn't include the beak of the bird, only the body (hence the +9)
                            upperBoundX = boxPositionX - image.getWidth() + 9;        // because when update() is called, it takes min of posx and upperbound even though we are checking the rightmost corner of bird
                            return true;
                        } else if (positionX + image.getWidth() < boxPositionX) {
                            upperBoundX = 400 - image.getWidth() + 20;
                            break;
                        }
                    } else {    // restores the default thresholds
                        lowerBoundX = -20;
                        upperBoundX = 400 - image.getWidth() + 20;
                    }
                  // restores the default thresholds when there is no box colliding in this direction
                } else if (box == 0) {
                    if (lowerBoundX == boxPositionX + boxWidth - 8 && leftBoxBounds == -1)
                        lowerBoundX = -20;
                    if (upperBoundX == boxPositionX - image.getWidth() + 9 && rightBoxBounds == -1)
                        upperBoundX = 400 - image.getWidth() + 20;
                }
            }
        }
        return false;
    }

    /**
     * Converts the bird's x-position to an index to be used with game's internal map
     * @param boxes the game's internal map of all of the boxes (hazards)
     * @return the index that corresponds to the bird's x-position on the map
     */
    public int getMappedX(int[][] boxes) {
        int mappedIndex = 0;                                    // the index of the row corresponding to the bird's x position
        double positionX = this.positionX+9;
        for (int i = 0; i < boxes.length; i++) {
            if (((i*50-22 <= positionX && ((i+1)*50)-25 > positionX))) {
                mappedIndex = i;
                break;
            } else if (((i+1)*50)-25 <= positionX && (i+1)*50 > positionX) {
                mappedIndex = i + 1;
                break;
            }
        }
        return mappedIndex;

    }

    /**
     * Converts the bird's y-position to an index to be used with game's internal map
     * @param boxes the game's internal map of all of the boxes (hazards)
     * @return the index that corresponds to the bird's y-position on the map
     */
    public int getMappedY(int[][] boxes) {
        int NUM_OF_BOXES = boxes[0].length;
        int boxWidth = 50;
        int rowHeight = 485;
        double y = positionY+image.getHeight()-boxWidth;
        int height = NUM_OF_BOXES - ((int) (rowHeight - y) / boxWidth) + 1;
        return height > 7 ? 7 : height;
//        if (boxes[height][groundLevel] == 0) {  // when there is no box under the bird, it will move down
//            move(Path.DOWN);
//        } else {
//            velocityY = 0;
//        }
    }

    /**
     * Everytime an entire layer of boxes fill up the ground level y-position is updated
     * @param y the amount to be decreased to obtain the new ground level
     */
    public void upGroundPosY(int y) {
        stackNum++;
        groundPosY -= y;
    }

    /**
     * Ensure the bird doesn't fall through the boxes by updating the position-y threshold based on the bird's x and y position on the screen
     * @param boxes the internal map of all of the stationary boxes on the screen
     */
    private void applyNormalForce(int[][] boxes) {
        int groundLevel = getMappedX(boxes);
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (boxes[i][groundLevel] == 1) {
                upperBoundY = groundPosY+4 - (NUM_OF_BOXES - i) * boxWidth;
                break;
            } else {
                upperBoundY = groundPosY+4;
            }
        }
    }

    /**
     * Adds the bird as a node to the scene graph to render the player
     * @param children all of the nodes of the scene graph
     */
    public void render(ObservableList<Node> children) {
        if (children.contains(bird)) children.remove(bird);
        children.add(bird);
    }

    public void slowMotion() {
        slowed = true;
    }

    private double getAccel() {
        if (slowed)
            return A/3;
        else
            return A;
    }

    public boolean isAlive() {
        return alive;
    }

    /**
     * Checks if the bird is not already dead before determining if it's alive or not
     */
    public void updateLife(boolean alive) {
        if (!alive && checkAgain) {
            this.alive = alive;
            checkAgain = false;
        }

    }

    public ImageView getImageView() { return this.bird; }

    /**
     * Resets all of the bird's variables to the default settings
     */
    public void newGame() {
        super.newGame();
        lowerBoundX = -20;
        lowerBoundY = 80;
        upperBoundX = 400-image.getWidth()+20;   // 5 is padding for beak
        upperBoundY = 439;
        slowed = false;
        ImageView bird;
        alive = true;
        checkAgain = true;
        groundPosY = 435;
        stackNum = 0;
    }

}
