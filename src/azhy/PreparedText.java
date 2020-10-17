package azhy;

public class PreparedText {
    private static final int WORDS_LIMIT = 100;

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_WORDS = "words";

    public String name, type, value;
    public String text = "";

    public PreparedText(String name, String type, String value){
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getText(){
        if(!text.equals("")) return text;

        if(type.equals(TYPE_TEXT)) {
            text = value.replace("\n", " ");//.replace("\n", "↲");//↩︎︎↲↵
            return text;
        }

        String[] words = value.split("\\|");
        StringBuilder result = new StringBuilder(words[0]);
        for(int i=0; i<WORDS_LIMIT; i++){
            result.append(" ").append(words[(int) (Math.random() * words.length)]);
        }
        text = result.toString();
        return text;
    }
}
