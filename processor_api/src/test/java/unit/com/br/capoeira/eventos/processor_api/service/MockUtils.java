package unit.com.br.capoeira.eventos.processor_api.service;


import com.br.capoeira.eventos.processor_api.dto.EventRequestDto;
import com.br.capoeira.eventos.processor_api.dto.EventResponseDto;
import com.br.capoeira.eventos.processor_api.entities.Category;
import com.br.capoeira.eventos.processor_api.entities.Event;
import com.br.capoeira.eventos.processor_api.entities.enums.EventScope;
import com.br.capoeira.eventos.processor_api.entities.enums.TypeContact;

import java.time.LocalDateTime;

public class MockUtils {

    public static Category getMockCategory(){
        var category = new Category();
        category.setId(1L);
        category.setName("Capoeira");
        category.setActive(true);
        return category;
    }

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
        event.setCategory(new Category());
        event.setScope(EventScope.PUBLIC);
        event.setActive(true);
        return event;
    }

    public static EventResponseDto getMockEventResponseDto(){
        var event = new EventResponseDto();
        event.setId(1L);
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
        event.setCategoryName("Capoeira");
        event.setScope(EventScope.PUBLIC);
        return event;
    }

    public static EventRequestDto getMockEventRequestDto(){
        var event = new EventRequestDto();
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
        event.setCategoryName("Capoeira");
        event.setScope(EventScope.PUBLIC);
        return event;
    }
}
