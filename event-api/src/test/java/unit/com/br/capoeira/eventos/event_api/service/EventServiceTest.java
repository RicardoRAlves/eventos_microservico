package unit.com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.producer.EventProducer;
import com.br.capoeira.eventos.event_api.repository.EventRepository;
import com.br.capoeira.eventos.event_api.service.EventService;
import com.br.capoeira.eventos.event_api.service.aws.S3Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static unit.com.br.capoeira.eventos.event_api.utils.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventProducer producer;
    @Mock
    private EventRepository repository;
    @Mock
    private S3Service s3Service;
    @InjectMocks
    private EventService eventService;

    @Test
    public void whenSendNewEventToProcessorShouldSave(){
        var event = getMockEvent();
        when(repository.save(any())).thenReturn(new Event());
        doNothing().when(producer).sendingNewEventToProcessor(any());
        eventService.sendingNewEventToProcessor(event);

        assertThat(event.getTransactionId()).isNotNull();

        verify(repository).save(any());
        verify(producer).sendingNewEventToProcessor(any());
    }

    @Test
    public void whenUpdatePhotoShouldReturnPath() throws URISyntaxException {
        var fileMock = mock(MultipartFile.class);

        when(s3Service.uploadFile(any())).thenReturn(new URI("https://my-bucket.s3.amazonaws.com/photo.jpg"));
        var path = eventService.updatePhoto(fileMock);
        assertThat(path).isNotEmpty();
    }
    @Test
    public void whenFindAllShouldReturnPath() {
        doNothing().when(producer).askingForSendingAllEvents();
        eventService.findAllEvents();
        verify(producer).askingForSendingAllEvents();
    }

    @Test
    public void whenUpdateEventShouldSendToQueue() {
        var eventMock = getMockEvent();
        eventMock.setTransactionId("1xkdi2393cd");
        when(repository.save(any())).thenReturn(eventMock);
        when(repository.existsByTransactionId(anyString())).thenReturn(true);
        doNothing().when(producer).sendingEventUpdatedToProcessor(any());
        eventService.updateEvent(eventMock);
        verify(repository).existsByTransactionId(anyString());
        verify(repository).save(any());
        verify(producer).sendingEventUpdatedToProcessor(any());
    }

    @Test
    public void whenUpdateEventNotFoundShouldThrowException() {
        var eventMock = getMockEvent();
        eventMock.setTransactionId("1xkdi2393cd");
        when(repository.existsByTransactionId(anyString())).thenReturn(false);
        assertThatThrownBy( () -> eventService.updateEvent(eventMock))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Event not found");
        verifyNoInteractions(producer);
        verify(repository, never()).save(any());
    }

    @Test
    public void whenSendingCreateErrorShouldSendToQueue() {
        var eventMock = getMockEvent();
        eventMock.setTransactionId("1xkdi2393cd");
        when(repository.save(any())).thenReturn(eventMock);
        when(repository.findByTransactionId(anyString())).thenReturn(Optional.of(eventMock));
        doNothing().when(producer).sendingErrorCreateEventToNotification(any());
        eventService.sendingCreateErrorToNotification(eventMock);
        verify(repository).findByTransactionId(anyString());
        verify(repository).save(any());
        verify(producer).sendingErrorCreateEventToNotification(any());
    }

    @Test
    public void whenSendingCreateErrorAndEventNotFoundShouldNotSave() {
        var eventMock = getMockEvent();
        eventMock.setTransactionId("1xkdi2393cd");
        when(repository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        doNothing().when(producer).sendingErrorCreateEventToNotification(any());
        eventService.sendingCreateErrorToNotification(eventMock);
        verify(repository).findByTransactionId(anyString());
        verify(repository, never()).save(any());
        verify(producer).sendingErrorCreateEventToNotification(any());
    }

    @Test
    public void whenTitleEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setTitle(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("Title must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenDescriptionEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setDescription(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Description must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenDateStartedEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setDateStarted(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Date of started of the Event must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenDateFinishedEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setDateFinished(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Date of finish of the Event must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenDateFinishedIsLessEqualToStartedShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setDateStarted(LocalDateTime.now());
        event.setDateFinished(LocalDateTime.now());

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Date of finish must be after the date of started");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenAddressEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setAddress(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Address must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    public void whenImageEmptyShouldNotSaveNewEvent() {
        var event = getMockEvent();
        event.setImage(null);

        assertThatThrownBy( () -> eventService.sendingNewEventToProcessor(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Image must be informed");
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }
}
