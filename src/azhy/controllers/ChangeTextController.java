package azhy.controllers;

import azhy.FileFactory;
import azhy.PreparedText;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class ChangeTextController {
    public ListView<String> textsListView;
    public TextArea previewTextArea;
    public CheckBox textCheckBox;
    public CheckBox wordsCheckBox;

    @FXML
    public void initialize(){
        //add current loaded texts
        for(PreparedText text: MainController.preparedTexts){
            textsListView.getItems().add(text.name);
            if(text == MainController.currentPreparedText)
                textsListView.getSelectionModel().select(
                        textsListView.getItems().size() - 1);
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

    public void addNewText(MouseEvent mouseEvent){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("../layouts/addText.fxml"));
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

        int index = textsListView.getSelectionModel().getSelectedIndex();

        String jsonText = FileFactory.readResourceFile(
                getClass(), "../Data/texts.json");
        if(jsonText != null) {
            JSONArray texts = new JSONArray(jsonText);
            texts.remove(index);
            FileFactory.writeResourceFile(
                    getClass(), "../Data/texts.json", texts.toString());
            MainController.preparedTexts.remove(index);
        }

        textsListView.getItems().remove(index);
    }
}
