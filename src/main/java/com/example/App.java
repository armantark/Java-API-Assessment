package com.example;

import static spark.Spark.*;

import java.io.OutputStream;

import com.google.gson.JsonObject;


// References used, with some various Stackoverflow posts and other snippets
// https://sparkjava.com/documentation#getting-started
// Had a choice between Spark and Spring, chose Spark because it seems more lightweight
public class App {
    private static StorageServiceFactory storageServiceFactory;
    /**
     * The main method for the entire program
     * @param args The command line args
     */
    public static void main(String[] args) {
        // Initialize Spark and set the port number
        port(4567);

        exception(Exception.class, (exception, request, response) -> exception.printStackTrace());

        storageServiceFactory = new StorageServiceFactory();

        // Create-file route
        post("/api/files/create-file", (request, response) -> {
            // Parse all the query params from the request
            String connectionName = request.queryParams("connectionName"); // at the moment, only AWS or Azure
            String filePath = request.queryParams("filePath"); // String representing the path to the file, e.g. /folder/
            String fileName = request.queryParams("fileName"); // String representing the file name, e.g. hello.txt
            byte[] fileContents = request.bodyAsBytes(); // Byte array contents of the file
        
            StorageService storageService = storageServiceFactory.getStorageService(connectionName); // use factory to instantiate connection
            boolean success;
            String errorMessage;
        
            try {
                success = storageService.createFile(connectionName, filePath, fileName, fileContents); // call createFile specific to AWS or Azure
                errorMessage = "";
            } catch (Exception e) {
                success = false;
                errorMessage = e.getMessage();
            }
            
            // using Gson to generate JSON files
            JsonObject responseJson = new JsonObject();
            // add success boolean and errorMessage to the JSON
            responseJson.addProperty("successful", success);
            responseJson.addProperty("errorMessage", errorMessage);
        
            // return the JSON as a string for easy reading
            return responseJson.toString();
        });

        // Download-file route
        get("/api/files/download-file", (request, response) -> {
            // Parse all the query params from the request
            String connectionName = request.queryParams("connectionName"); // at the moment, only AWS or Azure
            String filePath = request.queryParams("filePath"); // String representing the path to the file, e.g. /folder/
            String fileName = request.queryParams("fileName");// String representing the file name, e.g. hello.txt

            StorageService storageService = storageServiceFactory.getStorageService(connectionName); // use factory to instantiate connection
            byte[] fileContents;

            try {
                fileContents = storageService.downloadFile(connectionName, filePath, fileName); // call downloadFile specific to AWS or Azure
        
                // Set the content type and content disposition headers
                response.type("application/octet-stream"); // octet-stream for binary data, as per HTTP guidelines for unknown data formats
                response.header("Content-Disposition", "attachment; filename=" + fileName); // specifies Content-Disposition header as attachment for download
        
                // Send the file contents as the response body
                OutputStream outputStream = response.raw().getOutputStream();
                outputStream.write(fileContents);
                outputStream.flush();
                outputStream.close();
                
                // return as raw data
                return response.raw();
            } catch (Exception e) {
                // Handle the error, i.e., set an error status code and message
                response.status(500);
                return null;
            }
        });

        // Delete-file route
        delete("/api/files/delete-file", (request, response) -> {
            // Parse all the query params from the request
            String connectionName = request.queryParams("connectionName"); // at the moment, only AWS or Azure
            String filePath = request.queryParams("filePath"); // String representing the path to the file, e.g. /folder/
            String fileName = request.queryParams("fileName");// String representing the file name, e.g. hello.txt
        
            StorageService storageService = storageServiceFactory.getStorageService(connectionName);
            boolean success;
        
            try {
                success = storageService.deleteFile(connectionName, filePath, fileName); // call deleteFile specific to AWS or Azure
            } catch (Exception e) {
                success = false;
            }

            // using Gson to generate JSON files
            JsonObject responseJson = new JsonObject();
            // add success boolean to the JSON
            responseJson.addProperty("successful", success);
        
            // return the JSON as a string for easy reading
            return responseJson.toString();
        });
    }
}
