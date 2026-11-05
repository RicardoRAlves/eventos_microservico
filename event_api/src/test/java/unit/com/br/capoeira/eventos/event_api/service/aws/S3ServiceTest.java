package unit.com.br.capoeira.eventos.event_api.service.aws;


import com.br.capoeira.eventos.event_api.config.exception.FileException;
import com.br.capoeira.eventos.event_api.service.aws.S3Service;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Utilities s3Utilities;

    @InjectMocks
    private S3Service s3Service;

    private final String BUCKET_NAME = "my-bucket";
    private final String IMAGE_PATH = "https://my-bucket.s3.amazonaws.com/photo.jpg";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET_NAME);
    }

    private void stubS3Utilities(URL expectedURL) throws MalformedURLException {
        when(s3Client.utilities()).thenReturn(s3Utilities);
        when(s3Utilities.getUrl(any(GetUrlRequest.class)))
                .thenReturn(expectedURL);
    }

    @Test
    void uploadFile_shouldReturnUri_whenMultipartFileIsValid() throws Exception {
        // Arrange
        var multipartFile = mock(MultipartFile.class);
        var inputStream = new ByteArrayInputStream("conteudo".getBytes());
        var expectedUrl = new URL(IMAGE_PATH);

        stubS3Utilities(expectedUrl);
        when(multipartFile.getOriginalFilename()).thenReturn("foto.jpg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        // Act
        URI result = s3Service.uploadFile(multipartFile);

        // Assert
        assertThat(result).isEqualTo(expectedUrl.toURI());
        verify(s3Client).putObject(
                argThat((PutObjectRequest req) ->
                        req.bucket().equals(BUCKET_NAME) &&
                                req.key().equals("foto.jpg") &&
                                req.contentType().equals("image/jpeg")
                ),
                any(RequestBody.class)
        );
    }

    @Test
    void uploadFile_shouldCallPutObject_withCorrectBucketAndKey() throws Exception {
        var multipartFile = mock(MultipartFile.class);
        var expectedUrl = new URL(IMAGE_PATH);

        stubS3Utilities(expectedUrl);
        when(multipartFile.getOriginalFilename()).thenReturn("documento.pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(multipartFile.getContentType()).thenReturn("application/pdf");

        s3Service.uploadFile(multipartFile);

        var captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET_NAME);
        assertThat(captor.getValue().key()).isEqualTo("documento.pdf");
    }

    // ─────────────────────────────────────────────
    // deleteImage — happy path
    // ─────────────────────────────────────────────

    @Test
    void deleteImage_shouldCallDeleteObject_withCorrectBucket() {
        s3Service.deleteImage(IMAGE_PATH);

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());

        assertThat(captor.getValue().bucket()).isEqualTo(BUCKET_NAME);
    }

    @Test
    void deleteImage_shouldUseExtractedPath_notFullUrl_asKey() {
        s3Service.deleteImage(IMAGE_PATH);

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());

        assertThat(captor.getValue().key()).isEqualTo("photo.jpg");
    }

    @Test
    void uploadFile_shouldThrowFileException_whenMultipartFileThrowsIOException() throws Exception {
        var multipartFile = mock(MultipartFile.class);

        when(multipartFile.getOriginalFilename()).thenReturn("foto.jpg");
        when(multipartFile.getInputStream()).thenThrow(new IOException("read error"));

        var exception = assertThrows(
                FileException.class,
                () -> s3Service.uploadFile(multipartFile)
        );

        assertThat(exception.getMessage())
                .contains("Error while trying to upload S3 file read error");

        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_shouldThrowFileException_whenUrlCannotBeConvertedToUri() throws Exception {
        var multipartFile = mock(MultipartFile.class);
        var inputStream = new ByteArrayInputStream("conteudo".getBytes());

        URL invalidUrl = new URL("https://my-bucket.s3.amazonaws.com/photo with space.jpg");

        when(multipartFile.getOriginalFilename()).thenReturn("foto.jpg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(null);

        stubS3Utilities(invalidUrl);

        var exception = assertThrows(
                FileException.class,
                () -> s3Service.uploadFile(multipartFile)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Erro ao converter URL para URI");

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deleteImage_shouldThrowFileException_whenUriIsInvalid() {
        var invalidUri = "http://[invalid-url";

        var exception = assertThrows(
                FileException.class,
                () -> s3Service.deleteImage(invalidUri)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Invalid URI, please check it: " + invalidUri);

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
