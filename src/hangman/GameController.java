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


    /**The initialize method is invoked after all the @FXML annotated members have been injected.*/
    public void initialize() throws IOException {
        System.out.println("in initialize");
        drawHangman();
        addTextBoxListener();
        setUpStatusLabelBindings();
    }

    private void addTextBoxListener() {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> ov, final String oldValue, final String newValue) {
                if(newValue.length() > 0 && isValidChar(newValue.charAt(0))) {
                    System.out.print(newValue);
                    game.makeMove(newValue);
                    textField.clear();
                }
                else{
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

    private void drawHangman() {

        Line line = new Line();
        line.setStartX(25.0f);
        line.setStartY(0.0f);
        line.setEndX(25.0f);
        line.setEndY(25.0f);
        line.setStroke(Color.WHITE);



        Circle c = new Circle();
        c.setRadius(10);
        c.fillProperty().set(Color.WHITE);


        board.getChildren().add(line);
        board.getChildren().add(c);

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