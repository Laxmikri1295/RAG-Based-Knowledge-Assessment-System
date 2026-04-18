package com.bookreaderai.backend.BlobStorage;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Service
public class S3 {

    private final S3Client s3Client;
    private final S3Presigner presigner;

    /**
     * Simple constructor that builds an S3 client from hard-coded placeholders.
     * Replace the placeholder values with real credentials/region before use.
     */
    public S3() {
        String accessKey = "<login AWS and collect it>";
        String secretKey = "<login AWS and collect it>";
        String regionName = "ap-south-1";

        AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
        this.presigner = S3Presigner.builder()
                .region(Region.of(regionName))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public boolean bucketExists(String bucketName) {
        try {
            // success when bucket exists in the same region
            s3Client.headBucket(req -> req.bucket(bucketName));
            return true;
        } catch (NoSuchBucketException e) {
            // genuinely does not exist
            return false;
        } catch (S3Exception e) {
            // 301 = bucket exists but is in a different region
            if (e.statusCode() == 301) {
                return true;
            }
            throw e; // propagate other errors
        }
    }

    public void createBucketIfNotExists(String bucketName) {
        if (!bucketExists(bucketName)) {
            s3Client.createBucket(request -> request.bucket(bucketName));
        }
    }

    public void uploadFile(String bucketName, String key, File file, Map<String, String> metadata) {
        createBucketIfNotExists(bucketName);
        s3Client.putObject(
                request -> request.bucket(bucketName)
                        .key(key)
                        .metadata(metadata),
                file.toPath());
    }

    public void downloadObject(String bucketName, String key, Path downloadPath) {

        s3Client.getObject(request -> request
                .bucket(bucketName)
                .key(key),
                ResponseTransformer.toFile(downloadPath));
    }

    public String generatePresignedUrl(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // its valid for 10 min
                .getObjectRequest(getObjectRequest)
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

}
