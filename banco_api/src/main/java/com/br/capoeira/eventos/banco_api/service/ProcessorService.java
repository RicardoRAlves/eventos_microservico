package com.br.capoeira.eventos.banco_api.service;

import com.br.capoeira.eventos.banco_api.entities.Event;
import com.br.capoeira.eventos.banco_api.producer.ProcessorProducer;
import com.br.capoeira.eventos.banco_api.repository.EventoRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
