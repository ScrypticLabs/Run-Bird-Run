/**
 * @File: Transporter.java
 * @Author: Abhi Gupta
 * @Description: This class controls when and how the boxes are dropped from above, ensuring that the player has some chance of avoiding
 *               them. Collision detection between boxes themselves is also handled here to prevent boxes from surpassing each other as
 *               they are falling synchronously.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Transporter {
    // DIMENSIONS
    private final double CANVAS_WIDTH;
    private final double PADDING = 0.5;
    private double rowHeight = 485;
    private double boxWidth = 50;
    // Stats
    private final int NUM_OF_BOXES = 8;
    private boolean countInital;
    private int boxesToBeDropped = 0;
    private int AT_REST = 1;
    private int COLUMN_OF_BOXES = NUM_OF_BOXES;
    private int stackNum = 0;
    // Boxes
    private Box[] boxes;            // the total number of boxes to be dropped in the current row (8) as Box objects
    private boolean[] nextBoxes;    // the next set of boxes to be dropped (passed in as an argument when rendering the caution signs)
    private int[][] screen;         // internal map of the game -> 0's represent empty space whereas 1's mean a stationary box is occupying the space
    private int[] onGround;         // the number of boxes on the ground (1's representing no boxes and 0's representing a box)
    private Random random = new Random();
    private ArrayList<Box> totalBoxes = new ArrayList<Box>();

    /**
     * Constructor
     * @param canvasWidth the width of the screen
     */
    public Transporter(double canvasWidth) {
        CANVAS_WIDTH = canvasWidth;
        boxes = new Box[NUM_OF_BOXES];
        nextBoxes = new boolean[NUM_OF_BOXES];
        countInital = false;
        onGround = new int[NUM_OF_BOXES];
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            nextBoxes[i] = random.nextBoolean();
        }
        screen = new int[COLUMN_OF_BOXES][NUM_OF_BOXES];
        importBoxesLinearFill(); // loads a new set of boxes to begin dropping them
    }

    /**
     * Deploys a series of boxes until they have all reached the ground level. When all of the boxes have been dropped to fill up an entire
     * row, new boxes are imported. This process repeats infinitely until a box eventually hits the player causing the game to end.
     * @param children all of the nodes on the scene graph
     * @param time the amount of time that has past since the user started playing the game
     * @param player the bird that the user is operating
     * @param offset the amount by which the screen has shifted vertically
     * @return whether or not the bird was hit by a box and died
     */
    public boolean deploy(ObservableList<Node> children, double time, Bird player, double offset) {
        boolean dead = false;   // the bird's initial state of well-being
        int birdIndexX = player.getMappedX(screen);
        double birdY = player.getY();
        int boxesOnGround = sum(onGround);                      // the number of boxes on the ground
        for (int i = 0; i < NUM_OF_BOXES; i++) {                // when the box is ready to be dropped = prepared
            if (boxes[i].isReady() && screen[1][i] != 1) {      //screen[1][i] limits the boxes from stacking up only as high as the screen array (7 boxes)
                boxes[i].setDropPosition(0 - offset);           // the initial drop position is only set once despite multiple calls to the method
                boxes[i].drop(time);                            // the box is dropped (acceleration added to y-position)

                // Response to Collision with Boxes from Above
                if (i == birdIndexX) {
                    player.setLowerBoundsY(boxes[i].getY() + boxWidth);
                    // removes bird from screen by indicating box on top and bottom collided with it
                    if (birdY <= player.getLowerBoundsY() && (int) birdY == (int) player.getUpperBoundsY())
                        dead = true;
                }
                boxes[i].render(children);                      // the box is rendered on to the screen after updating its y-position
                if (!countInital) boxesToBeDropped++;           // counts the number of boxes that have to be dropped in a single round / drop
                onGround[i] = boxes[i].inMotion() ? 1 : 0;

                // updates the game's internal map by finding which spots are occupied in the map when the boxes have stopped moving
                if (!boxes[i].inMotion()) {
                    double STOP = 400 * stackNum + boxes[i].getSTOP();
                    int y = COLUMN_OF_BOXES - 1 - ((int) ((rowHeight - boxWidth - STOP) / boxWidth) + 1) - stackNum;
                    screen[y][i] = AT_REST;
                }
            }
        }
        if (!countInital) countInital = true;
        if (boxesToBeDropped == boxesOnGround) {        // when the number of boxes that were loaded have been dropped, a new set of boxes
            countInital = false;                        // are loaded and the process repeats
            boxesToBeDropped = 0;
            importBoxes();
        }
        return !dead;   // whether or not alive
    }

    private void importBoxesOriginal() {
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (onGround[i] == 0) {
                totalBoxes.add(boxes[i]);
                boxes[i] = new Box(i * (int) (CANVAS_WIDTH / NUM_OF_BOXES) + PADDING, 0, boxes[i].getSTOP() - 50, i % 2 == 0);
            } else {
                boxes[i].prepare();
            }
            onGround[i] = 1;
        }
    }

    /**
     * Creates new instances of Box to get more boxes once all of them in the current stack have been deployed
     */
    private void importBoxes() {
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (onGround[i] == 0) {     // only makes new instances of the boxes that have settled and are not in the air
                totalBoxes.add(boxes[i]);
                boxes[i] = new Box(i * (int) (CANVAS_WIDTH / NUM_OF_BOXES) + PADDING, 0, boxes[i].getSTOP() - 50, i % 2 == 0);
            } boxes[i].prepare(nextBoxes[i]);
            nextBoxes[i] = random.nextBoolean();
            onGround[i] = 1;
        }
        ensureNotAllPrepared();                                 // makes sure not an entire row of boxes fall (the user will inevitably lose)
    }

    public void deployLinearFill(GraphicsContext gc, double time) {
        int boxesOnGround = sum(onGround);
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (boxes[i].isReady()) {
                boxes[i].drop(time);
                boxes[i].render(gc);
                if (!countInital) boxesToBeDropped++;
                onGround[i] = boxes[i].inMotion() ? 1 : 0;
            }
        }
        if (!countInital) countInital = true;
        if (boxesToBeDropped == boxesOnGround) resetRoundLinearFill();
        if (boxesOnGround == NUM_OF_BOXES) importBoxesLinearFill();
    }

    public void resetRoundLinearFill() {
        countInital = false;
        boxesToBeDropped = 0;
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (onGround[i] == 1) {
                boxes[i].prepare();
            }
        }
    }

    private void importBoxesLinearFill() {
        rowHeight -= boxWidth;
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            onGround[i] = 1;
            boxes[i] = new Box(i*(int)(CANVAS_WIDTH/NUM_OF_BOXES)+PADDING,0,rowHeight, i % 2 == 0);
            totalBoxes.add(boxes[i]);
//            if (i == 3)
//                boxes[i].readyToDrop = true;
//            else
//                boxes[i].readyToDrop = false;
        }
    }

    private void ensureNotAllPrepared() {
        boolean all = true;
        for (Box box : boxes) {
            if (!box.isReady()) {
                all = false; return;
            }
        } if (all) {
            for (int i = 0; i < NUM_OF_BOXES; i++) {
                if (random.nextBoolean()) {
                    boxes[i].unprepare();
                    nextBoxes[i] = false;
                }
            }
        }
    }

    /**
     * Returns the internal map of the game or all of the boxes that are occupying the screen
     */
    public int[][] getHazards() {
        return screen;
    }

    /**
     * Checks for collision between boxes that are still in the air and the bird's MAPPED x and y positions
     */
    public int[] getActiveBoxBounds(int birdMappedX, int birdMappedY) {
        int[] boxBounds = new int[2];
        int counter = 1;
        int leftX = -1; int rightX = -1;
        boolean leftFound = false; boolean rightFound = false;

        while (!leftFound && !rightFound) {     // flood-fill search algorithm, looks for a box in the air to the left and right of the bird until one
                                                // is found and sets the birds position thresholds to those boxes' x-positions
            if (birdMappedX-counter >= 0) {
                if (boxes[birdMappedX-counter].inMotion() && getMappedY(boxes[birdMappedX-counter].getY()) == birdMappedY) {
                    leftX = birdMappedX-counter; leftFound = true;
                }
            }
            if (birdMappedX+counter <= NUM_OF_BOXES-1) {
                if (boxes[birdMappedX+counter].inMotion() && getMappedY(boxes[birdMappedX+counter].getY()) == birdMappedY) {
                    rightX = birdMappedX+counter; rightFound = true;
                }
            }
            if (birdMappedX-counter < 0 && birdMappedX+counter > NUM_OF_BOXES-1) break;
            counter++;
        }
        boxBounds[0] = leftX != -1 ? (int)(leftX*boxWidth+boxWidth) : -1;
        boxBounds[1] = rightX != -1 ? (int)(rightX*boxWidth) : -1;
        return boxBounds;

    }

    /**
     * Maps a box's y-position to an index that can be used to reference the internal map's y-position
     */
    public int getMappedY(double boxY) {
        int height = NUM_OF_BOXES - ((int)((rowHeight - boxY + 10) / boxWidth) + 1); //-50 to boxy for debugging
        return height > 7 ? 7 : height;
    }

    public int getStackMinHeight() {
        int y = 0;
        boolean allPresent;
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            allPresent = true;
            for (int j = 0; j < NUM_OF_BOXES; j++) {
                if (screen[i][j] == 0) {
                    allPresent = false;
                    break;
                }
            }
            if (allPresent) {
                y = i;
                break;
            }
        }
        return NUM_OF_BOXES-y;
    }

    public int getNumOfBoxes() { return NUM_OF_BOXES; }

    private void outputScreen() {
        for (int i = 0; i < screen.length; i++) {
            for (int j = 0; j < screen[i].length; j++) {
                System.out.print(screen[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("---------------");
    }

    /**
     * Finds the sum of a primitive integer array
     */
    private int sum(int[] nums) {
        int sum = 0;
        for (int num : nums) sum += num;
        return NUM_OF_BOXES-sum;
    }

    /**
     * Returns general information about the transporter such as the number of boxes it carries, each box's width and the maximum row height (AKA box height)
     */
    public int[] getInfo() {
        return new int[]{NUM_OF_BOXES,(int)(rowHeight+boxWidth),(int)boxWidth};
    }

    /**
     * Resets the internal map every level
     */
    public boolean reset() {
        boolean topFull = true;
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (screen[1][i] != 1) {
                topFull = false;
                break;
            }
        } if (topFull) {
            countInital = false;
            boxesToBeDropped = 0;
            importBoxes();
            stackNum++;
            screen = new int[COLUMN_OF_BOXES][NUM_OF_BOXES];
        }
        return topFull;
    }

    public boolean[] getNextBoxes() { return nextBoxes; }

    public boolean allOnGround() { return sum(onGround) == boxesToBeDropped; }

    public int amountOnGround() { return sum(onGround); }

    /**
     * Resets this class' variables to their default values
     */
    public void newGame() {
        rowHeight = 485;
        boxWidth = 50;
        boxesToBeDropped = 0;
        AT_REST = 1;
        COLUMN_OF_BOXES = NUM_OF_BOXES;
        stackNum = 0;
        totalBoxes = new ArrayList<Box>();
        boxes = new Box[NUM_OF_BOXES];
        nextBoxes = new boolean[NUM_OF_BOXES];
        countInital = false;
        onGround = new int[NUM_OF_BOXES];
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            nextBoxes[i] = random.nextBoolean();
        }
        screen = new int[COLUMN_OF_BOXES][NUM_OF_BOXES];
        importBoxesLinearFill();
    }

    public void removeBoxesFromGraph(ObservableList<Node> children) {
        for (Box box : totalBoxes) {
            if (children.contains(box.getImageView()))
                children.remove(box.getImageView());
        }
    }
}


