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

    public void regenerateText(){
        if(type.equals(TYPE_TEXT)) return;

        String[] words = value.split("\\|");
        StringBuilder result = new StringBuilder();
        for(int i=0; i<WORDS_LIMIT; i++){
            int rnd = (int) (Math.random() * words.length);
            if(i!=0) result.append(" ");
            result.append(words[rnd]);
        }
        text = result.toString();
    }
}
