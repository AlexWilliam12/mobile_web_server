package com.example.minimalistserver.controller;

import android.content.Context;

import com.example.minimalistserver.models.Produto;
import com.example.minimalistserver.repository.PersistenceUnit;
import com.example.minimalistserver.utils.HttpParse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class ControllerHandler {

    private boolean serverActive = false;
    private final int PORT = 8080;
    private final ObjectMapper mapper;
    private final PersistenceUnit persistenceUnit;
    private final HttpParse parse;

    public ControllerHandler(Context context) {
        this.mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        this.parse = new HttpParse();
        this.persistenceUnit = new PersistenceUnit(context);
    }

    private final Pattern pattern = Pattern.compile("\\{.*?\\}", Pattern.DOTALL);

    public void startServer() throws IOException {
        this.serverActive = true;
        serverRunner();
    }

    public boolean isServerActive() {
        return serverActive;
    }

    public void stopServer() throws IOException {
        this.serverActive = false;
        Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(), PORT);
        PrintStream printStream = new PrintStream(socket.getOutputStream());
        printStream.println("close");
        socket.close();
        System.out.println("Server is close");
    }

    private void serverRunner() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running at port: " + PORT);

            while (serverActive) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                OutputStreamWriter outputWriter = new OutputStreamWriter(clientSocket.getOutputStream());
                try {
                    String requestLine = requestReader.readLine();

                    String method = parse.parseMethod(requestLine);
                    String path = parse.parsePath(requestLine);

                    if (method != null && (path.equals("/produto") || Pattern.matches("/produto/\\d+", path))) {
                        switch (method) {
                            case "GET":
                                handleGetRequest(path, outputWriter);
                                break;
                            case "POST":
                                handlePostRequest(requestReader, outputWriter);
                                break;
                            case "PUT":
                                handlePutRequest(requestReader, outputWriter);
                                break;
                            case "DELETE":
                                handleDeleteRequest(path, outputWriter);
                                break;
                            default:
                                sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, "Method Not Allowed", outputWriter);
                        }
                    } else {
                        sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, "Not Found", outputWriter);
                    }
                } catch (Exception error) {
                    sendResponse(
                            HttpsURLConnection.HTTP_INTERNAL_ERROR,
                            "Internal Server Error", outputWriter,
                            error.getMessage() != null ? error.getMessage() : "");
                    error.printStackTrace();
                } finally {
                    outputWriter.flush();
                    outputWriter.close();
                    requestReader.close();
                    clientSocket.close();
                }
            }
        }
    }

    private void handleGetRequest(String path, OutputStreamWriter writer) throws IOException {
        if (Pattern.matches("/produto/\\d+", path)) {
            String pathVariable = parse.parsePathVariable(path);
            if (!pathVariable.matches("-?\\d+(\\.\\d+)?")) {
                throw new IllegalArgumentException("The id must be a number");
            }
            int id = Integer.parseInt(pathVariable);
            Produto produto = persistenceUnit.select(id);
            if (produto == null) {
                throw new NoSuchElementException("Item was not found");
            }
            sendResponse(HttpsURLConnection.HTTP_OK, "OK", writer, mapper.writeValueAsString(produto));
        }
        if (path.equals("/produto")) {
            List<Produto> produtos = persistenceUnit.list();
            if (produtos.isEmpty()) {
                throw new NoSuchElementException("There is no item register");
            }
            sendResponse(HttpsURLConnection.HTTP_OK, "OK", writer, mapper.writeValueAsString(produtos));
        }
    }

    private void handlePostRequest(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        StringBuilder builder = new StringBuilder();

        int caractere;
        while ((caractere = reader.read()) != -1) {
            char c = (char) caractere;
            builder.append(c);
            if (c == '}')
                break;
        }

        Matcher matcher = pattern.matcher(builder.toString());
        String requestBody = Optional
                .ofNullable(matcher.find() ? matcher.group() : null)
                .orElseThrow(() -> new IllegalArgumentException("Invalid JSON body"));

        Produto produto = mapper.readValue(requestBody, Produto.class);

        long rowsAffected = persistenceUnit.insert(produto);
        if (rowsAffected == -1L) {
            throw new IllegalArgumentException("Item is already register");
        }
        sendResponse(HttpsURLConnection.HTTP_CREATED, "Created", writer);
    }

    private void handlePutRequest(BufferedReader reader, OutputStreamWriter writer) throws IOException {
        StringBuilder builder = new StringBuilder();

        int caractere;
        while ((caractere = reader.read()) != -1) {
            char c = (char) caractere;
            builder.append(c);
            if (c == '}')
                break;
        }

        Matcher matcher = pattern.matcher(builder.toString());
        String requestBody = Optional
                .ofNullable(matcher.find() ? matcher.group() : null)
                .orElseThrow(() -> new IllegalArgumentException("Invalid JSON body"));

        Produto produto = mapper.readValue(requestBody, Produto.class);

        int rowsAffected = persistenceUnit.update(produto);
        if (rowsAffected == 0) {
            throw new NoSuchElementException("Item was not found");
        }
        sendResponse(HttpsURLConnection.HTTP_NO_CONTENT, "No Content", writer);
    }

    private void handleDeleteRequest(String path, OutputStreamWriter writer)
            throws IOException {
        String pathVariable = parse.parsePathVariable(path);
        if (!pathVariable.matches("-?\\d+(\\.\\d+)?")) {
            throw new IllegalArgumentException("The id must be a number");
        }
        int id = Integer.parseInt(pathVariable);
        int rowsAffected = persistenceUnit.delete(id);
        if (rowsAffected == 0) {
            throw new NoSuchElementException("Item was not found");
        }
        sendResponse(HttpsURLConnection.HTTP_NO_CONTENT, "No Content", writer);
    }

    private void sendResponse(int statusCode, String statusText, OutputStreamWriter writer) throws IOException {
        writer.write(String.format(Locale.getDefault(), "HTTP/1.1 %d %s\r\n", statusCode, statusText));
        writer.write("Content-Length: 0\r\n");
        writer.write("\r\n");
    }

    private void sendResponse(int statusCode, String statusText, OutputStreamWriter writer, String responseData)
            throws IOException {
        int contentLength = responseData.getBytes().length;
        writer.write(String.format(Locale.getDefault(), "HTTP/1.1 %d %s\r\n", statusCode, statusText));
        writer.write(String.format(Locale.getDefault(), "Content-Length: %d\r\n", contentLength));
        writer.write("Content-type: application/json\r\n");
        writer.write("\r\n");
        writer.write(responseData);
    }
}