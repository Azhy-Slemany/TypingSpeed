package azhy.controllers;

import azhy.FileFactory;
import azhy.FileFactory.Texts;
import azhy.FileFactory.UserSettings;
import azhy.PreparedText;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.*;

import static azhy.FileFactory.*;

public class MainController {
    public TextField textField;
    public TextArea textArea;
    public Label countingLabel;
    public Label wpmLabel;
    public Label accuracyLabel;
    public Label errorsLabel;
    public Label timeLabel;
    public Button stateButton;
    public ComboBox<String> timeComboBox;
    public ImageView muteImageView;

    private static int TIME = (int)(1.0 * 60);

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
    static Map<String, PreparedText> preparedTexts;
    static PreparedText currentPreparedText;
    Map<String, MediaPlayer> sounds;
    boolean isMuted = false;

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
        //load fonts for those users who their username contains whitespace
        //Font robotoFont = tryLoadFont("Roboto-Regular.ttf", 22);
        Font ubuntuMono = tryLoadFont("UbuntuMono-Regular.ttf", 22);
        textArea.setFont(ubuntuMono);
        textField.setFont(ubuntuMono);

        //initialize variables
        timer = new Timer();
        startingTimer = new Timer();
        preparedTexts = new HashMap<>();
        setTimeLabel(TIME);
        sounds = FileFactory.Sounds.loadSoundFiles(getClass());

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

                //play music
                playSound(sounds.get(FileFactory.Sounds.TYPING_ERROR_SOUND));
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
        Texts texts = new Texts();
        preparedTexts = texts.getPreparedTexts(getClass());

        //add times
        timeComboBox.getItems().addAll(
                "30 secs", "1 min", "2 min", "5 min", "10 min");

        //read user settings
        UserSettings settings = new UserSettings(getClass());
        String textName = (String)settings.get(UserSettings.SETTINGS_LAST_CHOSEN_TEXT);
        currentPreparedText = preparedTexts.get(textName);
        currentPreparedText.loadValue(getClass());

        isMuted = !(boolean)settings.get(UserSettings.SETTINGS_SOUND);
        if(isMuted) muteImageView.setImage(Images.getImage(getClass(), Images.IMAGE_MUTE));

        TIME = (int)settings.get(UserSettings.SETTINGS_TIME);
        setTimeLabel(TIME);
    }

    private TimerTask timerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(currentTimeLapse >= TIME){
                    new Thread(() -> Platform.runLater(() ->
                            stopTyping()
                    )).start();
                    return;
                }

                timer.schedule(timerTask(), 1000); currentTimeLapse++;
                new Thread(() -> Platform.runLater(() ->
                        setTimeLabel(TIME - currentTimeLapse)
                )).start();
            }
        };
    }

    private TimerTask startingTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                if(startingTime == 1){
                    new Thread(() -> Platform.runLater(() -> {
                        textArea.setText("");
                        startTyping();
                    })).start();
                    return;
                }else startingTime--;
                new Thread(() -> Platform.runLater(() ->
                        countingLabel.setText(
                                String.valueOf(startingTime)))
                ).start();
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
        textField.setText(currentPreparedText.getText(getClass()));
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
        currentPreparedText.regenerateText(getClass());// time goes to under zero please fix this
    }

    public void stateButtonPressed(ActionEvent actionEvent){
        if(currentState.equals(States.Rest)){
            beforeStartTyping();
            return;
        }
        stopTyping();
    }

    public void muteImageViewClicked(MouseEvent mouseEvent){
        String name = isMuted? Images.IMAGE_SOUND: Images.IMAGE_MUTE;
        muteImageView.setImage(Images.getImage(getClass(), name));
        isMuted = !isMuted;
        new UserSettings(getClass()).set(getClass(),
                UserSettings.SETTINGS_SOUND, !isMuted);
    }

    public void timeComboBoxChose(ActionEvent actionEvent){
        int index = timeComboBox.getSelectionModel().getSelectedIndex();
        if(index == 0) TIME = 30;
        else if(index == 1) TIME = (int)(1.0 * 60);
        else if(index == 2) TIME = (int)(2.0 * 60);
        else if(index == 3) TIME = (int)(5.0 * 60);
        else if(index == 4) TIME = (int)(10.0 * 60);
        setTimeLabel(TIME);
        new UserSettings(getClass()).set(getClass(),
                UserSettings.SETTINGS_TIME, TIME);
    }

    public void typed(KeyEvent keyEvent){
        /*if(!currentState.equals(States.Writing) && !isStarting) {
            stateButton.setDisable(true);
            startingTimer.schedule(startingTimerTask(), 0);
            return;
        }*/
        if(currentState != States.Writing) return;

        if(currentTimeLapse >= TIME){
            if(textArea.isEditable())
                textArea.setEditable(false);
            return;
        }

        playSound(sounds.get(FileFactory.Sounds.TYPING_SOUND));

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
        String beforeText = currentPreparedText.getText(getClass())
                .substring(0, currentTextIndex - 1);
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
        String text = currentPreparedText.getText(getClass());// I think we should count missed words as accuracy reducers also
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

    private void playSound(MediaPlayer player){
        if(isMuted) return;
        player.stop();
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
