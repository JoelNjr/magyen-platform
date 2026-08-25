package com.magyen.platform.production.infrastructure.storage;

import com.magyen.platform.production.application.dto.ProductionReferenceImageContent;
import com.magyen.platform.production.application.port.ProductionReferenceImageStoragePort;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador S3-compatible para Cloudflare R2. No expone credenciales ni URLs públicas.
 */
public class S3ProductionReferenceImageStorageAdapter implements ProductionReferenceImageStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3ProductionReferenceImageStorageAdapter(S3Client s3Client, String bucket) {
        this.s3Client = Objects.requireNonNull(s3Client, "S3 client must not be null");
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("Object storage bucket must not be blank");
        }
        this.bucket = bucket;
    }

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        Objects.requireNonNull(objectKey, "Object key must not be null");
        Objects.requireNonNull(content, "Content must not be null");
        Objects.requireNonNull(contentType, "Content type must not be null");
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(contentType)
                        .contentLength((long) content.length)
                        .build(),
                RequestBody.fromBytes(content)
        );
    }

    @Override
    public Optional<ProductionReferenceImageContent> get(String objectKey) {
        Objects.requireNonNull(objectKey, "Object key must not be null");
        try {
            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );
            String contentType = response.response().contentType();
            return Optional.of(new ProductionReferenceImageContent(response.asByteArray(), contentType));
        } catch (NoSuchKeyException exception) {
            return Optional.empty();
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                return Optional.empty();
            }
            throw exception;
        }
    }

    @Override
    public void delete(String objectKey) {
        Objects.requireNonNull(objectKey, "Object key must not be null");
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(objectKey)
                            .build()
            );
        } catch (NoSuchKeyException exception) {
            // Already absent.
        } catch (S3Exception exception) {
            if (exception.statusCode() != 404) {
                throw exception;
            }
        }
    }
}
