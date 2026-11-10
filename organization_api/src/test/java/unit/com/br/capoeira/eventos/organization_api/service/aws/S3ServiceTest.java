package unit.com.br.capoeira.eventos.organization_api.service.aws;

import com.br.capoeira.eventos.organization_api.config.exception.FileException;
import com.br.capoeira.eventos.organization_api.service.aws.S3Service;
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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.net.URI;

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
    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", BUCKET);
        ReflectionTestUtils.setField(s3Service, "publicUrl", "http://10.0.2.2:4566");
    }

    @Test
    void organizationUploadPreservesDeterministicNamingContentTypeAndSize() throws Exception {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        byte[] content = "logo".getBytes();
        when(file.getOriginalFilename()).thenReturn("logo.jpeg");
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getSize()).thenReturn((long) content.length);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));

        URI result = s3Service.uploadOrganizationImage(file, "Capoeira São José");

        var requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        var bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());
        assertThat(requestCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(requestCaptor.getValue().key())
                .isEqualTo("organizations/capoeira-sao-jose/capoeira-sao-jose.jpg");
        assertThat(requestCaptor.getValue().contentType()).isEqualTo("image/jpeg");
        assertThat(bodyCaptor.getValue().optionalContentLength()).contains((long) content.length);
        assertThat(result.toString()).isEqualTo(
                "http://10.0.2.2:4566/my-bucket/organizations/capoeira-sao-jose/capoeira-sao-jose.jpg"
        );
    }

    @Test
    void organizationUploadRejectsUnsupportedImageType() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("logo.gif");
        when(file.getContentType()).thenReturn("image/gif");

        assertThrows(FileException.class,
                () -> s3Service.uploadOrganizationImage(file, "Organization"));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void deleteSupportsLocalPathStyleAndAwsUrls() {
        s3Service.deleteImage(
                "http://10.0.2.2:4566/my-bucket/organizations/capoeira/foto%20principal.jpg"
        );
        s3Service.deleteImage(
                "https://my-bucket.s3.sa-east-1.amazonaws.com/organizations/capoeira/logo.jpg"
        );

        var captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, org.mockito.Mockito.times(2)).deleteObject(captor.capture());
        assertThat(captor.getAllValues().get(0).key())
                .isEqualTo("organizations/capoeira/foto principal.jpg");
        assertThat(captor.getAllValues().get(1).key())
                .isEqualTo("organizations/capoeira/logo.jpg");
    }

    @Test
    void deleteRejectsUrlFromAnotherOrigin() {
        assertThrows(FileException.class,
                () -> s3Service.deleteImage("https://example.com/my-bucket/logo.jpg"));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
