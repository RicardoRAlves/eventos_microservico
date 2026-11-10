package com.br.capoeira.eventos.event_api.service.aws;

import com.br.capoeira.eventos.event_api.config.exception.FileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${s3.bucket}")
    private String bucketName;

    @Value("${s3.public-url:}")
    private String publicUrl;

    private final S3Client s3Client;

    public URI uploadFile(MultipartFile multipartFile) {
        String key = multipartFile.getOriginalFilename();
        if (key == null || key.isBlank()) {
            throw new FileException("Invalid file name");
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            var requestBuilder = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key);

            String contentType = multipartFile.getContentType();
            if (contentType != null && !contentType.isBlank()) {
                requestBuilder.contentType(contentType);
            }

            s3Client.putObject(
                    requestBuilder.build(),
                    RequestBody.fromInputStream(inputStream, multipartFile.getSize())
            );

            log.info("S3 upload finished. key={}", key);
            return buildFileUrl(key);
        } catch (IOException e) {
            throw new FileException("Error while trying to read S3 upload file", e);
        } catch (URISyntaxException e) {
            throw new FileException("Error while generating S3 object URL", e);
        } catch (SdkException e) {
            throw new FileException("Error while trying to upload S3 file", e);
        }
    }

    private URI buildFileUrl(String key) throws URISyntaxException {
        if (publicUrl != null && !publicUrl.isBlank()) {
            String baseUrl = publicUrl.replaceAll("/+$", "");
            String encodedKey = Arrays.stream(key.split("/", -1))
                    .map(this::encodePathSegment)
                    .collect(Collectors.joining("/"));
            return URI.create(baseUrl + "/" + bucketName + "/" + encodedKey);
        }

        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucketName).key(key).build())
                .toURI();
    }

    private String encodePathSegment(String segment) {
        return URLEncoder.encode(segment, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    public void deleteImage(String fileUrl) {
        try {
            String key = extractObjectKey(fileUrl);
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("S3 delete finished. key={}", key);
        } catch (FileException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new FileException("Invalid S3 object URL", e);
        } catch (SdkException e) {
            throw new FileException("Error while trying to delete S3 file", e);
        }
    }

    private String extractObjectKey(String fileUrl) {
        URI uri = URI.create(fileUrl);
        validateHttpUrl(uri);

        String rawKey;
        if (isConfiguredPublicEndpoint(uri)) {
            rawKey = extractPathStyleKey(uri, URI.create(publicUrl));
        } else if (isAwsVirtualHostedUrl(uri)) {
            rawKey = removeLeadingSlash(uri.getRawPath());
        } else if (isAwsPathStyleUrl(uri)) {
            rawKey = removeBucketPrefix(removeLeadingSlash(uri.getRawPath()));
        } else {
            throw new FileException("Unsupported S3 object URL origin");
        }

        if (rawKey.isBlank()) {
            throw new FileException("Invalid S3 object key");
        }

        return URLDecoder.decode(rawKey.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private void validateHttpUrl(URI uri) {
        boolean http = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!http || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new FileException("Invalid S3 object URL");
        }
    }

    private boolean isConfiguredPublicEndpoint(URI uri) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return false;
        }
        URI configured = URI.create(publicUrl);
        return sameEndpoint(uri, configured);
    }

    private String extractPathStyleKey(URI objectUri, URI baseUri) {
        String basePath = baseUri.getRawPath() == null
                ? ""
                : baseUri.getRawPath().replaceAll("/+$", "");
        String expectedPrefix = basePath + "/" + bucketName + "/";
        String path = objectUri.getRawPath();
        if (path == null || !path.startsWith(expectedPrefix)) {
            throw new FileException("S3 object URL does not belong to the configured bucket");
        }
        return path.substring(expectedPrefix.length());
    }

    private boolean isAwsVirtualHostedUrl(URI uri) {
        String bucket = Pattern.quote(bucketName.toLowerCase(Locale.ROOT));
        return uri.getHost().toLowerCase(Locale.ROOT).matches(
                bucket + "\\.s3(?:[.-][a-z0-9-]+)?\\.amazonaws\\.com(?:\\.cn)?"
        );
    }

    private boolean isAwsPathStyleUrl(URI uri) {
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        return host.matches("s3(?:[.-][a-z0-9-]+)?\\.amazonaws\\.com(?:\\.cn)?")
                && removeLeadingSlash(uri.getRawPath()).startsWith(bucketName + "/");
    }

    private String removeBucketPrefix(String path) {
        String prefix = bucketName + "/";
        if (!path.startsWith(prefix)) {
            throw new FileException("S3 object URL does not belong to the configured bucket");
        }
        return path.substring(prefix.length());
    }

    private String removeLeadingSlash(String path) {
        if (path == null || path.length() <= 1 || path.charAt(0) != '/') {
            throw new FileException("Invalid S3 object URL");
        }
        return path.substring(1);
    }

    private boolean sameEndpoint(URI first, URI second) {
        return first.getScheme().equalsIgnoreCase(second.getScheme())
                && first.getHost().equalsIgnoreCase(second.getHost())
                && effectivePort(first) == effectivePort(second);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() >= 0) {
            return uri.getPort();
        }
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }
}
