package unit.com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.dto.*;
import com.br.capoeira.eventos.event_api.mapper.EventSaleMapper;
import com.br.capoeira.eventos.event_api.model.EventSaleItem;
import com.br.capoeira.eventos.event_api.producer.EventSaleProducer;
import com.br.capoeira.eventos.event_api.repository.EventSaleItemRepository;
import com.br.capoeira.eventos.event_api.service.EventSaleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSaleServiceTest {

    @Mock
    private EventSaleMapper mapper;

    @Mock
    private EventSaleItemRepository repository;

    @Mock
    private EventSaleProducer producer;

    @InjectMocks
    private EventSaleService service;

    @Test
    void shouldCreateItemSaleWithSuccess() {
        var request = createRequest();
        var entity = new EventSaleItem();
        var response = responseDto("tx-123");

        when(repository.existsByTransactionId(anyString()))
                .thenReturn(false);

        when(mapper.createRequestDtoToEventSale(request))
                .thenReturn(entity);

        when(mapper.eventSaleToResponseDto(entity))
                .thenReturn(response);

        var result = service.createItemSale(request);

        assertNotNull(result);
        assertEquals(response, result);
        assertTrue(entity.getActive());
        assertNotNull(entity.getTransactionId());

        verify(repository).save(entity);
        verify(producer).sendingSaleCreateToProcessor(response);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateRequestIsNull() {
        assertThrows(
                ValidationException.class,
                () -> service.createItemSale(null)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateDescriptionIsBlank() {
        var request = createRequest();
        request.setDescription("");

        assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateQuantityIsNegative() {
        var request = createRequest();
        request.setQuantity(-1);

        assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateValueIsNegative() {
        var request = createRequest();
        request.setValue(BigDecimal.valueOf(-10));

        assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnCreate() {
        var request = createRequest();
        var entity = new EventSaleItem();

        when(repository.existsByTransactionId(anyString()))
                .thenReturn(false);

        when(mapper.createRequestDtoToEventSale(request))
                .thenReturn(entity);

        when(repository.save(entity))
                .thenThrow(new RuntimeException("database error"));

        assertThrows(
                RuntimeException.class,
                () -> service.createItemSale(request)
        );

        verify(repository).save(entity);
        verify(producer, never()).sendingSaleCreateToProcessor(any());
    }

    @Test
    void shouldUpdateItemSaleWithSuccess() {
        var request = updateRequest();
        var entity = eventSaleItem("tx-123");
        var response = responseDto("tx-123");

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        when(mapper.eventSaleToResponseDto(entity))
                .thenReturn(response);

        var result = service.updateItemSale(request);

        assertNotNull(result);
        assertEquals(response, result);

        verify(mapper).updateRequestDtoToEventSale(request, entity);
        verify(repository).save(entity);
        verify(producer).sendingSaleUpdateToProcessor(response);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateRequestIsNull() {
        assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(null)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenEventSaleToUpdateWasNotFound() {
        var request = updateRequest();

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.empty());

        assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        verify(repository).findByTransactionId("tx-123");
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnUpdate() {
        var request = updateRequest();
        var entity = eventSaleItem("tx-123");

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        when(repository.save(entity))
                .thenThrow(new RuntimeException("database error"));

        assertThrows(
                RuntimeException.class,
                () -> service.updateItemSale(request)
        );

        verify(repository).save(entity);
        verify(producer, never()).sendingSaleUpdateToProcessor(any());
    }

    @Test
    void shouldReturnWithoutErrorWhenDeleteItemSaleWasNotFound() {
        var request = deleteRequest();

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.empty());

        service.deleteItemSale(request);

        verify(repository).findByTransactionId("tx-123");
        verify(repository, never()).save(any());
        verifyNoInteractions(producer);
    }

    @Test
    void shouldDeleteItemSaleWithSuccessWhenItemIsActive() {
        var request = deleteRequest();
        var entity = eventSaleItem("tx-123");
        entity.setActive(true);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        service.deleteItemSale(request);

        assertFalse(entity.getActive());

        verify(repository).save(entity);
        verify(producer).sendingSaleDeleteToProcessor(any(EventSaleDeleteResponseDto.class));
    }

    @Test
    void shouldSendDeleteMessageWithoutSavingWhenItemIsAlreadyInactive() {
        var request = deleteRequest();
        var entity = eventSaleItem("tx-123");
        entity.setActive(false);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        service.deleteItemSale(request);

        verify(repository, never()).save(any());
        verify(producer).sendingSaleDeleteToProcessor(any(EventSaleDeleteResponseDto.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenDeleteRequestIsNull() {
        assertThrows(
                ValidationException.class,
                () -> service.deleteItemSale(null)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenDeleteTransactionIdIsBlank() {
        var request = deleteRequest();
        request.setTransactionId("");

        assertThrows(
                ValidationException.class,
                () -> service.deleteItemSale(request)
        );

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateEventTransactionIdIsBlank() {
        var request = createRequest();
        request.setEventTransactionId("");

        var exception = assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        assertEquals("Event transaction id must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateDescriptionHasMoreThan150Characters() {
        var request = createRequest();
        request.setDescription("a".repeat(151));

        var exception = assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        assertEquals("Description must have a maximum of 150 characters", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateQuantityIsNull() {
        var request = createRequest();
        request.setQuantity(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        assertEquals("Quantity must be zero or greater", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreateValueIsNull() {
        var request = createRequest();
        request.setValue(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        assertEquals("Value must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenGeneratedTransactionIdAlreadyExists() {
        var request = createRequest();

        when(repository.existsByTransactionId(anyString()))
                .thenReturn(true);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.createItemSale(request)
        );

        assertEquals("Transaction id already exists", exception.getMessage());

        verify(repository).existsByTransactionId(anyString());
        verifyNoInteractions(mapper, producer);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateTransactionIdIsBlank() {
        var request = updateRequest();
        request.setTransactionId("");

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Transaction id must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateEventTransactionIdIsBlank() {
        var request = updateRequest();
        request.setEventTransactionId("");

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Event transaction id must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateDescriptionIsBlank() {
        var request = updateRequest();
        request.setDescription("");

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Description must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateDescriptionHasMoreThan150Characters() {
        var request = updateRequest();
        request.setDescription("a".repeat(151));

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Description must have a maximum of 150 characters", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateQuantityIsNull() {
        var request = updateRequest();
        request.setQuantity(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Quantity must be zero or greater", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateQuantityIsNegative() {
        var request = updateRequest();
        request.setQuantity(-1);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Quantity must be zero or greater", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateValueIsNull() {
        var request = updateRequest();
        request.setValue(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Value must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateValueIsNegative() {
        var request = updateRequest();
        request.setValue(BigDecimal.valueOf(-1));

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Value must be zero or greater", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowValidationExceptionWhenUpdateActiveIsNull() {
        var request = updateRequest();
        request.setActive(null);

        var exception = assertThrows(
                ValidationException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals("Active must be informed", exception.getMessage());

        verifyNoInteractions(repository, mapper, producer);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenProducerFailsOnCreate() {
        var request = createRequest();
        var entity = new EventSaleItem();
        var response = responseDto("tx-123");

        when(repository.existsByTransactionId(anyString()))
                .thenReturn(false);

        when(mapper.createRequestDtoToEventSale(request))
                .thenReturn(entity);

        when(mapper.eventSaleToResponseDto(entity))
                .thenReturn(response);

        doThrow(new RuntimeException("rabbit error"))
                .when(producer)
                .sendingSaleCreateToProcessor(response);

        var exception = assertThrows(
                RuntimeException.class,
                () -> service.createItemSale(request)
        );

        assertTrue(exception.getMessage().contains("Error while trying to create event sale item. transactionId="));

        verify(repository).save(entity);
        verify(producer).sendingSaleCreateToProcessor(response);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenProducerFailsOnUpdate() {
        var request = updateRequest();
        var entity = eventSaleItem("tx-123");
        var response = responseDto("tx-123");

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        when(mapper.eventSaleToResponseDto(entity))
                .thenReturn(response);

        doThrow(new RuntimeException("rabbit error"))
                .when(producer)
                .sendingSaleUpdateToProcessor(response);

        var exception = assertThrows(
                RuntimeException.class,
                () -> service.updateItemSale(request)
        );

        assertEquals(
                "Error while trying to update event sale. transactionId=tx-123",
                exception.getMessage()
        );

        verify(repository).save(entity);
        verify(producer).sendingSaleUpdateToProcessor(response);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryFailsOnDelete() {
        var request = deleteRequest();
        var entity = eventSaleItem("tx-123");
        entity.setActive(true);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        when(repository.save(entity))
                .thenThrow(new RuntimeException("database error"));

        var exception = assertThrows(
                RuntimeException.class,
                () -> service.deleteItemSale(request)
        );

        assertEquals(
                "Error while trying to delete event sale item. transactionId=tx-123",
                exception.getMessage()
        );

        verify(repository).save(entity);
        verify(producer, never()).sendingSaleDeleteToProcessor(any());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenProducerFailsOnDelete() {
        var request = deleteRequest();
        var entity = eventSaleItem("tx-123");
        entity.setActive(false);

        when(repository.findByTransactionId("tx-123"))
                .thenReturn(Optional.of(entity));

        doThrow(new RuntimeException("rabbit error"))
                .when(producer)
                .sendingSaleDeleteToProcessor(any(EventSaleDeleteResponseDto.class));

        var exception = assertThrows(
                RuntimeException.class,
                () -> service.deleteItemSale(request)
        );

        assertEquals(
                "Error while trying to delete event sale item. transactionId=tx-123",
                exception.getMessage()
        );

        verify(repository, never()).save(any());
        verify(producer).sendingSaleDeleteToProcessor(any(EventSaleDeleteResponseDto.class));
    }



    private EventSaleItemCreateRequestDto createRequest() {
        var dto = new EventSaleItemCreateRequestDto();
        dto.setEventTransactionId("event-tx-123");
        dto.setDescription("Camiseta do evento");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        return dto;
    }

    private EventSaleItemUpdateRequestDto updateRequest() {
        var dto = new EventSaleItemUpdateRequestDto();
        dto.setTransactionId("tx-123");
        dto.setEventTransactionId("event-tx-123");
        dto.setDescription("Camiseta atualizada");
        dto.setQuantity(5);
        dto.setValue(BigDecimal.valueOf(60));
        dto.setActive(true);
        return dto;
    }

    private EventSaleItemDeleteRequestDto deleteRequest() {
        var dto = new EventSaleItemDeleteRequestDto();
        dto.setTransactionId("tx-123");
        return dto;
    }

    private EventSaleItem eventSaleItem(String transactionId) {
        var entity = new EventSaleItem();
        entity.setTransactionId(transactionId);
        entity.setEventTransactionId("event-tx-123");
        entity.setDescription("Camiseta do evento");
        entity.setQuantity(10);
        entity.setValue(BigDecimal.valueOf(50));
        entity.setActive(true);
        return entity;
    }

    private EventSaleItemResponseDto responseDto(String transactionId) {
        var dto = new EventSaleItemResponseDto();
        dto.setTransactionId(transactionId);
        dto.setEventTransactionId("event-tx-123");
        dto.setDescription("Camiseta do evento");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        dto.setActive(true);
        return dto;
    }
}
