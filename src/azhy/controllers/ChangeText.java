package azhy.controllers;

import azhy.PreparedText;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

public class ChangeText {
    public ListView<String> textsListView;
    public TextArea previewTextArea;
    public CheckBox textCheckBox;
    public CheckBox wordsCheckBox;

    @FXML
    public void initialize(){
        //add current loaded texts
        for(PreparedText text: MainController.preparedTexts){
            textsListView.getItems().add(text.name);
        }

        //add listener
        textsListView.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> ov,
                                        String old_val, String new_val) {
                        int s = textsListView.getSelectionModel().getSelectedIndex();
                        PreparedText pText = MainController.preparedTexts.get(s);
                        previewTextArea.setText(pText.value);
                        if(pText.type.equals(PreparedText.TYPE_TEXT)){
                            textCheckBox.setSelected(true);
                            wordsCheckBox.setSelected(false);
                        }else{
                            textCheckBox.setSelected(false);
                            wordsCheckBox.setSelected(true);
                        }
                    }
                });
    }

    public void chooseText(MouseEvent mouseEvent){
        if(textsListView.getSelectionModel().isEmpty()) return;

        int s = textsListView.getSelectionModel().getSelectedIndex();
        MainController.currentPreparedText = MainController.preparedTexts.get(s);
        ((Node)textsListView).getScene().getWindow().hide();
    }

}
