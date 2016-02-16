/**
 * @File: ScoreKeeper.java
 * @Author: Abhi Gupta
 * @Description: This class keeps track of the user's score and other related information for the duration of a single game. When a game is over,
 *               this class is also responsible for rendering the menu and providing on-screen instructions for starting a new game. The arrow keys
 *               and earned points that are displayed while playing the game are handled by this class as well.
 */

package sample;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

public class ScoreKeeper {
    private int level = 1;
    private int score = 0;
    private Key keyRight;
    private Key keyLeft;
    private Label text;
    private Label menuText;
    private int textInitialY;
    private int textInitialX = 180;
    private boolean case2 = false; private boolean case3 = false; // these are special cases to center the text when it is certain characters long
    private int count = 0;
    private int scoreToBeAdded = 0;
    private boolean alreadySet = false;

    public ScoreKeeper() {
        keyRight = new Key(Keyboard.RIGHT, 310, 575);
        keyLeft = new Key(Keyboard.LEFT, 25, 575);
        text = new Label(""+score);
        textInitialY = 547;
        text.setLayoutX(textInitialX);
        text.setLayoutY(textInitialY);
        text.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,70));
        text.setTextFill(Colour.semiWhite);

        menuText = new Label(""+score);
        if (menuText.getText().length() == 1)
            menuText.setLayoutX(190);
        else
            menuText.setLayoutX(190-14);
        menuText.setLayoutY(302);
        menuText.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,40));
        menuText.setTextFill(Color.WHITE);

    }

    public void renderScoreBoard(ObservableList<Node> children, ArrayList<String> input, double offset) {
        incrementScore();
        renderText(children, offset);
        keyRight.render(children,input,offset);
        keyLeft.render(children,input,offset);
    }

    private void incrementScore() {
        count++;
        if (count % 8 == 0 && scoreToBeAdded > 0) {
            score++;
            scoreToBeAdded--;
        } if (scoreToBeAdded == 0) alreadySet = false;
    }

    public void upLevel() { level++; }

    public void renderText(ObservableList<Node> children, double offset) {
        if (children.contains(text)) children.remove(text);
        text.setLayoutY(textInitialY-offset);
        String score = this.score+"";
        text.setText(score);
        if (score.length() == 2 && !case2) {
            text.setLayoutX(textInitialX-26);
            case2 = true;
        } else if (score.length() == 3 && !case3) {
//            text.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,65));
            text.setLayoutX(textInitialX-48);
            case3 = true;
        }
        children.add(text);
    }

    public void upScore(int amount) {
        if (!alreadySet) {
            scoreToBeAdded = amount;
            alreadySet = true;
        }

    }

    public boolean isWinner() {
        return level == 5;
    }

    public void newGame() {
        level = 1;
        score = 0;
        case2 = false; case3 = false;
        count = 0;
        scoreToBeAdded = 0;
        alreadySet = false;
        text = new Label(""+score);
        text.setLayoutX(textInitialX);
        text.setLayoutY(textInitialY);
        text.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,70));
        text.setTextFill(Colour.semiWhite);

        menuText = new Label(""+score);
        if (menuText.getText().length() == 1)
            menuText.setLayoutX(190);
        else
            menuText.setLayoutX(190-14);
        menuText.setLayoutY(302);
        menuText.setFont(Font.font("Comic Sans MS",FontWeight.BOLD,40));
        menuText.setTextFill(Color.WHITE);
    }

    public void removeBoardFromGraph(ObservableList<Node> children) {
        keyLeft.hide(children);
        keyRight.hide(children);
        if (children.contains(text))
            children.remove(text);
        if (children.contains(menuText)) {
            children.remove(menuText);
        }
    }

    public void displayScore(ObservableList<Node> children) {
        menuText.setText(score+"");
        if (menuText.getText().length() == 1)
            menuText.setLayoutX(190);
        else
            menuText.setLayoutX(190-14);
        if (!children.contains(menuText))
            children.add(menuText);
    }
}
