package azhy;

import org.json.JSONObject;

public class PreparedText {
    private static final int WORDS_LIMIT = 100;

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_WORDS = "words";

    public String name, type;
    private String value;
    public String text = "";

    public PreparedText(String name, String type){
        this.name = name;
        this.type = type;
    }

    public PreparedText(String name, String type, String value){
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getValue(Class class_){
        if(value != null) return value;
        FileFactory.Texts texts = new FileFactory.Texts();
        return texts.getValue(class_, name);
    }

    public void loadValue(Class class_){
        if(value != null) return;
        FileFactory.Texts texts = new FileFactory.Texts();
        value = texts.getValue(class_, name);
    }

    public void deleteValue(){
        value = null;
    }

    public String getText(Class class_){
        if(!text.equals("")) return text;

        String value = this.value == null? getValue(class_): this.value;
        if(type.equals(TYPE_TEXT)) {
            text = value.replaceAll("(?:\n|\t|\\s+)+", " ");//.replace("\n", "↲");//↩︎︎↲↵
            return text;
        }

        String[] words = value.split("\\|");
        StringBuilder result = new StringBuilder();
        for(int i=0; i<WORDS_LIMIT; i++){
            int rnd = (int) (Math.random() * words.length);
            if(i!=0) result.append(" ");
            result.append(words[rnd]);
        }
        text = result.toString();
        return text;
    }

    public void regenerateText(Class class_){
        if(type.equals(TYPE_TEXT)) return;

        String value = this.value == null? getValue(class_): this.value;
        String[] words = value.split("\\|");
        StringBuilder result = new StringBuilder();
        for(int i=0; i<WORDS_LIMIT; i++){
            int rnd = (int) (Math.random() * words.length);
            if(i!=0) result.append(" ");
            result.append(words[rnd]);
        }
        text = result.toString();
    }

    public JSONObject toJSONObject(Class class_){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("type", type);
        jsonObject.put("value", getValue(class_));
        return jsonObject;
    }
}
