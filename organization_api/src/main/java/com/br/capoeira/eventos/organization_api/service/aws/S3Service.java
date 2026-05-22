package com.br.capoeira.eventos.organization_api.service.aws;

import com.br.capoeira.eventos.organization_api.config.exception.FileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final String ORGANIZATION_FOLDER = "organizations";

    @Value("${s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public URI uploadFile(MultipartFile multipartFile) {
        try {
            log.info("uploadFile started");

            String fileName = multipartFile.getOriginalFilename();
            String contentType = multipartFile.getContentType();

            return uploadFile(
                    multipartFile.getInputStream(),
                    fileName,
                    contentType
            );
        } catch (IOException e) {
            throw new FileException("Error while trying to upload S3 file " + e.getMessage());
        }
    }

    public URI uploadOrganizationImage(
            MultipartFile multipartFile,
            String organizationName
    ) {
        try {
            log.info("uploadOrganizationImage started for organization={}", organizationName);

            String contentType = multipartFile.getContentType();
            String extension = extractExtension(multipartFile, contentType);

            log.info(
                    "Image metadata. originalFilename={}, contentType={}, extension={}",
                    multipartFile.getOriginalFilename(),
                    contentType,
                    extension
            );

            String key = buildOrganizationImageKey(organizationName, extension);

            return uploadFile(
                    multipartFile.getInputStream(),
                    key,
                    contentType
            );
        } catch (IOException e) {
            throw new FileException("Error while trying to upload organization image " + e.getMessage());
        }
    }

    private URI uploadFile(InputStream inputStream, String key, String contentType) {
        try {
            byte[] bytes = inputStream.readAllBytes();

            var putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromBytes(bytes)
            );

            log.info("uploadFile finished. key={}", key);

            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .build())
                    .toURI();

        } catch (Exception e) {
            throw new FileException("Erro ao fazer upload do arquivo", e);
        }
    }

    private String buildOrganizationImageKey(
            String organizationName,
            String extension
    ) {
        String organizationSlug = normalizeToSlug(organizationName);

        return "%s/%s/%s.%s".formatted(
                ORGANIZATION_FOLDER,
                organizationSlug,
                organizationSlug,
                extension
        );
    }

    private String normalizeToSlug(String value) {
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }

    private String extractExtension(
            MultipartFile multipartFile,
            String contentType
    ) {
        String originalFilename = multipartFile.getOriginalFilename();

        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".") + 1
            ).toLowerCase(Locale.ROOT);

            if (extension.equals("jpeg")) {
                return "jpg";
            }

            if (List.of("jpg", "png", "webp").contains(extension)) {
                return extension;
            }
        }

        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return "jpg";
        }

        if ("image/png".equalsIgnoreCase(contentType)) {
            return "png";
        }

        if ("image/webp".equalsIgnoreCase(contentType)) {
            return "webp";
        }

        throw new FileException("Invalid image type: " + contentType);
    }

    public void deleteImage(String fileName) {
        try {
            var key = URI.create(fileName).getPath().substring(1);

            var deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);

            log.info("deleteImage finished. key={}", key);
        } catch (IllegalArgumentException e) {
            throw new FileException("Invalid URI, please check it: " + fileName);
        }
    }
}
