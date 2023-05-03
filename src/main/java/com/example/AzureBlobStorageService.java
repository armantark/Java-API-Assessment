package com.example;

import com.azure.storage.blob.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

// References as follows, as well as some miscellaneous sources
// https://learn.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java?tabs=powershell%2Cmanaged-identity%2Croles-azure-portal%2Csign-in-azure-cli
public class AzureBlobStorageService implements StorageService {
    private final BlobServiceClient blobServiceClient;

    /**
     * Construct the storage service given a connection string
     * @param connectionString as given in the factory, to connect to Azure
     */
    public AzureBlobStorageService(String connectionString) {
        this.blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Override
    public boolean createFile(String connectionName, String filePath, String fileName, byte[] fileContents) {
        // Use client to get a blob container client given filepath and create a new one if it doesn't exist
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(filePath);
        containerClient.createIfNotExists();

        // Get the blob client based off the given file name, attempt to upload, return false if it fails
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents)) {
            blobClient.upload(inputStream, fileContents.length);
            inputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] downloadFile(String connectionName, String filePath, String fileName) {
        // Use client to get a blob container client given filepath
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(filePath);

        // Get the blob client based off the given file name, attempt to download, return null if it fails
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
            byte[] arr = outputStream.toByteArray();
            outputStream.close();
            return arr;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean deleteFile(String connectionName, String filePath, String fileName) {
        // Use client to get a blob container client given filepath
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(filePath);

        // Get the blob client based off the given file name, attempt to delete, return false if it fails
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        try {
            blobClient.deleteIfExists();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

