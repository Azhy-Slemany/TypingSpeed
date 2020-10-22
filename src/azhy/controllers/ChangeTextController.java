package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

import static azhy.controllers.MainController.preparedTexts;
import static azhy.controllers.MainController.currentPreparedText;

public class ChangeTextController {
    public ListView<String> textsListView;
    public TextArea previewTextArea;
    public CheckBox textCheckBox;
    public CheckBox wordsCheckBox;

    @FXML
    public void initialize(){
        //add current loaded texts
        String[] keys = preparedTexts.keySet().toArray(new String[0]);
        Arrays.sort(keys);
        for(String key: keys){
            PreparedText text = preparedTexts.get(key);
            textsListView.getItems().add(text.name);
            if(text == currentPreparedText) {
                int index = textsListView.getItems().size() - 1;
                textsListView.getSelectionModel().select(index);
                previewTextArea.setText(currentPreparedText.value);
                if (currentPreparedText.type.equals(PreparedText.TYPE_TEXT)) {
                    textCheckBox.setSelected(true);
                    wordsCheckBox.setSelected(false);
                } else {
                    textCheckBox.setSelected(false);
                    wordsCheckBox.setSelected(true);
                }
            }
        }

        //add listener
        textsListView.getSelectionModel().selectedItemProperty().addListener(
                (ov, old_val, new_val) -> {
                    String name = textsListView.getSelectionModel().getSelectedItem();
                    PreparedText pText = preparedTexts.get(name);
                    previewTextArea.setText(pText.value);
                    if (pText.type.equals(PreparedText.TYPE_TEXT)) {
                        textCheckBox.setSelected(true);
                        wordsCheckBox.setSelected(false);
                    } else {
                        textCheckBox.setSelected(false);
                        wordsCheckBox.setSelected(true);
                    }
                });
    }

    public void chooseText(MouseEvent mouseEvent){
        if(textsListView.getSelectionModel().isEmpty()) return;

        String name = textsListView.getSelectionModel().getSelectedItem();
        currentPreparedText = preparedTexts.get(name);

        //update userSettings.json
        FileFactory.UserSettings settings = new FileFactory.UserSettings(getClass());
        settings.set(getClass(), FileFactory.SETTINGS_LAST_CHOSEN_TEXT, name);

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
        preparedTexts.remove(name);
        new FileFactory.Texts().update(getClass(), preparedTexts);

        int index = textsListView.getSelectionModel().getSelectedIndex();
        textsListView.getItems().remove(index);
    }
}
