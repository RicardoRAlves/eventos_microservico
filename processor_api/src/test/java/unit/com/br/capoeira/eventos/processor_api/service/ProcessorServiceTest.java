package unit.com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.config.exception.ValidationException;
import com.br.capoeira.eventos.processor_api.entities.Category;
import com.br.capoeira.eventos.processor_api.mapper.EventMapper;
import com.br.capoeira.eventos.processor_api.producer.ProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.CategoryRepository;
import com.br.capoeira.eventos.processor_api.repository.EventRepository;
import com.br.capoeira.eventos.processor_api.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.*;

@ExtendWith(MockitoExtension.class)
class ProcessorServiceTest {

    @Mock
    private EventRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventMapper mapper;

    @Mock
    private ProcessorProducer producer;

    @InjectMocks
    private ProcessorService service;

    @Test
    void whenFindAllShouldAddToQueue() {
        var category = getMockCategory();
        var event = getMockEvent();
        event.setCategory(category);

        var response = getMockEventResponseDto();

        when(repository.findAll()).thenReturn(List.of(event));
        when(mapper.eventToResponseDto(event)).thenReturn(response);
        doNothing().when(producer).sendAllEvents(any());

        service.findAll();

        verify(repository).findAll();
        verify(mapper).eventToResponseDto(event);
        verify(producer).sendAllEvents(List.of(response));
    }

    @Test
    void whenFindAllAndIsEmptyShouldSendEmptyListToQueue() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        doNothing().when(producer).sendAllEvents(any());

        service.findAll();

        verify(repository).findAll();
        verify(producer).sendAllEvents(List.of());
        verifyNoInteractions(mapper);
    }

    @Test
    void whenCreateNewEventShouldAddToQueue() {
        var dto = getMockEventRequestDto();
        var category = getMockCategory();
        var event = getMockEvent();
        var savedEvent = getMockEvent();
        savedEvent.setId(1L);
        savedEvent.setCategory(category);

        var response = getMockEventResponseDto();

        when(mapper.requestDtoToEvent(dto)).thenReturn(event);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName())).thenReturn(Optional.of(category));
        when(repository.save(event)).thenReturn(savedEvent);
        when(mapper.eventToResponseDto(savedEvent)).thenReturn(response);
        doNothing().when(producer).sendEventForSuccessQueue(response);

        service.createNewEvent(dto);

        verify(mapper).requestDtoToEvent(dto);
        verify(categoryRepository).findByNameIgnoreCase(dto.getCategoryName());
        verify(repository).save(event);
        verify(mapper).eventToResponseDto(savedEvent);
        verify(producer).sendEventForSuccessQueue(response);
        verify(producer, never()).sendEventForFailQueue(any());
    }

    @Test
    void whenCreateNewEventAndCategoryDoesNotExistShouldCreateCategory() {
        var dto = getMockEventRequestDto();
        var event = getMockEvent();

        var newCategory = getMockCategory();
        newCategory.setId(null);
        newCategory.setName(dto.getCategoryName());

        var savedCategory = getMockCategory();
        savedCategory.setId(10L);
        savedCategory.setName(dto.getCategoryName());

        var savedEvent = getMockEvent();
        savedEvent.setId(1L);
        savedEvent.setCategory(savedCategory);

        var response = getMockEventResponseDto();

        when(mapper.requestDtoToEvent(dto)).thenReturn(event);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName())).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(repository.save(event)).thenReturn(savedEvent);
        when(mapper.eventToResponseDto(savedEvent)).thenReturn(response);

        service.createNewEvent(dto);

        verify(categoryRepository).findByNameIgnoreCase(dto.getCategoryName());
        verify(categoryRepository).save(any(Category.class));
        verify(repository).save(event);
        verify(producer).sendEventForSuccessQueue(response);
    }

    @Test
    void whenCreateNewEventAndThrowExceptionShouldAddToErrorQueue() {
        var dto = getMockEventRequestDto();
        var category = getMockCategory();
        var event = getMockEvent();

        when(mapper.requestDtoToEvent(dto)).thenReturn(event);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName())).thenReturn(Optional.of(category));
        when(repository.save(event)).thenThrow(new RuntimeException("Database error"));

        ValidationException exception =
                assertThrows(ValidationException.class, () -> service.createNewEvent(dto));

        assertTrue(exception.getMessage().contains("Error while creating event"));

        verify(mapper).requestDtoToEvent(dto);
        verify(categoryRepository).findByNameIgnoreCase(dto.getCategoryName());
        verify(repository).save(event);
        verify(producer).sendEventForFailQueue(dto);
        verify(producer, never()).sendEventForSuccessQueue(any());
    }

    @Test
    void whenCreateCategoryConcurrentlyShouldReuseExistingCategory() {
        var dto = getMockEventRequestDto();
        var event = getMockEvent();

        var category = getMockCategory();
        category.setName(dto.getCategoryName());

        var savedEvent = getMockEvent();
        savedEvent.setId(1L);
        savedEvent.setCategory(category);

        var response = getMockEventResponseDto();

        when(mapper.requestDtoToEvent(dto)).thenReturn(event);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(repository.save(event)).thenReturn(savedEvent);
        when(mapper.eventToResponseDto(savedEvent)).thenReturn(response);

        service.createNewEvent(dto);

        verify(categoryRepository, times(2)).findByNameIgnoreCase(dto.getCategoryName());
        verify(categoryRepository).save(any(Category.class));
        verify(repository).save(event);
        verify(producer).sendEventForSuccessQueue(response);
    }

    @Test
    void shouldSendToUpdateQueueWhenEventExists() {
        var dto = getMockEventRequestDto();
        dto.setTransactionId("ahahs2#aas112");

        var savedEvent = getMockEvent();
        savedEvent.setId(1L);
        savedEvent.setTransactionId(dto.getTransactionId());

        var category = getMockCategory();
        category.setName(dto.getCategoryName());

        var updatedEvent = getMockEvent();
        updatedEvent.setId(1L);
        updatedEvent.setTransactionId(dto.getTransactionId());
        updatedEvent.setCategory(category);

        var response = getMockEventResponseDto();

        when(repository.findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId()))
                .thenReturn(Optional.of(savedEvent));
        doNothing().when(mapper).updateEventFromDto(dto, savedEvent);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName())).thenReturn(Optional.of(category));
        when(repository.save(savedEvent)).thenReturn(updatedEvent);
        when(mapper.eventToResponseDto(updatedEvent)).thenReturn(response);
        doNothing().when(producer).sendEventForUpdateQueue(response);

        service.updateEvent(dto);

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId());
        verify(mapper).updateEventFromDto(dto, savedEvent);
        verify(categoryRepository).findByNameIgnoreCase(dto.getCategoryName());
        verify(repository).save(savedEvent);
        verify(producer).sendEventForUpdateQueue(response);
        verify(producer, never()).sendEventForUpdateErrorQueue(any());
    }

    @Test
    void shouldSendToErrorQueueWhenEventNotFound() {
        var dto = getMockEventRequestDto();
        dto.setTransactionId("ahahs2#aas112");

        when(repository.findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId()))
                .thenReturn(Optional.empty());

        ValidationException exception =
                assertThrows(ValidationException.class, () -> service.updateEvent(dto));

        assertTrue(exception.getMessage().contains("Event not found for transactionId"));

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId());
        verify(repository, never()).save(any());
        verify(producer).sendEventForUpdateErrorQueue(dto);
        verify(producer, never()).sendEventForUpdateQueue(any());
    }

    @Test
    void shouldSendToErrorQueueWhenUpdateFails() {
        var dto = getMockEventRequestDto();
        dto.setTransactionId("ahahs2#aas112");

        var savedEvent = getMockEvent();
        savedEvent.setId(1L);
        savedEvent.setTransactionId(dto.getTransactionId());

        var category = getMockCategory();
        category.setName(dto.getCategoryName());

        var updatedEvent = getMockEvent();
        updatedEvent.setId(1L);
        updatedEvent.setTransactionId(dto.getTransactionId());
        updatedEvent.setCategory(category);

        var response = getMockEventResponseDto();

        when(repository.findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId()))
                .thenReturn(Optional.of(savedEvent));
        doNothing().when(mapper).updateEventFromDto(dto, savedEvent);
        when(categoryRepository.findByNameIgnoreCase(dto.getCategoryName())).thenReturn(Optional.of(category));
        when(repository.save(savedEvent)).thenReturn(updatedEvent);
        when(mapper.eventToResponseDto(updatedEvent)).thenReturn(response);
        doThrow(new RuntimeException("Error on Queue")).when(producer).sendEventForUpdateQueue(response);

        ValidationException exception =
                assertThrows(ValidationException.class, () -> service.updateEvent(dto));

        assertTrue(exception.getMessage().contains("Error while updating event"));

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(dto.getTransactionId());
        verify(repository).save(savedEvent);
        verify(producer).sendEventForUpdateQueue(response);
        verify(producer).sendEventForUpdateErrorQueue(dto);
    }
}
