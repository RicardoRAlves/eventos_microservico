package unit.com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.EventCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventUpdateRequestDto;
import com.br.capoeira.eventos.event_api.dto.OrganizationResponseDto;
import com.br.capoeira.eventos.event_api.enums.EventScope;
import com.br.capoeira.eventos.event_api.enums.TypeContact;
import com.br.capoeira.eventos.event_api.mapper.EventMapper;
import com.br.capoeira.eventos.event_api.model.Category;
import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.producer.EventProducer;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.repository.EventRepository;
import com.br.capoeira.eventos.event_api.restClient.OrganizationClient;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventMapper eventMapper;
    @Mock
    private EventProducer producer;
    @Mock
    private EventRepository repository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private S3Service s3Service;
    @Mock
    private OrganizationClient client;

    @InjectMocks
    private EventService eventService;

    @Test
    void whenSendNewEventToProcessorShouldSave() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        var category = getMockCategory();
        var responseDto = getMockEventResponseDto();

        when(client.findUnitById(anyLong())).thenReturn(new OrganizationResponseDto(1L, 1L));
        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(repository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.eventToResponseDto(any(Event.class), any(Category.class))).thenReturn(responseDto);
        doNothing().when(producer).sendingNewEventToProcessor(any(EventResponseDto.class));

        var response = eventService.sendingNewEventToProcessor(requestDto);

        assertThat(response).isNotNull();
        verify(client).findUnitById(anyLong());
        verify(eventMapper).createRequestDtoToEvent(any(EventCreateRequestDto.class));
        verify(categoryRepository).findByName("Capoeira");
        verify(repository).save(any(Event.class));
        verify(producer).sendingNewEventToProcessor(any(EventResponseDto.class));
        verify(eventMapper).eventToResponseDto(any(Event.class), any(Category.class));
    }

    @Test
    void whenUpdatePhotoShouldReturnPath() throws URISyntaxException {
        var fileMock = mock(MultipartFile.class);
        when(fileMock.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(any())).thenReturn(new URI("https://my-bucket.s3.amazonaws.com/photo.jpg"));

        var path = eventService.updatePhoto(fileMock);

        assertThat(path).isEqualTo("https://my-bucket.s3.amazonaws.com/photo.jpg");
        verify(s3Service).uploadFile(fileMock);
    }

    @Test
    void whenUpdatePhotoWithEmptyFileShouldThrowException() {
        var fileMock = mock(MultipartFile.class);
        when(fileMock.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> eventService.updatePhoto(fileMock))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image file must be provided");

        verifyNoInteractions(s3Service);
    }

    @Test
    void whenFindAllShouldSendMessageToQueue() {
        doNothing().when(producer).askingForSendingAllEvents();

        eventService.findAllEvents();

        verify(producer).askingForSendingAllEvents();
    }

    @Test
    void whenUpdateEventShouldSendToQueue() {
        var requestDto = getMockEventUpdateRequestDto();
        var event = getMockEvent();
        event.setTransactionId("1xkdi2393cd");

        var category = getMockCategory();
        var responseDto = getMockEventResponseDto();

        when(client.findUnitById(anyLong())).thenReturn(new OrganizationResponseDto(1L, 1L));
        when(repository.findByTransactionId(anyString())).thenReturn(Optional.of(event));
        doNothing().when(eventMapper).updateRequestDtoToEvent(any(EventUpdateRequestDto.class), any(Event.class));
        when(categoryRepository.findByName(anyString())).thenReturn(Optional.of(category));
        when(repository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.eventToResponseDto(any(Event.class), any(Category.class))).thenReturn(responseDto);
        doNothing().when(producer).sendingEventUpdatedToProcessor(any(EventResponseDto.class));

        var response = eventService.updateEvent(requestDto);

        assertThat(response).isNotNull();
        verify(client).findUnitById(anyLong());
        verify(repository).findByTransactionId(anyString());
        verify(eventMapper).updateRequestDtoToEvent(any(EventUpdateRequestDto.class), any(Event.class));
        verify(categoryRepository).findByName("Capoeira");
        verify(repository).save(any(Event.class));
        verify(producer).sendingEventUpdatedToProcessor(any(EventResponseDto.class));
        verify(eventMapper).eventToResponseDto(any(Event.class), any(Category.class));
    }

    @Test
    void whenUpdateEventNotFoundShouldThrowException() {
        var requestDto = getMockEventUpdateRequestDto();

        when(repository.findByTransactionId(requestDto.getTransactionId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Event not found");

        verify(repository).findByTransactionId(requestDto.getTransactionId());
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void whenSendingCreateErrorShouldSendToQueue() {
        var eventMock = getMockEvent();
        var eventResponseMock = getMockEventResponseDto();
        eventMock.setTransactionId("1xkdi2393cd");

        when(repository.findByTransactionId(anyString())).thenReturn(Optional.of(eventMock));
        when(repository.save(any(Event.class))).thenReturn(eventMock);
        doNothing().when(producer).sendingErrorCreateEventToNotification(any(EventResponseDto.class));

        eventService.sendingCreateErrorToNotification(eventResponseMock);

        verify(repository).findByTransactionId(anyString());
        verify(repository).save(any(Event.class));
        verify(producer).sendingErrorCreateEventToNotification(any(EventResponseDto.class));
    }

    @Test
    void whenSendingCreateErrorAndEventNotFoundShouldNotSave() {
        var eventMock = getMockEvent();
        var eventResponseMock = getMockEventResponseDto();
        eventMock.setTransactionId("1xkdi2393cd");

        when(repository.findByTransactionId(anyString())).thenReturn(Optional.empty());
        doNothing().when(producer).sendingErrorCreateEventToNotification(any(EventResponseDto.class));

        eventService.sendingCreateErrorToNotification(eventResponseMock);

        verify(repository).findByTransactionId(anyString());
        verify(repository, never()).save(any());
        verify(producer).sendingErrorCreateEventToNotification(any(EventResponseDto.class));
    }

    @Test
    void whenTitleEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setTitle(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title must be informed");

        verify(eventMapper).createRequestDtoToEvent(any(EventCreateRequestDto.class));
        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenDescriptionEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDescription(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Description must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenDateStartedEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateStarted(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date started of the event must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenDateFinishedEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateFinished(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date finished of the event must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenDateFinishedIsLessEqualToStartedShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateStarted(LocalDateTime.now());
        event.setDateFinished(LocalDateTime.now());

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date finished must be after date started");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenLocationEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setLocationName(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Location name must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenAddressEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setAddress(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Address must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenImageEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setImage(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenCategoryNameEmptyShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setCategoryName(null);

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category name must be informed");

        verifyNoInteractions(categoryRepository);
        verifyNoInteractions(repository);
        verifyNoInteractions(producer);
    }

    @Test
    void whenCategoryNotFoundShouldNotSaveNewEvent() {
        var requestDto = getMockEventCreateRequestDto();
        var event = getMockEvent();

        when(eventMapper.createRequestDtoToEvent(any(EventCreateRequestDto.class))).thenReturn(event);
        when(client.findUnitById(anyLong())).thenReturn(new OrganizationResponseDto(1L, 1L));
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category not found");

        verify(categoryRepository).findByName("Capoeira");
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    private Event getMockEvent() {
        return Event.builder()
                .title("Batizado Bonfim")
                .description("Evento anual de capoeira")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0, 0))
                .locationName("Academia Central")
                .address("Rua das Flores, 100")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("https://image.com/evento.png")
                .categoryName("Capoeira")
                .scope(EventScope.ORGANIZATION_UNIT)
                .organizationId(1L)
                .organizationUnitId(10L)
                .active(true)
                .build();
    }

    private Category getMockCategory() {
        return Category.builder()
                .id(1L)
                .name("Capoeira")
                .active(true)
                .build();
    }

    private EventCreateRequestDto getMockEventCreateRequestDto() {
        return new EventCreateRequestDto(
                "Batizado Bonfim",
                "Evento anual de capoeira",
                LocalDateTime.of(2026, 5, 10, 19, 0, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,
                10L
        );
    }

    private EventUpdateRequestDto getMockEventUpdateRequestDto() {
        return new EventUpdateRequestDto(
                "1xkdi2393cd",
                "Batizado Bonfim",
                "Evento anual de capoeira",
                LocalDateTime.of(2026, 5, 10, 19, 0, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,
                10L,
                true
        );
    }

    private EventResponseDto getMockEventResponseDto() {
        return EventResponseDto.builder()
                .transactionId("1xkdi2393cd")
                .title("Batizado Bonfim")
                .description("Evento anual de capoeira")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0, 0))
                .locationName("Academia Central")
                .address("Rua das Flores, 100")
                .typeContact(TypeContact.WHATSAPP)
                .contact("11999999999")
                .image("https://image.com/evento.png")
                .categoryName("Capoeira")
                .scope(EventScope.ORGANIZATION_UNIT)
                .organizationId(1L)
                .organizationUnitId(10L)
                .active(true)
                .build();
    }
}