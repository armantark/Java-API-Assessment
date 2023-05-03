package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import spark.Spark;

// https://www.tutorialspoint.com/junit/junit_environment_setup.htm
// https://docs.oracle.com/en/java/javase/12/docs/api/java.net.http/java/net/http/package-summary.html
// https://www.vogella.com/tutorials/JUnitRestAPI/article.html
// https://www.baeldung.com/parameterized-tests-junit-5
public class AppTest {

    @BeforeAll
    public static void setUp() {
        App.main(new String[0]);
    }

    @AfterAll
    public static void stopServer() {
        Spark.stop();
    }

    private HttpResponse<String> createTestFile(String connectionName, String filePath, String fileName, String fileContent, HttpClient client) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:4567/api/files/create-file"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                                "connectionName=" + connectionName
                                + "&filePath=" + filePath
                                + "&fileName=" + fileName
                                + "&fileContents=" + fileContent
                ))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(request.toString());
        assertEquals(200, response.statusCode());
        return response;
    }

    @ParameterizedTest
    @ValueSource(strings = {"azure", "aws"})
    public void testCreateFile(String connectionName) throws Exception {
        String filePath = "testfolder/";
        String fileName = "test.txt";
        String fileContent = "testing testing 123";
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = createTestFile(connectionName, filePath, fileName, fileContent, client);

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        assertTrue(jsonResponse.get("successful").getAsBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"azure", "aws"})
    public void testDownloadFile(String connectionName) throws Exception {
        String filePath = "testfolder/";
        String fileName = "test.txt";
        String expected = "testing testing 123";
        HttpClient client = HttpClient.newHttpClient();
        createTestFile(connectionName, filePath, fileName, expected, client);

        HttpRequest downloadRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4567/api/files/download-file?connectionName=" + connectionName 
                    + "&filePath=" + filePath 
                    + "&fileName=" + fileName))
            .GET()
            .build();

        HttpResponse<byte[]> downloadResponse = client.send(downloadRequest, HttpResponse.BodyHandlers.ofByteArray());

        assertEquals(200, downloadResponse.statusCode());
        assertEquals("application/octet-stream", downloadResponse.headers().firstValue("Content-Type").orElse(""));
        assertEquals("attachment; filename=" + fileName, downloadResponse.headers().firstValue("Content-Disposition").orElse(""));
        
        String downloadedFileContent = new String(downloadResponse.body(), StandardCharsets.UTF_8);
        assertEquals(expected, downloadedFileContent);
    }


    @ParameterizedTest
    @ValueSource(strings = {"azure", "aws"})
    public void testDeleteFile(String connectionName) throws Exception {
        String filePath = "testfolder/";
        String fileName = "test.txt";
        String fileContent = "testing testing 123";
        HttpClient client = HttpClient.newHttpClient();
        createTestFile(connectionName, filePath, fileName, fileContent, client);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:4567/api/files/delete-file?connectionName=" + connectionName 
                    + "&filePath=" + filePath 
                    + "&fileName=" + fileName))
            .DELETE()
            .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());

        JsonObject jsonResponse = JsonParser.parseString(deleteResponse.body()).getAsJsonObject();
        assertTrue(jsonResponse.get("successful").getAsBoolean());
    }

}
