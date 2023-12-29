package com.example.minimalistserver.utils;

public class HttpParse {

    public String parseMethod(String requestLine) {
        String[] parts = requestLine.split(" ");
        return parts.length >= 1 ? parts[0] : null;
    }

    public String parsePath(String requestLine) {
        String[] parts = requestLine.split(" ");
        return (parts.length >= 2) ? parts[1] : "/";
    }

    public String parsePathVariable(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }
}
