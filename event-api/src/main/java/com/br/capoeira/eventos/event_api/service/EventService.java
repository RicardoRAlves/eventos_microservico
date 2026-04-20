package com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.EventCreateRequestDto;
import com.br.capoeira.eventos.event_api.dto.EventResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventUpdateRequestDto;
import com.br.capoeira.eventos.event_api.mapper.EventMapper;
import com.br.capoeira.eventos.event_api.model.Category;
import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.producer.EventProducer;
import com.br.capoeira.eventos.event_api.repository.CategoryRepository;
import com.br.capoeira.eventos.event_api.repository.EventRepository;
import com.br.capoeira.eventos.event_api.service.aws.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final EventMapper eventMapper;
    private final EventProducer producer;
    private final EventRepository repository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    public EventResponseDto sendingNewEventToProcessor(EventCreateRequestDto dto) {
        var event = eventMapper.createRequestDtoToEvent(dto);

        validateEvent(event);

        var category = findActiveCategoryByName(event.getCategoryName());

        var transactionId = String.format(
                TRANSACTION_ID_PATTERN,
                Instant.now().toEpochMilli(),
                UUID.randomUUID()
        );

        event.setTransactionId(transactionId);

        try {
            log.info("Sending new event to processor. transactionId={}", transactionId);

            repository.save(event);
            var eventResponseDto = eventMapper.eventToResponseDto(event, category);
            producer.sendingNewEventToProcessor(eventResponseDto);

            return eventResponseDto;
        } catch (Exception e) {
            var errorMessage = "Error while trying to send event to processor. transactionId=%s"
                    .formatted(transactionId);
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public EventResponseDto updateEvent(EventUpdateRequestDto dto) {
        if (!repository.existsByTransactionId(dto.getTransactionId())) {
            throw new ValidationException("Event not found");
        }

        var event = eventMapper.updateRequestDtoToEvent(dto);

        validateEvent(event);

        var category = findActiveCategoryByName(dto.getCategoryName());

        try {
            log.info("Sending updated event to processor. transactionId={}", event.getTransactionId());

            repository.save(event);
            var eventResponseDto = eventMapper.eventToResponseDto(event, category);
            producer.sendingEventUpdatedToProcessor(eventResponseDto);

            return eventResponseDto;
        } catch (Exception e) {
            var errorMessage = "Error while trying to update event. transactionId=%s"
                    .formatted(event.getTransactionId());
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public String updatePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Image file must be provided");
        }

        log.info("Uploading photo to S3");
        return s3Service.uploadFile(file).toString();
    }

    public void findAllEvents() {
        producer.askingForSendingAllEvents();
    }

    public void sendingCreateErrorToNotification(EventResponseDto eventResponseDto) {
        var optionalSavedEvent = repository.findByTransactionId(eventResponseDto.getTransactionId());

        if (optionalSavedEvent.isPresent()) {
            var savedEvent = optionalSavedEvent.get();
            savedEvent.setActive(false);
            repository.save(savedEvent);
        }

        producer.sendingErrorCreateEventToNotification(eventResponseDto);
    }

    private Category findActiveCategoryByName(String name) {
        return categoryRepository.findByName(name)
                .filter(category -> Boolean.TRUE.equals(category.getActive()))
                .orElseThrow(() -> new ValidationException("Category not found"));
    }

    private void validateEvent(Event event) {
        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new ValidationException("Title must be informed");
        }

        if (event.getDescription() == null || event.getDescription().isBlank()) {
            throw new ValidationException("Description must be informed");
        }

        if (Objects.isNull(event.getDateStarted())) {
            throw new ValidationException("Date started of the event must be informed");
        }

        if (Objects.isNull(event.getDateFinished())) {
            throw new ValidationException("Date finished of the event must be informed");
        }

        if (!event.getDateFinished().isAfter(event.getDateStarted())) {
            throw new ValidationException("Date finished must be after date started");
        }

        if (event.getLocationName() == null || event.getLocationName().isBlank()) {
            throw new ValidationException("Location name must be informed");
        }

        if (event.getAddress() == null || event.getAddress().isBlank()) {
            throw new ValidationException("Address must be informed");
        }

        if (Objects.isNull(event.getTypeContact())) {
            throw new ValidationException("Type contact must be informed");
        }

        if (event.getContact() == null || event.getContact().isBlank()) {
            throw new ValidationException("Contact must be informed");
        }

        if (event.getImage() == null || event.getImage().isBlank()) {
            throw new ValidationException("Image must be informed");
        }

        if (event.getCategoryName() == null || event.getCategoryName().isBlank()) {
            throw new ValidationException("Category name must be informed");
        }

        if (Objects.isNull(event.getScope())) {
            throw new ValidationException("Scope must be informed");
        }

        validateScope(event);
    }

    private void validateScope(Event event) {
        switch (event.getScope()) {
            case PUBLIC -> {
            }
            case ORGANIZATION -> {
                if (event.getOrganizationId() == null) {
                    throw new ValidationException("Organization event must have organization informed");
                }
            }
            case ORGANIZATION_UNIT -> {
                if (event.getOrganizationId() == null) {
                    throw new ValidationException("Organization unit event must have organization informed");
                }
                if (event.getOrganizationUnitId() == null) {
                    throw new ValidationException("Organization unit event must have organization unit informed");
                }
            }
            default -> throw new ValidationException("Invalid scope informed");
        }
    }
}
