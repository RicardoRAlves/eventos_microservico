package com.br.capoeira.eventos.event_api.service.aws;

import com.br.capoeira.eventos.event_api.config.exception.FileException;
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
import java.net.URISyntaxException;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    @Value("${s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    public URI uploadFile(MultipartFile multipartFile) {
        try {
            log.info("uploadFile started");
            String fileName = multipartFile.getOriginalFilename();
            InputStream inputStream = multipartFile.getInputStream();
            String contentType = multipartFile.getContentType();
            return uploadFile(inputStream, fileName, contentType);
        } catch (IOException e) {
            throw  new FileException("Error while trying to upload S3 file "+ e.getMessage());
        }

    }

    private URI uploadFile(InputStream inputStream, String fileName, String contentType) {
        try {

            var putOb = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putOb, RequestBody.fromInputStream(inputStream, inputStream.available()));

            log.info("uploadFile fim");

            return s3Client.utilities()
                    .getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileName).build())
                    .toURI();
        } catch (URISyntaxException e) {
            throw  new FileException("Erro ao converter URL para URI");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteImage(String fileName){
        String nomeFile = fileName.substring(fileName.indexOf(".com/") + 5);
        var deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteRequest);
    }
}
