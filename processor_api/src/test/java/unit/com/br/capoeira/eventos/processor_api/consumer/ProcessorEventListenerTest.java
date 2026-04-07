package unit.com.br.capoeira.eventos.processor_api.consumer;

import com.br.capoeira.eventos.processor_api.consumer.ProcessorEventListener;
import com.br.capoeira.eventos.processor_api.service.ProcessorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static unit.com.br.capoeira.eventos.processor_api.service.MockUtils.getMockEvent;

@ExtendWith(MockitoExtension.class)
public class ProcessorEventListenerTest {


    @Mock
    private ProcessorService service;

    @InjectMocks
    private ProcessorEventListener listener;

    @Test
    public void shouldReceiveNewEventQueue(){
        var event = getMockEvent();
        doNothing().when(service).createNewEvent(any());

        listener.saveEvent(event);

        verify(service).createNewEvent(any());
    }

    @Test
    public void shouldReceiveGetAllQueue(){
        doNothing().when(service).findAll();

        listener.getAllEvents();

        verify(service).findAll();
    }

    @Test
    public void shouldReceiveUpdateEventQueue(){
        var event = getMockEvent();
        doNothing().when(service).updateEvent(any());

        listener.updateEvents(event);

        verify(service).updateEvent(any());
    }
}
