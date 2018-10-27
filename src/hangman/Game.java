package hangman;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private String answer;
    private String tmpAnswer;
    private String[] letterAndPosArray;
    private String[] words;
    private int moves;
    private int index;
    private final ReadOnlyObjectWrapper<GameStatus> gameStatus;
    private ObjectProperty<Boolean> gameState = new ReadOnlyObjectWrapper<Boolean>();

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
                return "Game on, let's go!";
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
         *
         * */
        gameStatus = new ReadOnlyObjectWrapper<GameStatus>(this, "gameStatus", GameStatus.OPEN);
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
        setRandomWord();
        prepTmpAnswer();
        prepLetterAndPosArray();
        moves = 0;

        gameState.setValue(false); // initial state
        createGameStatusBinding();
    }

    private void createGameStatusBinding() {
        /**An Observable is */
        List<Observable> allObservableThings = new ArrayList<>();
        ObjectBinding<GameStatus> gameStatusBinding = new ObjectBinding<GameStatus>() {
            {
                super.bind(gameState);
            }
            @Override
            public GameStatus computeValue() {
                log("in computeValue");
                GameStatus check = checkForWinner(index);
                if(check != null ) {
                    return check;
                }

                if(tmpAnswer.trim().length() == 0){
                    log("new game");
                    return GameStatus.OPEN;
                }
                else if (index != -1){
                    log("good guess");
                    return GameStatus.GOOD_GUESS;
                }
                else {
                    moves++;
                    log("bad guess");
                    return GameStatus.BAD_GUESS;
                    //printHangman();
                }
            }
        };
        System.out.println("This first");
        gameStatus.bind(gameStatusBinding);
        System.out.println("This second");
    }

    public ReadOnlyObjectProperty<GameStatus> gameStatusProperty() {
        return gameStatus.getReadOnlyProperty();
    }
    public GameStatus getGameStatus() {
        return gameStatus.get();
    }

    private void setRandomWord() {
        //int idx = (int) (Math.random() * words.length);
        answer = "apple";//words[idx].trim(); // remove new line character
    }

    private void prepTmpAnswer() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < answer.length(); i++) {
            sb.append(" ");
        }
        tmpAnswer = sb.toString();
    }

    private void prepLetterAndPosArray() {
        letterAndPosArray = new String[answer.length()];
        for(int i = 0; i < answer.length(); i++) {
            letterAndPosArray[i] = answer.substring(i,i+1);
        }
    }

    private int getValidIndex(String input) {
        int index = -1;
        for(int i = 0; i < letterAndPosArray.length; i++) {
            if(letterAndPosArray[i].equals(input)) {
                index = i;
                letterAndPosArray[i] = "";
                break;
            }
        }
        return index;
    }

    private int update(String input) {
        int index = getValidIndex(input);
        if(index != -1) {
            StringBuilder sb = new StringBuilder(tmpAnswer);
            sb.setCharAt(index, input.charAt(0));
            tmpAnswer = sb.toString();
        }
        return index;
    }

    private static void drawHangmanFrame() {}

    public void makeMove(String letter) {
        log("\nin makeMove: " + letter);
        index = update(letter);
        // this will toggle the state of the game
        gameState.setValue(!gameState.getValue());
    }

    public void reset() {}

    private int numOfTries() {
        return 5; // TODO, fix me
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
        else if(moves == numOfTries()) {
            log("game over");
            return GameStatus.GAME_OVER;
        }
        else {
            return null;
        }
    }
}
