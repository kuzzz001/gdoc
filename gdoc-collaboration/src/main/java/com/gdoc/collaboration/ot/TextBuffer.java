package com.gdoc.collaboration.ot;

public class TextBuffer {
    private String text;

    public TextBuffer(String text) {
        this.text = text != null ? text : "";
    }

    public String getText() {
        return text;
    }

    public int length() {
        return text.length();
    }

    public String slice(int start, int end) {
        if (start < 0) start = 0;
        if (end > text.length()) end = text.length();
        if (start >= end) return "";
        return text.substring(start, end);
    }

    public void delete(int start, int end) {
        if (start < 0) start = 0;
        if (end > text.length()) end = text.length();
        if (start >= end) return;
        text = text.substring(0, start) + text.substring(end);
    }

    public void insert(int pos, String str) {
        if (str == null || str.isEmpty()) return;
        if (pos < 0) pos = 0;
        if (pos > text.length()) pos = text.length();
        text = text.substring(0, pos) + str + text.substring(pos);
    }

    public TextBuffer clone() {
        return new TextBuffer(text);
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TextBuffer other = (TextBuffer) obj;
        return text.equals(other.text);
    }

    public int hashCode() {
        return text.hashCode();
    }
}
