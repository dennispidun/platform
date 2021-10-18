package de.iip_ecosphere.platform.deviceMgt.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class S3Storage implements Storage {

    private String prefix;
    private MinioClient minioClient;
    private String bucket;

    public S3Storage(String prefix, MinioClient minioClient, String bucket) {
        this.prefix = prefix;
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public Set<String> list() {
        return StreamSupport.stream(this.minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucket)
                .prefix(prefix)
                .recursive(true)
                .build()).spliterator(), false)
                .map(r -> {
                try {
                    return r.get().objectName();
                } catch (ErrorResponseException
                        | InsufficientDataException
                        | InternalException
                        | InvalidKeyException
                        | InvalidResponseException
                        | IOException
                        | NoSuchAlgorithmException
                        | ServerException
                        | XmlParserException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    @Override
    public String generateDownloadUrl(String key) {
        try {
            return this.minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .object(key)
                .bucket(bucket)
                .method(Method.GET)
                .expiry(60)
                .build());
        } catch (ErrorResponseException
                | InsufficientDataException
                | InternalException
                | InvalidKeyException
                | InvalidResponseException
                | IOException
                | NoSuchAlgorithmException
                | XmlParserException
                | ServerException e) {
            e.printStackTrace();
        }
        return null;
    }
}
