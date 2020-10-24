package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static azhy.controllers.MainController.preparedTexts;
import static azhy.controllers.MainController.currentPreparedText;

public class ChangeTextController {
    public ListView<String> textsListView;
    public TextArea previewTextArea;
    public CheckBox textCheckBox;
    public CheckBox wordsCheckBox;
    public TextField searchTextField;

    @FXML
    public void initialize(){
        addCurrentLoadedTexts();

        //add listener
        textsListView.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    String name = textsListView.getSelectionModel().getSelectedItem();
                    if(name == null) return;
                    PreparedText pText = preparedTexts.get(name);
                    previewTextArea.setText(pText.getValue(getClass()));
                    if (pText.type.equals(PreparedText.TYPE_TEXT)) {
                        textCheckBox.setSelected(true);
                        wordsCheckBox.setSelected(false);
                    } else {
                        textCheckBox.setSelected(false);
                        wordsCheckBox.setSelected(true);
                    }
                });
    }

    private void addCurrentLoadedTexts(){
        String[] keys = preparedTexts.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for(String key: keys){
            PreparedText text = preparedTexts.get(key);
            textsListView.getItems().add(text.name);
            if(text == currentPreparedText) {
                int index = textsListView.getItems().size() - 1;
                textsListView.getSelectionModel().select(index);
                previewTextArea.setText(currentPreparedText.getValue(getClass()));
                if (currentPreparedText.type.equals(PreparedText.TYPE_TEXT)) {
                    textCheckBox.setSelected(true);
                    wordsCheckBox.setSelected(false);
                } else {
                    textCheckBox.setSelected(false);
                    wordsCheckBox.setSelected(true);
                }
            }
        }
    }

    public void chooseText(MouseEvent mouseEvent){
        if(textsListView.getSelectionModel().isEmpty()) return;

        String name = textsListView.getSelectionModel().getSelectedItem();
        currentPreparedText.deleteValue();
        currentPreparedText = preparedTexts.get(name);
        currentPreparedText.loadValue(getClass());

        //update userSettings.json
        FileFactory.UserSettings settings = new FileFactory.UserSettings(getClass());
        settings.set(getClass(), FileFactory.UserSettings
                .SETTINGS_LAST_CHOSEN_TEXT, name);

        textsListView.getScene().getWindow().hide();
    }

    public void addNewText(MouseEvent mouseEvent){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().
                    getResource("../layouts/addText.fxml"));
            Stage stage = new Stage();
            stage.setResizable(false);
            stage.setTitle("Add New Text");
            stage.setScene(new Scene(root, 300, 300));
            stage.show();
            // Hiding the window for adding the new text to the listview
            ((Node)(mouseEvent.getSource())).getScene().getWindow().hide();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteTextClicked(MouseEvent event){
        if(textsListView.getSelectionModel().isEmpty()) return;

        String name = textsListView.getSelectionModel().getSelectedItem();
        if(FileFactory.UserSettings.isDefaultTextName(getClass(), name)){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("You can't delete default texts!!");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Are you sure that you want to delete '" + name + "' text?");
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() != ButtonType.OK) return;

        int index = textsListView.getSelectionModel().getSelectedIndex();
        textsListView.getItems().remove(index);

        if(name.equals(currentPreparedText.name)){
            String newName = textsListView.getItems().get(0);
            currentPreparedText = preparedTexts.get(newName);
            currentPreparedText.loadValue(getClass());

            textsListView.getSelectionModel().select(0);
            previewTextArea.setText(currentPreparedText.getValue(getClass()));
            if (currentPreparedText.type.equals(PreparedText.TYPE_TEXT)) {
                textCheckBox.setSelected(true);
                wordsCheckBox.setSelected(false);
            } else {
                textCheckBox.setSelected(false);
                wordsCheckBox.setSelected(true);
            }
        }
        preparedTexts.remove(name);
        new FileFactory.Texts().deleteText(getClass(), name);
    }

    public void searched(KeyEvent e){
        textsListView.getItems().clear();
        previewTextArea.setText("");
        textCheckBox.setSelected(false);
        wordsCheckBox.setSelected(false);

        String[] keys = preparedTexts.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for(String key: keys){
            String searchText = searchTextField.getText().toLowerCase();
            if(e.getCode() != KeyCode.BACK_SPACE) searchText += e.getText();
            if(!key.toLowerCase().contains(searchText))
                continue;
            PreparedText text = preparedTexts.get(key);
            textsListView.getItems().add(text.name);
            if(text == currentPreparedText) {
                int index = textsListView.getItems().size() - 1;
                textsListView.getSelectionModel().select(index);
                previewTextArea.setText(currentPreparedText.getValue(getClass()));
                if (currentPreparedText.type.equals(PreparedText.TYPE_TEXT)) {
                    textCheckBox.setSelected(true);
                    wordsCheckBox.setSelected(false);
                } else {
                    textCheckBox.setSelected(false);
                    wordsCheckBox.setSelected(true);
                }
            }
        }
    }
}
