package vladyslavpohrebniakov.depressingthoughts.retrofit;

public class Tweet {

    private String text;

    public String getText() {
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        return text;
    }
}
