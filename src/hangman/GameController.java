package hangman;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;

public class GameController {

    private final ExecutorService executorService;
    private final Game game;

    public GameController(Game game) {
        this.game = game;
        executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });
    }

    @FXML
    private VBox board ;
    @FXML
    private Label statusLabel ;
    @FXML
    private Label enterALetterLabel ;
    @FXML
    private TextField textField ;
    @FXML
    private Label answerLabel;
    @FXML
    private Label playerAnswerLabel;
    @FXML
    private Label badGuesses;
    @FXML
    private Label updateAnswerLabel;
    @FXML
    private Label updateBadGuesses;
    @FXML
    private Label numOfMovesLeft;
    @FXML
    private Label updateYourAnswerLabel;


    /**The initialize method is invoked after all the @FXML annotated members have been injected.*/
    public void initialize() throws IOException {
        System.out.println("in initialize");
        drawHangman(0);
        addTextBoxListener();
        setUpStatusLabelBindings();
    }

    private void addTextBoxListener() {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if(newValue.length() > 0 && isValidChar(newValue.charAt(0))) {
                    System.out.print(newValue);
                    int badmoves = game.makeMove(newValue);
                    updateBadGuesses.textProperty().bind(Bindings.format("%s", game.getUpdateBadGuesses()));
                    updateAnswerLabel.textProperty().bind(Bindings.format("%s", game.getTmpAnswer()));
                    numOfMovesLeft.textProperty().bind(Bindings.format("%s", "You have " + (game.numOfTries()-game.getBadmoves()) + " moves left."));
                    textField.clear();
                    drawHangman(badmoves);
                    if( game.getBadmoves() == game.numOfTries() ){
                        updateAnswerLabel.textProperty().bind(Bindings.format("%s", game.getAnswer()));
                        playerAnswerLabel.textProperty().bind(Bindings.format("%s", "Your answer:"));
                        updateYourAnswerLabel.textProperty().bind(Bindings.format("%s", game.getTmpAnswer()));
                    }
                }
                else {
                    textField.clear();

                }



            }
        });
    }

    /**This method returns true if the letter entered by the player is a non-capital alphabetic letter*/
    private boolean isValidChar(char c){
        int ascii = (int) c;
        System.out.println("The int value: " + ascii);
        if( ascii >= 97 && ascii <= 122 ){
            return true;
        }
        else{
            if(ascii >= 65 && ascii <= 90){
                createAlertBox("All answers contain non-capital letters. Enter a non-capital letter.");
                return false;
            }
            else{
                createAlertBox("You have entered a non alphabetic character. Enter a non-capital alphabetical letter.");
                return false;
            }
        }
    }

    /**This method creates an alert box*/
    private void createAlertBox(String message){
        Alert alertBox = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alertBox.showAndWait();
    }

    private void setUpStatusLabelBindings() {

        System.out.println("in setUpStatusLabelBindings");
        statusLabel.textProperty().bind(Bindings.format("%s", game.gameStatusProperty()));
        enterALetterLabel.textProperty().bind(Bindings.format("%s", "Enter a letter:"));
        updateAnswerLabel.textProperty().bind(Bindings.format("%s", game.getTmpAnswer()));
        numOfMovesLeft.textProperty().bind(Bindings.format("%s", "You have " + (game.numOfTries()-game.getBadmoves()) + " moves left."));
        if( game.getUpdateBadGuesses() == null ) {
            updateBadGuesses.textProperty().bind(Bindings.format("%s", ""));
        } else {
            updateBadGuesses.textProperty().bind(Bindings.format("%s", game.getUpdateBadGuesses()));
        }

		/*	Bindings.when(
					game.currentPlayerProperty().isNotNull()
			).then(
				Bindings.format("To play: %s", game.currentPlayerProperty())
			).otherwise(
				""
			)
		);
		*/
    }
    private void drawHangman(int moves) {

        // Hanging Bar
        Line hangerSupport = new Line(20.0f, 470.0f, 300.0f, 470.0f);
        hangerSupport.setManaged(false);
        hangerSupport.setStrokeWidth(5);
        hangerSupport.setStroke(Color.WHITE);

        Line hangerPole = new Line(150.0f, 470.0f, 150.0f, 50.0f);
        hangerPole.setManaged(false);
        hangerPole.setStrokeWidth(5);
        hangerPole.setStroke(Color.WHITE);

        Line hangerTop = new Line(150.0f, 50.0f, 300.0f, 50.0f);
        hangerTop.setManaged(false);
        hangerTop.setStrokeWidth(5);
        hangerTop.setStroke(Color.WHITE);

        Line hangerRope = new Line(300.0f, 50.0f, 300.0f, 150.0f);
        hangerRope.setManaged(false);
        hangerRope.setStrokeWidth(5);
        hangerRope.setStroke(Color.BROWN);
        //End Hanging bar
        //Person
        Circle head = new Circle(300.0f, 200.0f, 0);
        head.setManaged(false);
        head.setRadius(50);
        head.setStroke(Color.BLACK);
        head.setFill(null);
        head.setStrokeWidth(5);

        Circle leftEye = new Circle(320.0f, 190.0f, 50);
        leftEye.setManaged(false);
        leftEye.setRadius(5);
        leftEye.setStroke(Color.BLUE);

        Circle rightEye = new Circle(280.0f, 190.0f, 50);
        rightEye.setManaged(false);
        rightEye.setRadius(5);
        rightEye.setStroke(Color.BLUE);

        Arc arc = new Arc(285.0f, 230.0f, 35.0f, 25.0f, 1.0f , 5.0f);
        arc.setManaged(false);
        arc.setType(ArcType.ROUND);

        Line BodyLine = new Line(300.0f, 250.0f, 300.0f, 350.0f);
        BodyLine.setManaged(false);
        BodyLine.setStrokeWidth(5);
        BodyLine.setStroke(Color.BLACK);

        Line LeftArm = new Line(300.0f, 280.0f, 250.0f, 320.0f);
        LeftArm.setManaged(false);
        LeftArm.setStrokeWidth(5);
        LeftArm.setStroke(Color.BLACK);

        Line RightArm = new Line(300.0f, 280.f, 350.0f, 320.0f);
        RightArm.setManaged(false);
        RightArm.setStrokeWidth(5);
        RightArm.setStroke(Color.BLACK);

        Line LeftLeg = new Line(300.0f, 350.0f, 250.0f, 400.0f);
        LeftLeg.setManaged(false);
        LeftLeg.setStrokeWidth(5);
        LeftLeg.setStroke(Color.BLACK);

        Line RightLeg = new Line(300.0f, 350.0f, 350.0f, 400.0f);
        RightLeg.setManaged(false);
        RightLeg.setStrokeWidth(5);
        RightLeg.setStroke(Color.BLACK);
        //End person


        if(moves == 0) {
            board.getChildren().add(hangerSupport);
            board.getChildren().add(hangerPole);
            board.getChildren().add(hangerTop);
            board.getChildren().add(hangerRope);
            /**
            board.getChildren().add(head);
            board.getChildren().add(leftEye);
            board.getChildren().add(rightEye);
            board.getChildren().add(arc);
            board.getChildren().add(BodyLine);
            board.getChildren().add(LeftArm);
            board.getChildren().add(RightArm);
            board.getChildren().add(LeftLeg);
            board.getChildren().add(RightLeg);
             **/
        }
        else if(moves==1){
            board.getChildren().add(head);
            board.getChildren().add(leftEye);
            board.getChildren().add(rightEye);
            board.getChildren().add(arc);
        }
        else if(moves==2){
            board.getChildren().add(BodyLine);
        }
        else if(moves==3){
            board.getChildren().add(LeftArm);
        }
        else if(moves==4){
            board.getChildren().add(RightArm);
        }
        else if(moves==5){
            board.getChildren().add(LeftLeg);
        }
        else if(moves==6){
            board.getChildren().add(RightLeg);
        }


    }

    @FXML
    private void newHangman() {
        game.reset();
    }

    @FXML
    private void quit() {
        board.getScene().getWindow().hide();
    }

}