package com.bookreaderai.backend.controller;

import com.bookreaderai.backend.BlobStorage.S3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.bookreaderai.backend.service.DocumentVectorService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Slf4j
public class FileController {

    private final S3 s3client;
    private final DocumentVectorService vectorService;

    /**
     * Accepts a multipart request:
     *   - part "file"       : the file itself
     *   - part "bookName"   : plain-text field
     *   - part "authorName" : plain-text field
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file,
                                             @RequestPart("bookName") String bookName,
                                             @RequestPart("authorName") String authorName) throws IOException {

        // Convert MultipartFile to a temporary java.io.File
        File temp = File.createTempFile("upload-", "-" + file.getOriginalFilename());
        file.transferTo(temp.toPath());

        // Metadata to store alongside the object
        Map<String, String> meta = Map.of(
                "bookName", bookName,
                "authorName", authorName
        );

        String bucket = "bookreader-bucket-laxmi";
        log.info("Bucket creation started");
        try{
            s3client.createBucketIfNotExists(bucket);
        }
        catch (Exception e){
            log.info("bucket creation failed "+e.getMessage());
        }
        log.info("file upload started");
        String key = file.getOriginalFilename();
        try{
            s3client.uploadFile(bucket, key, temp, meta);
        }catch (Exception e){
            log.info("file upload to bucket failed "+ e.getMessage());
            throw e;
        }

        // after successful S3 upload, run vector-store pipeline in background
        try {
            vectorService.processAndStore(temp.toPath(), bookName, meta);
        } catch (Exception e) {
            log.error("Vector store pipeline failed for {}", bookName, e);
            throw new RuntimeException("Vector processing failed: " + e.getMessage(), e);
        }

        // Immediately return a presigned download URL so the client knows how to fetch the file
        String presignedUrl = s3client.generatePresignedUrl(bucket, key);
        return ResponseEntity.ok(presignedUrl);
    }

    @GetMapping("/presignedurl/{fileName}")
    public ResponseEntity<String> getPresignedUrl(@PathVariable String fileName) {
        String bucket = "bookreader-bucket-laxmi";
        String presignedUrl = s3client.generatePresignedUrl(bucket, fileName);
        return ResponseEntity.ok(presignedUrl);
    }
    @GetMapping("/download")
    public ResponseEntity<String> getDownloadUrl(@RequestParam String key) {
        String bucket = "bookreader-bucket-laxmi";
        String presignedUrl = s3client.generatePresignedUrl(bucket, key);
        return ResponseEntity.ok(presignedUrl);
    }
}
