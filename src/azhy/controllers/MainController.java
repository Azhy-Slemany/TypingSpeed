package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class MainController {
    public TextField textField;
    public TextArea textArea;
    public Label countingLabel;
    public Label wpmLabel;
    public Label accuracyLabel;
    public Label errorsLabel;
    public Label timeLabel;
    public Button stateButton;

    private static final int TIME = (int)(1.0 * 60); // hey time goes to under zero while you space several times in the end of the time

    Timer timer, startingTimer;
    long currentTimeLapse = 0;
    int startingTime = 0;
    int currentWpm;
    ArrayList<Integer> errors;
    int deletedLetters;
    int correctTextLength;
    int currentTextIndex;
    enum States{Rest, Writing, Starting, Finished}
    States currentState = States.Rest;
    static ArrayList<PreparedText> preparedTexts;
    static PreparedText currentPreparedText;

    //set labels
    private void setWpmLabel(Object value){
        wpmLabel.setText("Speed : " + value + " wpm");
    }
    private void setAccuracyLabel(Object value){
        accuracyLabel.setText("Accuracy : " + value + "%");
    }
    private void setErrorsLabel(){
        errorsLabel.setText("Errors : " + errors.size());
    }
    private void setTimeLabel(long value){
        String result = value > 60? value / 60 + ":" + value % 60: value + "";
        timeLabel.setText("Time : " + result);
    }


    @FXML
    public void initialize() {
        //load fonts for windows
        Font robotoFont = tryLoadFont("Roboto-Regular.ttf", 22);
        Font ubuntuMono = tryLoadFont("UbuntuMono-Regular.ttf", 22);
        textArea.setFont(ubuntuMono);
        textField.setFont(ubuntuMono);

        //initialize variables
        timer = new Timer();
        startingTimer = new Timer();
        preparedTexts = new ArrayList<>();
        setTimeLabel(TIME);

        //make tab 4 spaces and add other key pressed activities
        textArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if(!currentState.equals(States.Writing)) return;

            KeyCode key = e.getCode();
            if (key == KeyCode.TAB) {
                textArea.insertText(textArea.getCaretPosition(), " ".repeat(4));
                e.consume();
            }else if(key == KeyCode.BACK_SPACE){
                //listen to backspace for reducing errors when reach them
                String txt = textArea.getText();
                if(txt.length() == 0) return;
                if(txt.toCharArray()[txt.length() - 1] == ' ') {
                    ArrayList<Object> dWord = getDeletedWord();
                    if(dWord == null) return;
                    String beforeWord = (String)dWord.get(0);
                    textField.setText(beforeWord + ' ' + textField.getText());
                    currentTextIndex -= (beforeWord.length() + 1);
                    if(!errors.isEmpty() && errors.get(errors.size() - 1) == (int) dWord.get(1)){
                        errors.remove(errors.size() - 1);
                        errorsLabel.setText("Errors : " + errors.size());
                    }
                }
            }else if(key == KeyCode.SPACE){
                String txt = textArea.getText();
                if(txt.length() > 0 && txt.toCharArray()[txt.length() - 1] == ' '){
                    textArea.setText(txt.substring(0, txt.length() - 1));
                    textArea.positionCaret(txt.length() - 1);
                    e.consume();
                }
            }
        });

        //prepare texts
        String jsonText = FileFactory.readResourceFile(
                getClass(), "../Data/texts.json");
        if(jsonText != null) {
            JSONArray texts = new JSONArray(jsonText);
            for(int i=0; i<texts.length(); i++){
                JSONObject text = (JSONObject) texts.get(i);
                String name = text.getString("name");
                String type = text.getString("type");
                String value = text.getString("value");
                preparedTexts.add(new PreparedText(name, type, value));
            }
            currentPreparedText = preparedTexts.get(0);
        }

    }

    private TimerTask timerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(currentTimeLapse >= TIME){
                    new Thread(() -> {
                        Platform.runLater(() ->
                                stopTyping()
                        );
                    }).start();
                    return;
                }

                timer.schedule(timerTask(), 1000); currentTimeLapse++;
                new Thread(() -> {
                    Platform.runLater(() ->
                            setTimeLabel(TIME - currentTimeLapse)
                    );
                }).start();
            }
        };
    }

    private TimerTask startingTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(startingTime == 1){
                    new Thread(() -> {
                        Platform.runLater(() -> {
                            textArea.setText("");
                            startTyping();
                        });
                    }).start();
                    return;
                }else startingTime--;
                new Thread(() -> {
                    Platform.runLater(() -> countingLabel.setText(String.valueOf(startingTime)));
                }).start();
                startingTimer.schedule(startingTimerTask(), 1000);
            }
        };
    }

    private void beforeStartTyping(){
        stateButton.setDisable(true);
        countingLabel.setVisible(true);
        textArea.setText("");
        textArea.requestFocus();
        animateCountingLabel();
        currentState = States.Starting;
        textField.setText(currentPreparedText.getText());
        startingTime = 4;
        startingTimer.schedule(startingTimerTask(), 0);
    }

    private void startTyping(){
        currentState = States.Writing;
        errors = new ArrayList<>();
        setErrorsLabel();
        deletedLetters = 0;
        timer.schedule(timerTask(), 1000);
        setAccuracyLabel('-');
        textArea.setEditable(true);
        textArea.requestFocus();
        stateButton.setText("Stop");
        stateButton.setDisable(false);
        countingLabel.setVisible(false);
        correctTextLength = currentTextIndex = 0;
    }

    private void stopTyping(){
        timer.cancel();
        timer = new Timer();
        setAccuracyLabel(getAccuracy());
        currentState = States.Finished;
        animateCountingLabelFinished();
        stateButton.setText("Start Typing");
        currentTimeLapse = 0;
        currentWpm = 0;
        textArea.setEditable(false);
        currentPreparedText.regenerateText();// time goes to under zero please fix this
    }

    public void stateButtonPressed(ActionEvent actionEvent){
        if(currentState.equals(States.Rest)){
            beforeStartTyping();
            return;
        }
        stopTyping();
    }

    public void typed(KeyEvent keyEvent){
        /*if(!currentState.equals(States.Writing) && !isStarting) {
            stateButton.setDisable(true);
            startingTimer.schedule(startingTimerTask(), 0);
            return;
        }*/
        if(currentState != States.Writing) return;

        if(currentTimeLapse >= TIME){
            //stopTyping();
            return;
        }

        KeyCode code = keyEvent.getCode();

        String textAreaText = textArea.getText();
        String[] words = textAreaText.split("\\s+");

        String textFieldText = textField.getText();
        String[] tfWords = textFieldText.split("\\s+");

        if(code == KeyCode.SPACE){
            String firstWord = tfWords[0];
            currentTextIndex += firstWord.length() + 1;

            if(words[words.length - 1].equals(firstWord)){
                correctTextLength += firstWord.length();
            }else {
                ArrayList<Object> deletedWord = getDeletedWord();
                if(deletedWord == null) return;
                errors.add((int) deletedWord.get(1));
            }
            setErrorsLabel();
            setWpmLabel(findWpm());

            if(textFieldText.length() > firstWord.length())
                textField.setText(textFieldText.substring(firstWord.length() + 1));
            else {
                textField.setText("");
                stopTyping();
            }
        }else if(code == KeyCode.BACK_SPACE){
            deletedLetters++;
        }else if(code == KeyCode.UP || code == KeyCode.DOWN ||
                code == KeyCode.RIGHT || code == KeyCode.LEFT){
            textArea.positionCaret(textArea.getText().length());
        }else if(tfWords.length == 1 && (words[words.length - 1] + keyEvent.getText())
                .equals(tfWords[tfWords.length - 1])){
            textArea.appendText(keyEvent.getText());
            stopTyping();
        }

    }

    public void clicked(MouseEvent mouseEvent){
        if(currentState == States.Rest && textArea.getText().isEmpty()){
            beforeStartTyping();
        }

        textArea.positionCaret(textArea.getText().length());
    }

    public void changeText(MouseEvent mouseEvent){
        if(currentState != States.Rest) stopTyping();

        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("../layouts/changeText.fxml"));
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("Change Text");
            stage.setScene(new Scene(root, 700, 300));
            stage.show();
            // Hide this current window (if this is what you want)
            // ((Node)(event.getSource())).getScene().getWindow().hide();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Object> getDeletedWord(){
        if(currentTextIndex == 0) return null;
        String beforeText = currentPreparedText.getText().substring(0, currentTextIndex - 1);
        String[] beforeWords = beforeText.split("\\s+");
        ArrayList<Object> result = new ArrayList<Object>();
        result.add(beforeWords[beforeWords.length - 1]);
        result.add(beforeWords.length - 1);
        return result;
    }

    private int findWpm(){
        //Method - 1
        //int wordsCount = textArea.getText().split("\\s+").length;
        //currentWpm = (int)(wordsCount * 60.0 / currentTimeLapse);

        //Method - 2
        double spaces = correctTextLength / 5.0;
        double wordsCount = (correctTextLength + spaces) / 5.0;
        currentWpm = (int)(wordsCount * 60.0 / currentTimeLapse);

        return currentWpm;
    }

    private String getAccuracy(){
        String text = currentPreparedText.getText();// I think we should count missed words as accuracy reducers also
        double cLetters = text.replace(" ", "").length();
        double result = Math.round((1 - (deletedLetters / (cLetters))) * 1000) / 10.0;
        return result == Math.round(result) ? ((int) result) + "" : result + "";
    }

    private void animateCountingLabel(){
        Timeline flash = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(countingLabel.opacityProperty(),0.6)),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(countingLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(countingLabel.opacityProperty(), 0.6))
        );
        flash.setAutoReverse(true);
        flash.setCycleCount(6);
        flash.play();
    }

    private void animateCountingLabelFinished(){
        countingLabel.setVisible(true);
        countingLabel.setText("Finished!");
        stateButton.setDisable(true);

        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(countingLabel.opacityProperty(), 0.5)),
                new KeyFrame(Duration.millis(1300),
                        new KeyValue(countingLabel.opacityProperty(), 0.1))
        );
        flash.play();
        flash.setOnFinished((e) -> {
            countingLabel.setVisible(false);
            stateButton.setDisable(false);
            currentState = States.Rest;
        });
    }

    private void playMusic(String filePath){
        Media sound = new Media(getClass().getResource(filePath).toExternalForm());
        MediaPlayer player = new MediaPlayer(sound);
        player.play();
    }

    private Font tryLoadFont(String fontName, double size){
        try {
            String fixedPath = getClass().
                    getResource("../Fonts/" + fontName).toURI().
                    toString().replace("%20", " ");
            Font font = Font.loadFont(fixedPath, size);
            if(font == null){
                System.out.println("Error: Font file \"" + fontName +
                        "\" couldn't be found.");
                return null;
            }
            return font;
        } catch (URISyntaxException e) {
            System.out.println("Error: Font file \"" + fontName +
                    "\" couldn't be found.");
            return null;
        }
    }
}
