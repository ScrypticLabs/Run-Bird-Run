/**
 * @File: Main.java
 * @Author: Abhi Gupta
 * @Description: This class runs the game 'Run Bird Run' whose objective is to avoid the falling boxes and to survive for the longest to
 *               get the best score. You can use the arrow or A and D keys to move left or right, and there are caution signs displayed
 *               at the top of the game showing where the next set of boxes will fall from. Although in the beginning they will appear
 *               fairly early, as the game progresses these signs will not show up as much in advance as before. Also, these signs should
 *               not always be trusted as they may try to confuse the player as the difficulty rises. Moreover, to further increase the
 *               difficulty of this game, the bird that the player will be operating will have a greater tendency to 'stick' on to the walls
 *               of some boxes, resulting in a potential death.
 */

package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;

public class Main extends Application {
    // Drawing Panels
    Group root;
    Canvas canvas;
    GraphicsContext gc;

    double screenY = 0;                     // the offset by which the screen's y position has increased
    int goal = 0;                           // the goal (amount) that the offset needs to reach
    int maxHeightBeforeTransition = 7;      // the height (units is boxes (3 box high)) of the boxes before the screen shifts down rapidly
    boolean transition = false;             // whether or not the screen is in transition
    boolean reset = false;                  // whether or not a new game has to be started
    double waitPriorToSigns = 300;          // the delay time before the caution signs are displayed (progessively decreases)
    boolean win = true;                     // so that the game starts at the menu
    boolean lost = false;
    int menuWaitTime = 0;                   // the delay time before the user can play again or start a new game

    /**
     * The default method called by JavaFX API to render the game on to the screen.
     * @param primaryStage the stage that the game will be rendered on
     */
    @Override
    public void start(Stage primaryStage) {
        root = new Group();
        Scene scene = new Scene(root,400,650);
        scene.getStylesheets().add(this.getClass().getResource("styleSheet.css").toExternalForm());
        // background gradient
        scene.setFill(new LinearGradient(0, 0, 1, 1, true,
                CycleMethod.REFLECT,
                new Stop(0.0, Colour.AQUA_BLUE),
                new Stop(1.0, Colour.TURQUOISE)));
        canvas = new Canvas(400,650);
        root.getChildren().add(canvas);         // only foreground is rendered on root, as it is easier to organize the layout of its children
        gc = canvas.getGraphicsContext2D();     // only background is rendered on canvas, as it takes the back-most view by default

        // Menu
        ImageView menu = new ImageView(Img.menu);
        menu.setX(canvas.getWidth()/2-155);
        menu.setY(0);

        // Boxes
        Transporter fedEx = new Transporter(canvas.getWidth());

        // Traffic Signs Controller
        TrafficController trafficGuard = new TrafficController(fedEx.getNumOfBoxes());

        // Score Keeper
        ScoreKeeper referee = new ScoreKeeper();

        // Players
        Bird player = new Bird(Img.birdRight, fedEx.getInfo());
        player.setPosition(300,439);

        // Keyboard Input
        ArrayList<String> input = new ArrayList<String>();
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent e) {
                        String code = e.getCode().toString();
                        if (!input.contains(code)) {
                            input.add(code);
                        }
                    }
                });
        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent e) {
                        String code = e.getCode().toString();
                        input.remove(code);
                    }
                });

        // Mouse Input
        scene.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent e) {
                        System.out.println();
                    }
                });

        // Main Game Loop
        Timeline gameLoop = new Timeline();
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        long timeStart = System.currentTimeMillis();        // the time when the game began
        KeyFrame kf = new KeyFrame(
                Duration.seconds(0.017), new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent ae) {
                        double t = (System.currentTimeMillis() - timeStart) / 1000.0;       // the amount of time that has progressed since the game began

                        // Clear the canvas
                        gc.clearRect(0, 0, 400,600);
                        // Redraw on Canvas
                        gc.drawImage(Img.bigClouds, 0-(t*8), 70);
                        drawBackground();                                                   // background is rendered even when the user is not playing to present the animations of the clouds
                        gc.drawImage(Img.smallClouds, 0-(t*15), 267);

                        // when the user hasn't win or lost the game -> playing the game
                        if (!win && !lost) {
                            // All of the settled boxes currently on the screen
                            int[][] hazards = fedEx.getHazards();

                            // Moves the screen down with growing progression
                            screenY += 0.122 * (t / 100);
                            if (reset) {
                                transition = true;
                                goal = (int) (screenY + 100 + (50 * (t / 100)));
                            }
                            if (transition) {
                                if ((int) screenY != goal) {
                                    screenY += 0.5;
                                } else {
                                    transition = false;
                                }
                            } scrollScreen(root.getChildren());

                            // Moves the player
                            if (player.isAlive()) {
                                // Collision (for stationary boxes)
                                boolean collide = player.checkCollision(hazards, fedEx.getActiveBoxBounds(player.getMappedX(hazards), player.getMappedY(hazards)));
                                // Bird Movement
                                if ((input.contains("LEFT") || input.contains("A")) && !collide) {
                                    player.move(Path.LEFT);
                                    player.rotate(0);
                                } else if ((input.contains("LEFT") || input.contains("A")) && collide) {
                                    player.deactivateG();
                                    player.move(Path.UP);
                                    player.move(Path.LEFT);
                                    player.rotate(90);
                                }
                                if ((input.contains("RIGHT") || input.contains("D")) && !collide) {
                                    player.move(Path.RIGHT);
                                    player.rotate(0);
                                } else if ((input.contains("RIGHT") || input.contains("D")) && collide) {
                                    player.deactivateG();
                                    player.move(Path.UP);
                                    player.move(Path.RIGHT);
                                    player.rotate(-90);
                                }
                                if (collide && input.isEmpty() || !collide) {
                                    player.activateG();
                                }
                                if (input.isEmpty())
                                    player.move(Path.STILL);
                                player.move(Path.DOWN);
                            }
                            player.update();    // updates the bird's position with regards to acceleration and velocity

                            // Check health of player
                            if (player.isAlive()) {
                                player.render(root.getChildren());  // render's player only if the bird is alive
                            } else if (fedEx.allOnGround()) {       // when the bird is dead, it waits for all the boxes to stop before going to the menu
                                win = false;
                                lost = true;
                            } if (!player.isAlive() && root.getChildren().contains(player.getImageView())) {
                                // ACTIVATE SPRITE ANIMATION OF BIRD BEING COMPRESSED BY BOX
                                root.getChildren().remove(player.getImageView());       // removes the bird from the screen as it is dead
                            }
                            player.updateLife(fedEx.deploy(root.getChildren(), t, player, screenY));    // updates the bird's status of whether or not it is alive

                            // Display Score Information
                            referee.renderScoreBoard(root.getChildren(), input, screenY);

                            // Display Caution Signs
                            if (fedEx.allOnGround()) {
                                waitPriorToSigns = 50;
                                referee.upScore(fedEx.amountOnGround());
                            }
                            waitPriorToSigns = waitPriorToSigns > 0 ? waitPriorToSigns - 1 : waitPriorToSigns;
                            if ((int) waitPriorToSigns == 0) {
                                trafficGuard.displayWarnings(root.getChildren(), fedEx.getNextBoxes(), fedEx.allOnGround(), screenY);
                            } else {
                                trafficGuard.hideWarnings(root.getChildren());
                            }

                            // Check if boxes have stacked up to a height of 7 boxes -> new level
                            if (fedEx.reset()) {
                                player.upGroundPosY(50 * 7);
                                referee.upLevel();
                                reset = true;
                            } else {
                                reset = false;
                            }
                            win = referee.isWinner();

                        // Reset Game to Play Again - hit the arrow keys
                        } else if ((input.contains("RIGHT") || input.contains("LEFT")) && menuWaitTime >= 60) {
                            // Reset all internal variables and discard previous data
                            // Boxes
                            fedEx.removeBoxesFromGraph(root.getChildren());
                            fedEx.newGame();
                            // Traffic Signs Controller
                            trafficGuard.newGame();
                            // Score Keeper
                            referee.removeBoardFromGraph(root.getChildren());
                            referee.newGame();
                            // Players
                            player.newGame();
                            player.setPosition(300,439);
                            screenY = 0;
                            root.getChildren().remove(menu);        // hides the menu from the screen
                            menuWaitTime = 0;
//                            timeStart = System.currentTimeMillis();
                            win = lost = false;

                        // Display Menu after Winning or Losing
                        } else {
                            menuWaitTime++;
                            if (!root.getChildren().contains(menu))
                                root.getChildren().add(menu);
                                referee.displayScore(root.getChildren());
                        }
                    }
                });
        gameLoop.getKeyFrames().add(kf);
        gameLoop.play();

        // Window Settings
        primaryStage.setTitle("Run Bird Run!");
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.centerOnScreen();
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Shifts the screen down by a constant value
     * @param children all of the nodes that need to be shifted
     */
    private void scrollScreen(ObservableList<Node> children) {
       if (children.size() >= 2) {
           for (Node child : children) {
               child.setTranslateY(screenY);
           }
       }
   }

    /**
     * Draws all of the elements of the background including the trees and the hills
     */
    private void drawBackground() {
        drawBase();
        drawGrass();
        drawTree(195,canvas.getHeight()-170,205,575,Colour.DARK_GREEN,Colour.FOREST_GREEN);
        drawTree(285,canvas.getHeight()-170,150,520,Colour.BRIGHT_GREEN,Colour.DEEP_GREEN);
        drawTree(-16,canvas.getHeight()-170,150,530,Colour.DARK_GREEN,Colour.FOREST_GREEN);
        drawTree(50,canvas.getHeight()-170,130,470,Colour.BRIGHT_GREEN,Colour.DEEP_GREEN);
    }

    /**
     * Draws and renders the grass spikes
     */
    private void drawGrass() {
        for (int i = 0; i < 9; i++) {
            int offset = i*50-35;
            double lastXPoint = 50+offset > canvas.getWidth() ? canvas.getWidth() : 50+offset;
            double lastYPoint = 50+offset > canvas.getWidth() ? canvas.getHeight()-145 : canvas.getHeight()-145;
            gc.setFill(Colour.LIME);
            gc.fillPolygon(new double[]{offset,25+offset,50+offset}, new double[]{canvas.getHeight()-145,canvas.getHeight()-120,canvas.getHeight()-145},3);
//            Polygon triangle = new Polygon(offset,canvas.getHeight()-145,25+offset,canvas.getHeight()-120);
//            if (50+offset > canvas.getWidth()) {
//                triangle.getPoints().addAll(lastXPoint,lastYPoint+12);
//            } triangle.getPoints().addAll(lastXPoint,lastYPoint);
//            triangle.setFill(Colour.LIME);
//            root.getChildren().add(triangle);
        }
    }

    /**
     * Draws and renders the grass layer
     */
    private void drawBase() {
        gc.setFill(Colour.GRASS_GREEN);
        gc.fillRect(0,canvas.getHeight()-145,canvas.getWidth(),145);
        gc.setFill(Colour.LIME);
        gc.fillRect(0,canvas.getHeight()-155,canvas.getWidth(),10);
        gc.setFill(Colour.ELECTRIC_GREEN);
        gc.fillRect(0,canvas.getHeight()-170,canvas.getWidth(),15);
    }

    /**
     * Draws and renders a tree of specified dimensions
     * @param x the x position of the tree
     * @param y the y position of the tree
     * @param width the width of the tree
     * @param height the height of the tree
     * @param left the colour of the left side of the tree
     * @param right the colour of the right side of the tree
     */
    private void drawTree(double x, double y, double width, double height, Color left, Color right) {
        double tempWidth = width < 120 ? 60 : width;
        double offset = 6; double offsetY = Math.pow(tempWidth,2)/1000;
        if (height > 400) {
            offset = 8;
        } else if (height <= 350) {
            offset = 4;
        }
        gc.setFill(left);
        // Base L
        gc.fillPolygon(new double[]{x,x+width/2,x+width/2}, new double[]{y,canvas.getHeight()-height,y},3);
        // Tip L
        gc.fillPolygon(new double[]{x+width/offset,x+width/2,x+width/2}, new double[]{y*3/4-offsetY,canvas.getHeight()-height,y*3/4-offsetY},3);

        gc.setFill(right);
        // Base R
        gc.fillPolygon(new double[]{x+width/2,x+width/2,x+width}, new double[]{y,canvas.getHeight()-height,y},3);
        // Tip R
        gc.fillPolygon(new double[]{x+width/2,x+width/2,x+width*((offset-1)/offset)}, new double[]{y*3/4-offsetY,canvas.getHeight()-height,y*3/4-offsetY},3);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
