package com.br.capoeira.eventos.event_api.service;

import com.br.capoeira.eventos.event_api.config.exception.ValidationException;
import com.br.capoeira.eventos.event_api.model.Event;
import com.br.capoeira.eventos.event_api.producer.EventProducer;
import com.br.capoeira.eventos.event_api.repository.EventRepository;
import com.br.capoeira.eventos.event_api.service.aws.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {
    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final EventProducer producer;
    private final EventRepository repository;
    private final S3Service s3Service;

    public void sendingNewEventToProcessor(Event event) {
        try {
            var id = format(String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID()));
            checkEvent(event);
            event.setTransactionId(id);
            event.setActive(true);
            log.info("sending event {} for database api ", event);
            repository.save(event);
            producer.sendingNewEventToProcessor(event);
        } catch (Exception e) {
            log.error("Error while trying to sending event {} to database api {} ", event, e.getMessage());
            throw new RuntimeException("Error while trying to sending event to database api");
        }
    }

    public String updatePhoto(MultipartFile file){
        log.info("uploading foto to S3 ");
        return s3Service.uploadFile(file).toString();
    }

    public void findAllEvents(){
        producer.askingForSendingAllEvents();
    }

    public void updateEvent(Event event){
        if(!repository.existsByTransactionId(event.getTransactionId())){
            throw new ValidationException("Event not found");
        }
        checkEvent(event);
        repository.save(event);
        producer.sendingEventUpdatedToProcessor(event);
    }
    public void sendingCreateErrorToNotification(Event event){
        var optionalSavedEvent = repository.findByTransactionId(event.getTransactionId());
        if (optionalSavedEvent.isPresent()){
            var savedEvent = optionalSavedEvent.get();
            savedEvent.setActive(false);
            repository.save(savedEvent);
        }
        producer.sendingErrorCreateEventToNotification(event);
    }

    private void checkEvent(Event event){
        if (isEmpty(event.getTitle())){
            throw new ValidationException("Title must be informed");
        }
        if (isEmpty(event.getDescription())){
            throw new ValidationException("Description must be informed");
        }
        if (isEmpty(event.getDateStarted())){
            throw new ValidationException("Date of started of the Event must be informed");
        }
        if (isEmpty(event.getDateFinished())){
            throw new ValidationException("Date of finish of the Event must be informed");
        }
        if (event.getDateFinished().isBefore(event.getDateStarted())){
            throw new ValidationException("Date of finish must be after the date of started");
        }
        if (isEmpty(event.getAddress())){
            throw new ValidationException("Address must be informed");
        }
        if (isEmpty(event.getImage())){
            throw new ValidationException("Image must be informed");
        }
    }
}
