package com.br.capoeira.eventos.evento_api.service;

import com.br.capoeira.eventos.evento_api.model.Event;
import com.br.capoeira.eventos.evento_api.producer.EventProducer;
import com.br.capoeira.eventos.evento_api.util.UploadImage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventProducer producer;

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

        return "";
    }

    /*
    public Event atualizarFoto(MultipartFile file, Long id){
        try {
            Event event = bancoApiService.buscarEventoPorId(id);
            String pathImage = UploadImage.fazendoUpload(file);
            event.setImagem(pathImage);
            bancoApiService.criarEvento(event);
            return event;
        } catch (Exception e) {
            logger.error("Ocorreu um erro ao tentar atualizar a foto para o id {}, \n mensagem original: {} ", id, e.getMessage());
            return null;
        }
    }

     */

    public void findAllEvents(){
        producer.askingForSendingAllEvents();
    }
}
