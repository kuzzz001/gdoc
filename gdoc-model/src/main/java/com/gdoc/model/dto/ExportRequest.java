package com.gdoc.model.dto;

public class ExportRequest {

    private String format; // pdf, word, markdown

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}