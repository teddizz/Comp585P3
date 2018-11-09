package hangman;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Game {

    private String answer; // actual answer
    private String tmpAnswer; // user's answer: a _ _ l _
    private String updateBadGuesses;
    private String[] letterAndPosArray; // contains the answer in an array form
    private int numBadMoves;
    private int index = 0;
    private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
    private ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<>();

    public enum GameStatus {
        GAME_OVER {
            @Override
            public String toString() {
                return "Game over!";
            }
        },
        BAD_GUESS {
            @Override
            public String toString() { return "Bad guess..."; }
        },
        GOOD_GUESS {
            @Override
            public String toString() {
                return "Good guess!";
            }
        },
        WON {
            @Override
            public String toString() {
                return "You won!";
            }
        },
        OPEN {
            @Override
            public String toString() {
                return "Game on, lets GO!";
            }
        }
    }

    public Game() {
        /**A JavaFX property defines a component's height, width, color, etc.
         * The class ReadOnlyObjectWrapper<T> extends the Class SimpleObjectProperty<<T>.
         * Ie., ReadOnlyObjectWrapper<T> is making the 'enum GameStatus' Class into a property. This property defines
         * the state of the game; which can have a starter state of "Game On, Lets GO!", another state of "Good Guess"
         * or a state of "Game over" etc.
         *
         * As an argument it takes a java.lang.Object bean which i think is the object to whom these properties belong to.
         * GameStatus properties should belong to the game itself, which is why we pass the argument 'this'.
         * The second argument is the name of the property. Which is simply "gameStatus".
         * The third argument is the initial value of the property, which is "Game on, lets go!"
         *
         * All JavaFX properties are Observable. This means that when a property’s value becomes invalid or changes,
         * the property notifies its registered InvalidationListeners or ChangeListeners. Read only properties cannot
         * be modified but read-only properties can change. Ie when we change from "Game on lets go" to "good guess" we
         * are not modifying the property "Game on lets go" the GameStatus is simply changing to "Good Guess".
         * For this reason, we use a ChangeListener.
         *
         * -Willy
         * */
        gameStatus = new ReadOnlyObjectWrapper<>(this, "gameStatus", GameStatus.OPEN);
        gameStatus.addListener(new ChangeListener<GameStatus>() {
            @Override
            public void changed(ObservableValue<? extends GameStatus> observable,
                                GameStatus oldValue, GameStatus newValue) {
                if (gameStatus.get() != GameStatus.OPEN) {
                    log("in Game: in changed");
//                    System.out.println("This is the old value: " + oldValue + " The new value: " + newValue);
                    //currentPlayer.set(null);
                }
            }

        });
        updateBadGuesses = "";
        setRandomWord();
        prepTmpAnswer();
        prepLetterAndPosArray();
        numBadMoves = 0;

        gameState.setValue(false); // initial state
        createGameStatusBinding();
    }

    private void createGameStatusBinding() {
        List<Observable> allObservableThings = new ArrayList<>();
        ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
            {
                super.bind(gameState);
            }
            @Override
            public GameStatus computeValue() {
                log("in computeValue");
                GameStatus check = checkForWinner(index);

                /**Returns true if the game is still in the state of guessing.
                 * if there player has won or lost, then return the game state of WON or GAMEOVER*/
                if(check != null ) {
                    return check;
                }

                /**tmpAnswer.trim() will have a length of 0 as long as the user has NOT guessed a single letter correctly.
                 * Its length will not equal 0 the moment the user guesses a letter correctly.
                 *
                 * the makeMove() method can set the 'index' value to 0 when the first letter of 'answer' is guessed correctly;however,
                 * tmpAnswer will no longer be 0. If the user guesses incorrectly, 'index' is equal to -1.
                 * Therefore, we can begin to track the users numBadMoves from their first guess; whether it is a Good or Bad guess.
                 *
                 * */
                if(tmpAnswer.trim().length() == 0 && index == 0){
                    log("new game");
                    return GameStatus.OPEN;
                }
                else if (index != -1){
                    log("good guess");
                    return GameStatus.GOOD_GUESS;
                }
                else {
                    numBadMoves++;
                    System.out.println("numBadMoves: " + numBadMoves);
                    log("bad guess");

                    
                    /**Checks to see if the user has lost. 
                     * Returns true if numBadMoves equals 5. Other wise, change the game state to 
                     * Bad_GUESS*/

                    check = checkForWinner(index);
                    if(check != null){
                        return check;
                    }
                    
                    
                    return GameStatus.BAD_GUESS;
                    //printHangman();
                }
            }
        };
        gameStatus.bind(gameStatusBinding);
    }

    public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
        return gameStatus.getReadOnlyProperty();
    }
    public GameStatus getGameStatus() {
        return gameStatus.get();
    }


    /**This method obtains a random word as the answer to the hangman game*/
    private void setRandomWord() {
        Random rand = new Random();
        int n = 0;
        try {
            File f = new File(this.getClass().getResource("/Words.txt").toURI());
            Scanner sc = new Scanner(f);
            while(sc.hasNext()) {
                n++;
                String line = sc.nextLine();
                if(rand.nextInt(n) == 0)
                    answer = line;
            }
        } catch(Exception e) {
            log(e.toString());
            answer = "apple";
        }
        log("Answer is: " + answer);

        /* OLD
        //int idx = (int) (Math.random() * words.length);
        answer = "apple";//words[idx].trim(); // remove new line character
        */
    }
    public int getBadmoves(){
        return numBadMoves;
    }

    public String getAnswer() {
         return answer;
    }

    public String getTmpAnswer() { return tmpAnswer; }

    public String getUpdateBadGuesses() {
        return updateBadGuesses;
    }

    /**This method creates a string with the number of spaces equal to the length of the answer.
     * ie if the answer is 'apple' then tmpAnswer will be a string of five spaces _ _ _ _ _
     **/
    private void prepTmpAnswer() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < answer.length(); i++) {
            sb.append("_");
        }
        tmpAnswer = sb.toString();
    }


    /**This method places each letter of the 'answer' into individual indexes of the letterAndPosArray.
     * The length of the array is equal to the length of the answer. */
    private void prepLetterAndPosArray() {
        letterAndPosArray = new String[answer.length()];
        for(int i = 0; i < answer.length(); i++) {
            letterAndPosArray[i] = answer.substring(i,i+1);
        }
    }

    /**This method returns -1 if the letter entered by the user is not a letter contained in the answer.
     * Other wise the method returns the index in which the letter entered by the player is held in the
     * letterAndPosArray . */
    private ArrayList<Integer> getValidIndex(String input) {
        ArrayList<Integer> index = new ArrayList<Integer>();
        for(int i = 0; i < letterAndPosArray.length; i++) {
            if(letterAndPosArray[i].equals(input)) {
                index.add(i);
                this.index = i;
                letterAndPosArray[i] = "";
            }
        }
        return index;
    }

    /**This method updates tmpAnswer if the user has guessed a correct letter. */
    private int update(String input) {
        this.index = -1;
        ArrayList<Integer> index = getValidIndex(input);
        if(index.size() != 0 ) {
            /**We are here if the player made a good guess*/
            for( int i = 0; i < index.size(); i++ ){
                StringBuilder sb = new StringBuilder(tmpAnswer);
                sb.setCharAt(index.get(i), input.charAt(0));
                tmpAnswer = sb.toString();
            }
        }
        return this.index;
    }

    private static void drawHangmanFrame() {}

    /**This method updates the index value to -1 if the guess by the player is incorrect
     * or a number from 0 to the length of the answer minus 1. ie a number between 0 to answer.length() -1*/
    public int makeMove(String letter) {

        log("\nin makeMove: " + letter);
        index = update(letter);
        // this will toggle the state of the game
        gameState.setValue(!gameState.getValue());
        return numBadMoves;
    }

    public void reset() {}

    /** The number of tries remaining*/
    public int numOfTries() {
        return 6; // TODO, fix me
    }

    public static void log(String s) {
        System.out.println(s);
    }

    private GameStatus checkForWinner(int status) {
        log("in checkForWinner");
        if(tmpAnswer.equals(answer)) {
            log("won");
            return GameStatus.WON;
        }
        else if(numBadMoves == numOfTries()) {
            log("game over");
            return GameStatus.GAME_OVER;
        }
        else {
            return null;
        }
    }
}
