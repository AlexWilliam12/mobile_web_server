package com.example.minimalistserver.utils;

import java.io.BufferedReader;
import java.io.IOException;

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

    public int parseContentLength(BufferedReader reader) throws NumberFormatException, IOException {
        String line;
        int contentLength = 0;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            if (line.contains("Content-Length")) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
            }
        }
        return contentLength;
    }
}
