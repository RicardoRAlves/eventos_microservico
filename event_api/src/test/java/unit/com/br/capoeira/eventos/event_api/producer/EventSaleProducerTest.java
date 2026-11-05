package unit.com.br.capoeira.eventos.event_api.producer;

import com.br.capoeira.eventos.event_api.dto.EventSaleDeleteResponseDto;
import com.br.capoeira.eventos.event_api.dto.EventSaleItemResponseDto;
import com.br.capoeira.eventos.event_api.producer.EventSaleProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventSaleProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventSaleProducer producer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                producer,
                "exchangeCreateSaleProcessor",
                "sale.create.exchange"
        );

        ReflectionTestUtils.setField(
                producer,
                "exchangeUpdateSaleProcessor",
                "sale.update.exchange"
        );

        ReflectionTestUtils.setField(
                producer,
                "exchangeDeleteSaleProcessor",
                "sale.delete.exchange"
        );
    }

    @Test
    void shouldSendSaleCreateToProcessor() {
        var dto = getEventSaleItemResponseDto();

        producer.sendingSaleCreateToProcessor(dto);

        verify(rabbitTemplate).convertAndSend(
                "sale.create.exchange",
                "",
                dto
        );
    }

    @Test
    void shouldSendSaleUpdateToProcessor() {
        var dto = getEventSaleItemResponseDto();

        producer.sendingSaleUpdateToProcessor(dto);

        verify(rabbitTemplate).convertAndSend(
                "sale.update.exchange",
                "",
                dto
        );
    }

    @Test
    void shouldSendSaleDeleteToProcessor() {
        var dto = new EventSaleDeleteResponseDto("tx-123");

        producer.sendingSaleDeleteToProcessor(dto);

        verify(rabbitTemplate).convertAndSend(
                "sale.delete.exchange",
                "",
                dto
        );
    }

    private EventSaleItemResponseDto getEventSaleItemResponseDto() {
        var dto = new EventSaleItemResponseDto();
        dto.setTransactionId("tx-123");
        dto.setEventTransactionId("event-tx-123");
        dto.setDescription("Camiseta");
        dto.setQuantity(10);
        dto.setValue(BigDecimal.valueOf(50));
        dto.setActive(true);
        return dto;
    }
}