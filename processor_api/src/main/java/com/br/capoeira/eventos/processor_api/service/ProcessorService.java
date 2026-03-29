package com.br.capoeira.eventos.processor_api.service;

import com.br.capoeira.eventos.processor_api.entities.Event;
import com.br.capoeira.eventos.processor_api.producer.ProcessorProducer;
import com.br.capoeira.eventos.processor_api.repository.EventoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class ProcessorService {

    private final EventoRepository eventoRepository;
    private final ProcessorProducer producer;

    public void findAll(){
        log.info("Getting all events");
        var events = eventoRepository.findAll();
        if (!events.isEmpty()){
            producer.sendAllEvents(events);
        }
    }

    public void createNewEvent(Event event) {
        log.info("creating new event");
        try{
            var newEvent = eventoRepository.save(event);
            producer.sendEventForSuccessQueue(newEvent);
        } catch (Exception e) {
            producer.sendEventForFailQueue(event);
            throw new RuntimeException("An error happened when it was trying to save ".concat(e.getMessage()));
        }

    }

    public void updateEvent(Event event) {
        log.info("update event: {}", event.getId());
        try{
            var optSavedEvent = eventoRepository.findTopByTransactionIdOrderByCreateAtDesc(event.getTransactionId());
            if (optSavedEvent.isPresent()){
                event.setId(optSavedEvent.get().getId());
                eventoRepository.save(event);
                producer.sendEventForUpdateQueue(event);
            } else {
                producer.sendEventForUpdateErrorQueue(event);
                throw new RuntimeException("An error happened \n Event not found: ".concat(event.getTransactionId()));
            }

        } catch (Exception e) {
            producer.sendEventForUpdateErrorQueue(event);
            throw new RuntimeException("An error happened when it was trying to updating event: ".concat(e.getMessage()));
        }
    }
}
