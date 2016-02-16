/**
 * @File: TrafficController.java
 * @Author: Abhi Gupta
 * @Description: This class controls when and how the caution signs appear to notify the user of where the next set of boxes
 *               will be dropping from.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;

public class TrafficController {
    // Warning Signs
    private final int NUM_OF_BOXES;
    private WarningSign[] warningSigns;
    private int counter = 0;    // delay time between each flicker
    private boolean flicker = false;


    public TrafficController(int boxes) {
        // boxes is the number of boxes that can fit in a single row (8)
        NUM_OF_BOXES = boxes;
        warningSigns = new WarningSign[NUM_OF_BOXES];
        // creates the number of sign as the number of boxes
        for (int i = 0; i < NUM_OF_BOXES; i++)
            warningSigns[i] = new WarningSign(i*50-2,0);
    }

    /**
     * Renders the caution signs on the screen based on what boxes are about to appear next
     * @param signLocations each index represents whether or not a caution sign is needed at that location
     * @param firstSetOnGround whether or not the initial set of boxes have landed
     * @param offset the amount by which the screen has shifted vertically
     */
    public void displayWarnings(ObservableList<Node> children, boolean[] signLocations, boolean firstSetOnGround, double offset) {
        for (int i = 0; i < NUM_OF_BOXES; i++) {
            if (signLocations[i]) {
                warningSigns[i].setY(0 - offset);
                warningSigns[i].render(children,flicker);
            } else {
                warningSigns[i].hide(children);
            }
        }
        counter++;     // the signs render in a way so that two images for each sign are alternating every 15 counts
        if (counter % 15 == 0) {
            flicker = !flicker;
        }
    }

    /**
     * Removes all of the caution signs from the scene graph
     */
    public void hideWarnings(ObservableList<Node> children) {
        counter = 0;
        for (WarningSign sign : warningSigns) {
            sign.hide(children);
        }
    }

    /**
     * Resets all of the variables
     */
    public void newGame() {
        counter = 0;
        flicker = false;
        warningSigns = new WarningSign[NUM_OF_BOXES];
        for (int i = 0; i < NUM_OF_BOXES; i++)
            warningSigns[i] = new WarningSign(i*50-2,0);
    }
}
