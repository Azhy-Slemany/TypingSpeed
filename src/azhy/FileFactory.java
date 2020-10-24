package azhy;

import azhy.controllers.MainController;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
        public static final String SETTINGS_LAST_CHOSEN_TEXT = "lastChosenText";
        public static final String SETTINGS_DEFAULT_TEXTS = "defaultTexts";
        public static final String SETTINGS_SOUND = "sound";
        public static final String SETTINGS_TIME = "time";

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
        public static boolean isDefaultTextName(Class class_, String name){
            String text = readResourceFile(class_, RESOURCE_FILE_USER_SETTINGS);
            if(text == null){
                System.out.println("Error reading userSettings data file!!");
                return false;
            }
            JSONObject json = new JSONObject(text);
            String texts = (String) json.get(SETTINGS_DEFAULT_TEXTS);
            String[] textNames = texts.split("\\|");
            for(String textName: textNames)
                if(textName.equals(name)) return true;
            return false;
        }
    }

    public static class Texts{
        public Map<String, PreparedText> getPreparedTexts(Class class_){
            Map<String, PreparedText> result = new HashMap<>();
            String jsonText = readResourceFile(class_, RESOURCE_FILE_TEXTS);
            if(jsonText != null) {
                JSONObject texts = new JSONObject(jsonText);
                for(String name: texts.keySet()){
                    JSONObject text = (JSONObject) texts.get(name);
                    String type = text.getString("type");
                    result.put(name, new PreparedText(name, type));
                }
            }
            return result;
        }
        public String getValue(Class class_, String name){
            String jsonText = readResourceFile(class_, RESOURCE_FILE_TEXTS);
            if(jsonText == null) return null;
            JSONObject obj = new JSONObject(jsonText);
            return (String)((JSONObject)obj.get(name)).get("value");
        }
        public void addText(Class class_, PreparedText text){
            String jsonText = readResourceFile(class_, RESOURCE_FILE_TEXTS);
            if(jsonText != null) {
                JSONObject texts = new JSONObject(jsonText);
                texts.put(text.name, text.toJSONObject(class_));
                writeResourceFile(class_,
                        "../Data/texts.json", texts.toString());
            }
        }
        public void deleteText(Class class_, String name){
            String jsonText = readResourceFile(class_, RESOURCE_FILE_TEXTS);
            if(jsonText != null) {
                JSONObject texts = new JSONObject(jsonText);
                texts.remove(name);
                writeResourceFile(class_,
                        "../Data/texts.json", texts.toString());
            }
        }
    }

    public static class Sounds{
        public static final String TYPING_SOUND = "typingSound.mp3";
        public static final String TYPING_ERROR_SOUND = "typingErrorSound.wav";

        public static Map<String, MediaPlayer> loadSoundFiles(Class class_){
            Map<String, MediaPlayer> result = new HashMap<>();

            result.put(TYPING_SOUND, getMediaPlayer(class_, TYPING_SOUND));
            result.put(TYPING_ERROR_SOUND, getMediaPlayer(class_, TYPING_ERROR_SOUND));

            return result;
        }

        private static MediaPlayer getMediaPlayer(Class class_, String name){
            String path = "../SoundFiles/" + name;
            Media media = new Media(class_.getResource(path).toExternalForm());
            return new MediaPlayer(media);
        }
    }

    public static class Images{
        public static final String IMAGE_SOUND = "sound.png";
        public static final String IMAGE_MUTE = "mute.png";

        public static Image getImage(Class class_, String name){
            String path = "../Images/" + name;
            return new Image(class_.getResource(path).toExternalForm());
        }
    }
}
