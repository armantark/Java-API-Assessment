package com.example;

import java.util.HashMap;

// StorageService factory for more genericity
public class StorageServiceFactory {

    private final HashMap<String, StorageService> storageServices; // Dictionary as said in spec

    /**
     * Initialize the factory with the specified credentials
     * In a real world setting, it would be provided by the environment file
     */
    public StorageServiceFactory() {
        storageServices = new HashMap<>();

        // Initialize the credentials and storage services here
        // Azure
        storageServices.put("azure", new AzureBlobStorageService(
            ""
        ));
        // AWS
        storageServices.put("aws", new AwsS3StorageService(
                "",
                "",
                "",
                ""
        ));
        // Ease of potential for more services such as Google Cloud Storage
    }

    /**
     * Return the storageService associated with the connectionName
     * @param connectionName either AWS or Azure supported at the moment
     * @return the specified storageDevice in the HashMap
     */
    public StorageService getStorageService(String connectionName) {
        StorageService storageService = storageServices.get(connectionName);

        // Throw exception if connectionName not supported
        if (storageService == null) {
            throw new IllegalArgumentException("Unsupported connection name: " + connectionName);
        }

        return storageService;
    }
}
