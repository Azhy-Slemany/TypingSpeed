package azhy;

import azhy.controllers.MainController;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class FileFactory {

    public static final String RESOURCE_FILE_TEXTS = "../Data/texts.json";
    public static final String RESOURCE_FILE_USER_SETTINGS = "../Data/userSettings.json";
    public static final String SETTINGS_LAST_CHOSEN_TEXT = "lastChosenText";

    public static String readResourceFile(Class tClass, String filePath){
        try{
            InputStream s = tClass.getResourceAsStream(filePath);
            return new Scanner(s, "UTF-8").useDelimiter("\\A").next();
        }catch (Exception e){
            System.out.println("Error happened reading file: " + filePath);
            return null;
        }
    }
    public static void writeResourceFile(Class tClass, String filePath, String text){
        try{
            String file = tClass.getResource(filePath).toURI()
                    .toString().replace("%20", " ").
                            replace("file:/", "");
            Path path = Paths.get(file);
            Writer out = Files.newBufferedWriter(path, Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            out.write(text);
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println("Error happened writing to file: " + filePath);
        }
    }

    public static class UserSettings{
        private JSONObject json;

        public UserSettings(Class class_){
            String text = readResourceFile(class_, RESOURCE_FILE_USER_SETTINGS);
            if(text == null){
                System.out.println("Error reading userSettings data file!!");
                return;
            }
            json = new JSONObject(text);
        }
        public Object get(String key){
            return json.get(key);
        }
        public void set(Class class_, String key, Object value){
            json.put(key, value);
            writeResourceFile(class_, RESOURCE_FILE_USER_SETTINGS, json.toString());
        }
    }

    public static class Texts{
        public Map<String, PreparedText> get(Class class_){
            Map<String, PreparedText> result = new HashMap<>();
            String jsonText = readResourceFile(class_, RESOURCE_FILE_TEXTS);
            if(jsonText != null) {
                JSONArray texts = new JSONArray(jsonText);
                for(int i=0; i<texts.length(); i++){
                    JSONObject text = (JSONObject) texts.get(i);
                    String name = text.getString("name");
                    String type = text.getString("type");
                    String value = text.getString("value");
                    result.put(name, new PreparedText(name, type, value));
                }
            }
            return result;
        }
        public void update(Class class_, Map<String, PreparedText> value){
            JSONArray texts = new JSONArray();
            for(String key: value.keySet()) {
                PreparedText current = value.get(key);
                JSONObject obj = new JSONObject();
                obj.put("name", current.name);
                obj.put("type", current.type);
                obj.put("value", current.value);
                texts.put(obj);
            }
            writeResourceFile(class_,
                    "../Data/texts.json", texts.toString());
        }
    }
}
