package unit.com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.*;
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
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private OrganizationClient organizationClient;

    @InjectMocks
    private EventService eventService;

    @Test
    void shouldCreateEventWithSuccess() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        var category = getMockCategory();
        var response = getMockEventResponseDto();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(organizationClient.findUnitById(10L)).thenReturn(organizationResponse);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(event)).thenReturn(event);
        when(eventMapper.eventToResponseDto(event, category)).thenReturn(response);

        var result = eventService.sendingNewEventToProcessor(request);

        assertThat(result).isNotNull();
        assertThat(event.getTransactionId()).isNotBlank();

        verify(eventMapper).createRequestDtoToEvent(request);
        verify(organizationClient).findUnitById(10L);
        verify(categoryRepository).findByName("Capoeira");
        verify(repository).save(event);
        verify(eventMapper).eventToResponseDto(event, category);
        verify(producer).sendingNewEventToProcessor(response);
    }

    @Test
    void shouldUpdateEventWithSuccess() {
        var request = getMockEventUpdateRequestDto();
        var event = getMockEvent();
        event.setTransactionId("tx-123");

        var category = getMockCategory();
        var response = getMockEventResponseDto();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(event));

        when(organizationClient.findUnitById(10L))
                .thenReturn(organizationResponse);

        when(categoryRepository.findByName("Capoeira"))
                .thenReturn(Optional.of(category));

        when(repository.save(event)).thenReturn(event);

        when(eventMapper.eventToResponseDto(event, category))
                .thenReturn(response);

        var result = eventService.updateEvent(request);

        assertThat(result).isNotNull();

        verify(repository).findByTransactionId("tx-123");
        verify(eventMapper).updateRequestDtoToEvent(request, event);
        verify(organizationClient).findUnitById(10L);
        verify(categoryRepository).findByName("Capoeira");
        verify(repository).save(event);
        verify(producer).sendingEventUpdatedToProcessor(response);
    }

    @Test
    void shouldThrowValidationExceptionWhenEventToUpdateWasNotFound() {
        var request = getMockEventUpdateRequestDto();

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Event not found");

        verify(repository).findByTransactionId("tx-123");
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldUploadPhotoWithSuccess() throws Exception {
        var file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(s3Service.uploadFile(file))
                .thenReturn(new URI("https://bucket.s3.amazonaws.com/photo.jpg"));

        var result = eventService.updatePhoto(file);

        assertThat(result).isEqualTo("https://bucket.s3.amazonaws.com/photo.jpg");

        verify(s3Service).uploadFile(file);
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsNull() {
        assertThatThrownBy(() -> eventService.updatePhoto(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image file must be provided");

        verifyNoInteractions(s3Service);
    }

    @Test
    void shouldThrowValidationExceptionWhenPhotoIsEmpty() {
        var file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> eventService.updatePhoto(file))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image file must be provided");

        verifyNoInteractions(s3Service);
    }

    @Test
    void shouldAskForSendingAllEvents() {
        eventService.findAllEvents();

        verify(producer).askingForSendingAllEvents();
    }

    @Test
    void shouldSendCreateErrorToNotificationAndDeactivateEventWhenExists() {
        var event = getMockEvent();
        event.setTransactionId("tx-123");
        event.setActive(true);

        var response = getMockEventResponseDto();

        when(repository.findByTransactionId(response.getTransactionId()))
                .thenReturn(Optional.of(event));

        eventService.sendingCreateErrorToNotification(response);

        assertThat(event.getActive()).isFalse();

        verify(repository).save(event);
        verify(producer).sendingErrorCreateEventToNotification(response);
    }

    @Test
    void shouldSendCreateErrorToNotificationWithoutSavingWhenEventDoesNotExist() {
        var response = getMockEventResponseDto();

        when(repository.findByTransactionId(response.getTransactionId()))
                .thenReturn(Optional.empty());

        eventService.sendingCreateErrorToNotification(response);

        verify(repository, never()).save(any());
        verify(producer).sendingErrorCreateEventToNotification(response);
    }

    @Test
    void shouldDeleteEventByTransactionIdWithSuccess() {
        var request = new EventDeleteRequestDto("tx-123");

        var event = getMockEvent();
        event.setTransactionId("tx-123");
        event.setActive(true);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(event));

        eventService.deleteEventByTransactionId(request);

        assertThat(event.getActive()).isFalse();

        verify(repository).save(event);
        verify(producer).sendingEventDeletedToProcessor(any(EventDeleteResponseDto.class));
    }

    @Test
    void shouldNotThrowWhenDeletingEventThatDoesNotExist() {
        var request = new EventDeleteRequestDto("tx-123");

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.empty());

        eventService.deleteEventByTransactionId(request);

        verify(repository, never()).save(any());
        verify(producer, never()).sendingEventDeletedToProcessor(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenCategoryIsInactive() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        var category = getMockCategory();
        category.setActive(false);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        when(organizationClient.findUnitById(10L))
                .thenReturn(new OrganizationResponseDto(1L, 10L));

        when(categoryRepository.findByName("Capoeira"))
                .thenReturn(Optional.of(category));

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category not found");

        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationUnitDoesNotBelongToOrganization() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(organizationClient.findUnitById(10L))
                .thenReturn(new OrganizationResponseDto(10L, 999L));

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id does not match the informed organization unit");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationScopeWithoutOrganizationId() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(EventScope.ORGANIZATION);
        event.setOrganizationId(null);
        event.setOrganizationUnitId(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization id must be informed");

        verifyNoInteractions(organizationClient);
        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenOrganizationUnitScopeWithoutUnitId() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(EventScope.ORGANIZATION_UNIT);
        event.setOrganizationUnitId(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Organization unit id must be informed");

        verifyNoInteractions(organizationClient);
        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldCreatePublicEventWithoutOrganizationValidationWhenIdsAreNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(EventScope.PUBLIC);
        event.setOrganizationId(null);
        event.setOrganizationUnitId(null);

        var category = getMockCategory();
        var response = getMockEventResponseDto();

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(categoryRepository.findByName("Capoeira"))
                .thenReturn(Optional.of(category));
        when(repository.save(event)).thenReturn(event);
        when(eventMapper.eventToResponseDto(event, category)).thenReturn(response);

        var result = eventService.sendingNewEventToProcessor(request);

        assertThat(result).isNotNull();

        verifyNoInteractions(organizationClient);
        verify(repository).save(event);
        verify(producer).sendingNewEventToProcessor(response);
    }

    @Test
    void shouldThrowValidationExceptionWhenTitleIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setTitle("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Title must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenDescriptionIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDescription("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Description must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenDateStartedIsNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateStarted(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date started of the event must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenDateFinishedIsNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateFinished(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date finished of the event must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenDateFinishedIsBeforeDateStarted() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setDateStarted(LocalDateTime.of(2026, 5, 10, 22, 0));
        event.setDateFinished(LocalDateTime.of(2026, 5, 10, 19, 0));

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Date finished must be after date started");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenLocationNameIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setLocationName("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Location name must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenAddressIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setAddress("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Address must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenTypeContactIsNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setTypeContact(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Type contact must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenContactIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setContact("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Contact must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenImageIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setImage("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Image must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCategoryNameIsBlank() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setCategoryName("");

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category name must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenScopeIsNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(null);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Scope must be informed");

        verifyNoInteractions(categoryRepository);
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCategoryDoesNotExist() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(organizationClient.findUnitById(10L)).thenReturn(organizationResponse);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Category not found");

        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldCreatePublicEventAndValidateOrganizationWhenBothIdsAreInformed() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(EventScope.PUBLIC);
        event.setOrganizationId(1L);
        event.setOrganizationUnitId(10L);

        var category = getMockCategory();
        var response = getMockEventResponseDto();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(organizationClient.findUnitById(10L)).thenReturn(organizationResponse);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(event)).thenReturn(event);
        when(eventMapper.eventToResponseDto(event, category)).thenReturn(response);

        var result = eventService.sendingNewEventToProcessor(request);

        assertThat(result).isNotNull();

        verify(organizationClient).findUnitById(10L);
        verify(repository).save(event);
        verify(producer).sendingNewEventToProcessor(response);
    }

    @Test
    void shouldCreateOrganizationEventWithoutUnitValidationWhenUnitIdIsNull() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        event.setScope(EventScope.ORGANIZATION);
        event.setOrganizationId(1L);
        event.setOrganizationUnitId(null);

        var category = getMockCategory();
        var response = getMockEventResponseDto();

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(event)).thenReturn(event);
        when(eventMapper.eventToResponseDto(event, category)).thenReturn(response);

        var result = eventService.sendingNewEventToProcessor(request);

        assertThat(result).isNotNull();

        verifyNoInteractions(organizationClient);
        verify(repository).save(event);
        verify(producer).sendingNewEventToProcessor(response);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnCreate() {
        var request = getMockEventCreateRequestDto();
        var event = getMockEvent();
        var category = getMockCategory();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(eventMapper.createRequestDtoToEvent(request)).thenReturn(event);
        when(organizationClient.findUnitById(10L)).thenReturn(organizationResponse);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(event)).thenThrow(new RuntimeException("database error"));

        assertThatThrownBy(() -> eventService.sendingNewEventToProcessor(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error while trying to send event to processor. transactionId=");

        verify(repository).save(event);
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnUpdate() {
        var request = getMockEventUpdateRequestDto();
        var event = getMockEvent();
        event.setTransactionId("tx-123");

        var category = getMockCategory();

        var organizationResponse = new OrganizationResponseDto();
        organizationResponse.setOrganizationUnitId(10L);
        organizationResponse.setOrganizationId(1L);

        when(repository.findByTransactionId("tx-123")).thenReturn(Optional.of(event));
        when(organizationClient.findUnitById(10L)).thenReturn(organizationResponse);
        when(categoryRepository.findByName("Capoeira")).thenReturn(Optional.of(category));
        when(repository.save(event)).thenThrow(new RuntimeException("database error"));

        assertThatThrownBy(() -> eventService.updateEvent(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error while trying to update event. transactionId=tx-123");

        verify(repository).save(event);
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnDelete() {
        var request = new EventDeleteRequestDto("tx-123");

        var event = getMockEvent();
        event.setTransactionId("tx-123");

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(event));

        when(repository.save(event))
                .thenThrow(new RuntimeException("database error"));

        assertThatThrownBy(() -> eventService.deleteEventByTransactionId(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error while trying to delete event. transactionId=tx-123");

        verify(repository).save(event);
        verify(producer, never()).sendingEventDeletedToProcessor(any());
    }

    private Event getMockEvent() {
        return Event.builder()
                .title("Batizado Bonfim")
                .description("Evento anual de capoeira")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0))
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
                LocalDateTime.of(2026, 5, 10, 19, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,
                1L,
                10L
        );
    }

    private EventUpdateRequestDto getMockEventUpdateRequestDto() {
        return new EventUpdateRequestDto(
                "tx-123",
                "Batizado Bonfim",
                "Evento anual de capoeira",
                LocalDateTime.of(2026, 5, 10, 19, 0),
                LocalDateTime.of(2026, 5, 10, 22, 0),
                "Academia Central",
                "Rua das Flores, 100",
                TypeContact.WHATSAPP,
                "11999999999",
                "https://image.com/evento.png",
                "Capoeira",
                EventScope.ORGANIZATION_UNIT,
                1L,
                10L,
                1L
        );
    }

    private EventResponseDto getMockEventResponseDto() {
        return EventResponseDto.builder()
                .transactionId("tx-123")
                .title("Batizado Bonfim")
                .description("Evento anual de capoeira")
                .dateStarted(LocalDateTime.of(2026, 5, 10, 19, 0))
                .dateFinished(LocalDateTime.of(2026, 5, 10, 22, 0))
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