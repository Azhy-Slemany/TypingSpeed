package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import static azhy.FileFactory.*;

public class AddTextController {
    public TextField nameTextField;
    public RadioButton textRadioButton, wordsRadioButton;
    public TextArea textArea;

    @FXML
    public void initialize() {

        textRadioButton.setOnMouseClicked((e) -> {
            wordsRadioButton.setSelected(!textRadioButton.isSelected());
        });

        wordsRadioButton.setOnMouseClicked((e) -> {
            textRadioButton.setSelected(!wordsRadioButton.isSelected());
        });
    }

    public void saveButtonClicked(MouseEvent event) {
        if(nameTextField.getText().equals("") || textArea.getText().equals(""))
            return;

        //check if some text already exists with that name
        for (String key : MainController.preparedTexts.keySet())
            if (MainController.preparedTexts.get(key).
                    name.equals(nameTextField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("An text already exist with that " +
                        "name, please change the name.");
                alert.show();
                return;
            }

        //prepare texts
        String name = nameTextField.getText();
        String type = textRadioButton.isSelected() ? "text" : "words";
        String value = textArea.getText();
        PreparedText pText = new PreparedText(name, type, value);
        MainController.preparedTexts.put(name, pText);
        MainController.currentPreparedText = pText;

        //add to texts file
        new Texts().addText(getClass(), pText);

        //change last chosen text
        FileFactory.UserSettings settings = new FileFactory.UserSettings(getClass());
        settings.set(getClass(), FileFactory.UserSettings
                .SETTINGS_LAST_CHOSEN_TEXT, name);

        //hide the window
        ((Node) event.getSource()).getScene().getWindow().hide();
    }

}
