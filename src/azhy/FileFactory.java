package azhy;

import javafx.scene.image.Image;
import javafx.scene.media.Media;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public abstract class FileFactory {

    public static final boolean IS_PRODUCTION = true;
    public static final String PARENT_PATH = "/azhy";

    public static final String RESOURCE_FILE_TEXTS = "../Data/texts.json";
    public static final String RESOURCE_FILE_USER_SETTINGS = "../Data/userSettings.json";

    public static String readResourceFile(Class tClass, String filePath){
        try{
            if(IS_PRODUCTION) {
                filePath = filePath.replace("..", PARENT_PATH);
                String productionPath = getProductionPath(tClass, filePath);
                File file = new File(productionPath);
                if(file.exists()){
                    Path path = file.toPath();
                    var reader = Files.newBufferedReader(path);
                    StringBuilder result = new StringBuilder();
                    int c;
                    while((c=reader.read())!=-1) result.append((char) c);
                    return result.toString();
                }
            }

            InputStream s = tClass.getResourceAsStream(filePath);
            return new Scanner(s, "UTF-8").useDelimiter("\\A").next();
        }catch (Exception e){
            System.out.println("Error happened reading file: " + filePath);
            return null;
        }
    }
    public static void writeResourceFile(Class tClass, String filePath, String text){
        try{
            String file = IS_PRODUCTION? getProductionPath(tClass, filePath):
                    tClass.getResource(filePath).getFile();
            Path path = new File(file).toPath();
            Writer out = Files.newBufferedWriter(path, Charset.defaultCharset(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
            out.write(text);
            out.flush();
            out.close();
        }catch (Exception e){
            System.out.println("Error happened writing to file: " + filePath);
            System.out.println(e.getMessage());
        }
    }
    private static String getProductionPath(Class tClass, String filePath){
        try {
            String jarPath = new File(tClass.getProtectionDomain().getCodeSource().
                    getLocation().getFile()).getParent();

            String[] locs = filePath.split("[\\\\\\/]");
            String fileName = locs[locs.length - 1];

            return Paths.get(jarPath, fileName).toString();
        }catch (Exception e){
            System.out.println("Error getting productionPath to resource file: " + filePath);
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static class UserSettings{
        public static final String SETTINGS_LAST_CHOSEN_TEXT = "lastChosenText";
        public static final String SETTINGS_DEFAULT_TEXTS = "defaultTexts";
        public static final String SETTINGS_SOUND = "sound";
        public static final String SETTINGS_TIME = "time";
        public static final String SETTINGS_WARNING = "warning";
        public static final String SETTINGS_ABOUT_TEXT = "aboutText";
        public static final String SETTINGS_HELP_TEXT = "helpText";

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
        public static final String TYPING_SOUND = "typingSound.wav";
        public static final String TYPING_ERROR_SOUND = "typingErrorSound.wav";
        public static final String WINNING_SOUND = "winning.wav";
        public static final String STARTING_SOUND = "starting.wav";
        public static final String STARTING_1_SOUND = "starting_1.wav";

        public static Map<String, Media> loadSoundFiles(Class class_){
            Map<String, Media> result = new HashMap<>();

            result.put(TYPING_SOUND, getMedia(class_, TYPING_SOUND));
            result.put(TYPING_ERROR_SOUND, getMedia(class_, TYPING_ERROR_SOUND));
            result.put(WINNING_SOUND, getMedia(class_, WINNING_SOUND));
            result.put(STARTING_SOUND, getMedia(class_, STARTING_SOUND));
            result.put(STARTING_1_SOUND, getMedia(class_, STARTING_1_SOUND));

            return result;
        }

        private static Media getMedia(Class class_, String name){
            String path = "../SoundFiles/" + name;

            if(IS_PRODUCTION) path = path.replace("..", PARENT_PATH);

            Media media = new Media(class_.getResource(path).toExternalForm());
            return media;
        }
    }

    public static class Images{
        public static final String IMAGE_SOUND = "sound.png";
        public static final String IMAGE_MUTE = "mute.png";
        public static final String IMAGE_WARNING = "warning.png";
        public static final String IMAGE_NOT_WARNING = "not_warning.png";

        public static Image getImage(Class class_, String name){
            String path = "../Images/" + name;
            if(IS_PRODUCTION) path = path.replace("..", PARENT_PATH);
            return new Image(class_.getResource(path).toExternalForm());
        }
    }
}
