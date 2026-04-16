package unit.com.br.capoeira.eventos.event_api.utils;

import com.br.capoeira.eventos.event_api.enums.TypeContact;
import com.br.capoeira.eventos.event_api.model.Event;

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
        event.setLocationName("Cultural Center");
        event.setAddress("Event Street");
        event.setImage("https://my-bucket.s3.amazonaws.com/photo.jpg");
        event.setActive(true);
        return event;
    }
}
