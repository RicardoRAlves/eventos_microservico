package unit.com.br.capoeira.eventos.user_api.service.aws;

import com.br.capoeira.eventos.user_api.config.exception.FileException;
import com.br.capoeira.eventos.user_api.service.aws.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    private static final String BUCKET = "my-bucket";

    @Mock
    private S3Client s3Client;
    @Mock
    private S3Utilities s3Utilities;
    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET);
        ReflectionTestUtils.setField(s3Service, "publicUrl", "");
    }

    @Test
    void uploadPreservesNameContentTypeSizeAndClosesStream() throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        InputStream stream = org.mockito.Mockito.mock(InputStream.class);
        URL awsUrl = URI.create("https://my-bucket.s3.amazonaws.com/foto.jpg").toURL();

        when(file.getOriginalFilename()).thenReturn("foto.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn(123L);
        when(file.getInputStream()).thenReturn(stream);
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class))).thenReturn(awsUrl);

        URI result = s3Service.uploadFile(file);

        var requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        var bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        verify(stream).close();
        assertThat(requestCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(requestCaptor.getValue().key()).isEqualTo("foto.jpg");
        assertThat(requestCaptor.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(bodyCaptor.getValue().optionalContentLength()).contains(123L);
        assertThat(result).isEqualTo(awsUrl.toURI());
    }

    @Test
    void uploadBuildsEncodedPublicPathStyleUrlForAndroid() throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        byte[] content = "image".getBytes();
        ReflectionTestUtils.setField(s3Service, "publicUrl", "http://10.0.2.2:4566/");
        when(file.getOriginalFilename()).thenReturn("foto verão+.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn((long) content.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        URI result = s3Service.uploadFile(file);

        assertThat(result.toASCIIString()).isEqualTo(
                "http://10.0.2.2:4566/my-bucket/foto%20ver%C3%A3o%2B.jpg"
        );
    }

    @Test
    void deleteSupportsLocalPathStyleUrlAndDecodesKey() {
        ReflectionTestUtils.setField(s3Service, "publicUrl", "http://10.0.2.2:4566");

        s3Service.deleteImage("http://10.0.2.2:4566/my-bucket/pasta/foto%20ver%C3%A3o%2B.jpg");

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(captor.getValue().key()).isEqualTo("pasta/foto verão+.jpg");
    }

    @Test
    void deleteSupportsAwsVirtualHostedUrl() {
        s3Service.deleteImage("https://my-bucket.s3.sa-east-1.amazonaws.com/pasta/photo.jpg");

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertThat(captor.getValue().key()).isEqualTo("pasta/photo.jpg");
    }

    @Test
    void deleteRejectsUnexpectedOriginAndWrongLocalBucket() {
        ReflectionTestUtils.setField(s3Service, "publicUrl", "http://10.0.2.2:4566");

        assertThrows(FileException.class,
                () -> s3Service.deleteImage("https://example.com/my-bucket/photo.jpg"));
        assertThrows(FileException.class,
                () -> s3Service.deleteImage("http://10.0.2.2:4566/other-bucket/photo.jpg"));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void uploadWrapsReadFailureAndDoesNotCallS3() throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("foto.jpg");
        when(file.getInputStream()).thenThrow(new IOException("read error"));

        FileException exception = assertThrows(FileException.class, () -> s3Service.uploadFile(file));

        assertThat(exception.getCause()).isInstanceOf(IOException.class);
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
}
