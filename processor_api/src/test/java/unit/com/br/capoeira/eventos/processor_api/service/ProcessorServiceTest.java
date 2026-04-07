package unit.com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.entities.Event;
import com.br.capoeira.eventos.processor_api.producer.ProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.EventoRepository;
import com.br.capoeira.eventos.processor_api.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class ProcessorServiceTest {

    @Mock
    private EventoRepository repository;

    @Mock
    private ProcessorProducer producer;

    @InjectMocks
    private ProcessorService service;

    @Test
    public void whenFindAllShouldAddToQueue() {
        var events = List.of(getMockEvent());
        when(repository.findAll()).thenReturn(events);
        doNothing().when(producer).sendAllEvents(any());

        service.findAll();

        verify(repository).findAll();
        verify(producer).sendAllEvents(any());
    }

    @Test
    public void whenFindAllAndIsEmptyShouldNotAddToQueue() {
        var events = new ArrayList<Event>();
        when(repository.findAll()).thenReturn(events);

        service.findAll();

        verify(repository).findAll();
        verifyNoInteractions(producer);
    }

    @Test
    public void whenCreateNewEventShouldAddToQueue() {
        var event = getMockEvent();
        when(repository.save(any())).thenReturn(event);
        doNothing().when(producer).sendEventForSuccessQueue(any());

        service.createNewEvent(event);

        verify(repository).save(any());
        verify(producer).sendEventForSuccessQueue(any());
    }

    @Test
    public void whenCreateNewEventAndThrowExceptionShouldAddToErrorQueue() {
        var event = getMockEvent();
        when(repository.save(any())).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.createNewEvent(event));

        assertTrue(exception.getMessage().contains("An error happened when it was trying to save"));

        verify(repository).save(any());
        verify(producer).sendEventForFailQueue(any());
        verify(producer, never()).sendEventForSuccessQueue(any());
    }

    @Test
    public void shouldSendToUpdateQueueWhenEventExists() {
        var event = getMockEvent();
        event.setId(1L);
        event.setTransactionId("ahahs2#aas112");
        when(repository.findTopByTransactionIdOrderByCreateAtDesc(anyString())).thenReturn(Optional.of(event));
        when(repository.save(any())).thenReturn(event);
        doNothing().when(producer).sendEventForUpdateQueue(any());

        service.updateEvent(event);

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(anyString());
        verify(repository).save(any());
        verify(producer).sendEventForUpdateQueue(any());
    }

    @Test
    public void shouldSendToErrorQueueWhenEventNotFound() {
        var event = getMockEvent();
        event.setId(1L);
        event.setTransactionId("ahahs2#aas112");
        when(repository.findTopByTransactionIdOrderByCreateAtDesc(anyString())).thenReturn(Optional.empty());
        doNothing().when(producer).sendEventForUpdateErrorQueue(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateEvent(event));

        assertTrue(exception.getMessage().contains("Event not found:"));

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(anyString());
        verify(repository, never()).save(any());
        verify(producer).sendEventForUpdateErrorQueue(any());
        verify(producer, never()).sendEventForUpdateQueue(any());
    }

    @Test
    public void shouldSendToErrorQueueWhenUpdateFails() {
        var event = getMockEvent();
        event.setId(1L);
        event.setTransactionId("ahahs2#aas112");
        when(repository.findTopByTransactionIdOrderByCreateAtDesc(anyString())).thenReturn(Optional.of(event));
        doThrow(new RuntimeException("Error on Queue")).when(producer).sendEventForUpdateQueue(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.updateEvent(event));

        assertTrue(exception.getMessage().contains("An error happened when it was trying to updating event:"));

        verify(repository).findTopByTransactionIdOrderByCreateAtDesc(anyString());
        verify(repository).save(any());
        verify(producer).sendEventForUpdateErrorQueue(any());
        verify(producer).sendEventForUpdateQueue(any());
    }
}
