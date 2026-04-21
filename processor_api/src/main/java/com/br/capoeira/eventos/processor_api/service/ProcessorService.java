package com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.config.exception.ValidationException;
import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.entities.Category;
import com.br.capoeira.eventos.processor_api.entities.Event;
import com.br.capoeira.eventos.processor_api.mapper.EventMapper;
import com.br.capoeira.eventos.processor_api.producer.ProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.CategoryRepository;
import com.br.capoeira.eventos.processor_api.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessorService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper mapper;
    private final ProcessorProducer producer;

    public void findAll() {
        log.info("Finding all events");

        var events = eventRepository.findAll()
                .stream()
                .map(mapper::eventToResponseDto)
                .toList();

        producer.sendAllEvents(events);
    }

    @Transactional
    public void createNewEvent(EventRequestDto dto) {
        log.info("Creating new event. transactionId={}", dto.getTransactionId());

        try {
            var event = mapper.requestDtoToEvent(dto);

            var category = findOrCreateCategory(dto.getCategoryName());
            event.setCategory(category);

            validateScope(event);

            var savedEvent = eventRepository.save(event);
            var responseDto = mapper.eventToResponseDto(savedEvent);

            producer.sendEventForSuccessQueue(responseDto);

            log.info("Event created successfully. id={}, transactionId={}",
                    savedEvent.getId(), savedEvent.getTransactionId());

        } catch (ValidationException e) {
            log.error("Validation error while creating event. transactionId={}", dto.getTransactionId(), e);
            producer.sendEventForFailQueue(dto);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating event. transactionId={}", dto.getTransactionId(), e);
            producer.sendEventForFailQueue(dto);
            throw new ValidationException("Error while creating event: " + e.getMessage());
        }
    }

    @Transactional
    public void updateEvent(EventRequestDto dto) {
        log.info("Updating event. transactionId={}", dto.getTransactionId());

        try {
            var savedEvent = findByTransactionId(dto.getTransactionId());

            mapper.updateEventFromDto(dto, savedEvent);

            var category = findOrCreateCategory(dto.getCategoryName());
            savedEvent.setCategory(category);

            validateScope(savedEvent);

            var updatedEvent = eventRepository.save(savedEvent);
            var responseDto = mapper.eventToResponseDto(updatedEvent);

            producer.sendEventForUpdateQueue(responseDto);

            log.info("Event updated successfully. id={}, transactionId={}",
                    updatedEvent.getId(), updatedEvent.getTransactionId());

        } catch (ValidationException e) {
            log.error("Validation error while updating event. transactionId={}", dto.getTransactionId(), e);
            producer.sendEventForUpdateErrorQueue(dto);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while updating event. transactionId={}", dto.getTransactionId(), e);
            producer.sendEventForUpdateErrorQueue(dto);
            throw new ValidationException("Error while updating event: " + e.getMessage());
        }
    }

    private Event findByTransactionId(String transactionId) {
        return eventRepository.findTopByTransactionIdOrderByCreateAtDesc(transactionId)
                .orElseThrow(() ->
                        new ValidationException("Event not found for transactionId: " + transactionId));
    }

    private Category findOrCreateCategory(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) {
            throw new ValidationException("Category name must be informed");
        }

        var normalizedName = normalizeCategoryName(categoryName);

        return categoryRepository.findByNameIgnoreCase(normalizedName)
                .orElseGet(() -> createCategorySafely(normalizedName));
    }

    private Category createCategorySafely(String normalizedName) {
        try {
            log.info("Category not found. Creating category. name={}", normalizedName);

            var category = new Category();
            category.setName(normalizedName);
            category.setActive(true);

            return categoryRepository.save(category);

        } catch (DataIntegrityViolationException e) {
            log.warn("Category already created concurrently. name={}", normalizedName);

            return categoryRepository.findByNameIgnoreCase(normalizedName)
                    .orElseThrow(() -> new ValidationException(
                            "Error while retrieving existing category: " + normalizedName));
        }
    }

    private String normalizeCategoryName(String categoryName) {
        return categoryName.trim().replaceAll("\\s+", " ");
    }

    private void validateScope(Event event) {
        if (event.getOrganizationUnitId() != null && event.getOrganizationId() == null) {
            throw new ValidationException("organizationUnitId requires organizationId");
        }

        switch (event.getScope()) {
            case PUBLIC -> {
                // público pode ou não ter vínculo com organização/unidade
            }

            case ORGANIZATION -> {
                if (event.getOrganizationId() == null) {
                    throw new ValidationException("ORGANIZATION events must have organizationId");
                }
            }

            case ORGANIZATION_UNIT -> {
                if (event.getOrganizationId() == null) {
                    throw new ValidationException("ORGANIZATION_UNIT events must have organizationId");
                }

                if (event.getOrganizationUnitId() == null) {
                    throw new ValidationException("ORGANIZATION_UNIT events must have organizationUnitId");
                }
            }
        }
    }
}