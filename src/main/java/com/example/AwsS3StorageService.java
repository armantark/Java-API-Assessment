package com.example;

import java.io.ByteArrayOutputStream;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

// References as follows, as well as some miscellaneous sources
// https://www.codejava.net/aws
public class AwsS3StorageService implements StorageService {
    private final S3Client s3Client;
    private String bucketName;

    /**
     * Construct the storage service given AWS credentials
     * @param accessKeyId The access key given by AWS
     * @param secretAccessKey The secret key given by AWS
     * @param region The region of the S3 bucket
     * @param bucketName The name of the S3 bucket
     */
    public AwsS3StorageService(String accessKeyId, String secretAccessKey, String region, String bucketName) {
        this.s3Client = S3Client.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                .region(Region.of(region))
                .build();
        this.bucketName = bucketName;
    }

    @Override
    public boolean createFile(String connectionName, String filePath, String fileName, byte[] fileContents) {
        // Create a request to put an object in the bucket given file path and name
        String key = filePath + fileName;
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Use a request body object to parse the fileContents byte array
        RequestBody requestBody = RequestBody.fromBytes(fileContents);
        // Attempt to put into the S3 client, return false if fail
        try {
            s3Client.putObject(request, requestBody);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public byte[] downloadFile(String connectionName, String filePath, String fileName) {
        // Create a request to get an object in the bucket given file path and name
        String key = filePath + fileName;
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Attempt to retrieve buffer from S3 client via output stream, return null if fail
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {

            // Read the contents of the ResponseInputStream into the ByteArrayOutputStream
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            
            while ((bytesRead = response.read(buffer)) !=  -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            response.close();

            return outputStream.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean deleteFile(String connectionName, String filePath, String fileName) {
        // Create a request to delete an object in the bucket given file path and name
        String key = filePath + fileName;
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        // Attempt to delete from the S3 client, return false if fail
        try {
            s3Client.deleteObject(request);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

