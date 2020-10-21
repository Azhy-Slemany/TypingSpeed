package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.json.JSONArray;
import org.json.JSONObject;

public class AddTextController {
    public TextField nameTextField;
    public RadioButton textRadioButton, wordsRadioButton;
    public TextArea textArea;

    @FXML
    public void initialize(){

        textRadioButton.setOnMouseClicked((e) -> {
            wordsRadioButton.setSelected(!textRadioButton.isSelected());
        });

        wordsRadioButton.setOnMouseClicked((e) -> {
            textRadioButton.setSelected(!wordsRadioButton.isSelected());
        });
    }

    public void saveButtonClicked(MouseEvent event){
        //prepare texts
        String jsonText = FileFactory.readResourceFile(
                getClass(), "../Data/texts.json");
        if(jsonText != null) {
            JSONArray texts = new JSONArray(jsonText);

            JSONObject text = new JSONObject();
            String name = nameTextField.getText();
            String type = textRadioButton.isSelected() ? "text" : "words";
            String value = textArea.getText();
            text.put("name", name);
            text.put("type", type);
            text.put("value", value);

            texts.put(text);
            FileFactory.writeResourceFile(getClass(),
                    "../Data/texts.json", texts.toString());
            PreparedText pText = new PreparedText(name, type, value);
            MainController.preparedTexts.add(pText);
            MainController.currentPreparedText = pText;
        }
        ((Node)event.getSource()).getScene().getWindow().hide();
    }

}
