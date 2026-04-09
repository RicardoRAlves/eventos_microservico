package unit.com.br.capoeira.eventos.notification.utils;


import com.br.capoeira.eventos.notification.model.Event;
import com.br.capoeira.eventos.notification.model.enums.TypeContact;

import java.time.LocalDateTime;

public class MockUtils {

    public static Event getMockEvent(){
        var event = new Event();
        event.setTitle("Title");
        event.setDescription("Event Description");
        event.setContact("@instagram");
        event.setTypeContact(TypeContact.INSTAGRAM);
        event.setDateStarted(LocalDateTime.now().minusDays(1));
        event.setDateFinished(LocalDateTime.now());
        event.setAddress("Event Street");
        event.setImage("https://my-bucket.s3.amazonaws.com/photo.jpg");
        event.setActive(true);
        return event;
    }
}
