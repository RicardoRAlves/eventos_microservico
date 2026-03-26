package com.br.capoeira.eventos.evento_api.service;

import com.br.capoeira.eventos.evento_api.model.Event;
import com.br.capoeira.eventos.evento_api.producer.EventProducer;
import com.br.capoeira.eventos.evento_api.service.aws.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventProducer producer;
    private final S3Service s3Service;

    public void sendingNewEventToProcessor(Event event) {
        try {
            log.info("sending event {} for database api ", event);
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
}
