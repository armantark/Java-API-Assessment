package com.example;

// Any storage service will implement this interface with the three specified routes
public interface StorageService {
    /**
     * Send the API request to the storage service to create a new file
     * @param connectionName either AWS or Azure supported at the moment
     * @param filePath String representing the path to the file, e.g. /folder/
     * @param fileName String representing the file name, e.g. hello.txt
     * @param fileContents Byte array contents of the file
     * @return Boolean should be true if successful, false otherwise
     */
    boolean createFile(String connectionName, String filePath, String fileName, byte[] fileContents);

    /** Send the API request to the storage service to download a file
     * @param connectionName either AWS or Azure supported at the moment
     * @param filePath String representing the path to the file, e.g. /folder/
     * @param fileName String representing the file name, e.g. hello.txt
     * @return Byte array contents of the file
     */
    byte[] downloadFile(String connectionName, String filePath, String fileName);
    
    /**
     * Send the API request to the storage service to delete a file
     * @param connectionName either AWS or Azure supported at the moment
     * @param filePath String representing the path to the file, e.g. /folder/
     * @param fileName String representing the file name, e.g. hello.txt
     * @return Boolean should be true if successful, false otherwise
     */
    boolean deleteFile(String connectionName, String filePath, String fileName);
}