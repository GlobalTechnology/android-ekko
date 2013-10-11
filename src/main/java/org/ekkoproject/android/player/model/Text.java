package org.ekkoproject.android.player.model;

public class Text {
    private String id;
    private String text;

    public Text(final String id, final String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }
}
