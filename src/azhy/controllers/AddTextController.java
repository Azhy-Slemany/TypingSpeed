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
        //check if some text already exists with that name
        for (String key : MainController.preparedTexts.keySet())
            if (MainController.preparedTexts.get(key).
                    name.equals(nameTextField.getText())) {
                //show a message box
                return;
            }

        //prepare texts
        String name = nameTextField.getText();
        String type = textRadioButton.isSelected() ? "text" : "words";
        String value = textArea.getText();
        PreparedText pText = new PreparedText(name, type, value);
        MainController.preparedTexts.put(name, pText);
        MainController.currentPreparedText = pText;
        new Texts().update(getClass(), MainController.preparedTexts);

        ((Node) event.getSource()).getScene().getWindow().hide();
    }

}
