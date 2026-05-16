package com.br.capoeira.eventos.notification.service;

import com.br.capoeira.eventos.notification.dto.*;
import com.br.capoeira.eventos.notification.dto.enums.Actions;
import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class NotificationEventService {

    private final FirebaseEventService firebaseEventService;

    public void createNewEvent(EventRequestDto event) {
        log.info("Notify android that event: {} has been created", event);
        firebaseEventService.addEvent(event);
        firebaseEventService.sendEventNotification(event, Actions.CREATE, resolveTopic(event));
    }

    public void updateEvent(EventRequestDto event) {
        log.info("Notify android that event: {} has been updated", event);
        firebaseEventService.updateEvent(event);
        firebaseEventService.sendEventNotification(event, Actions.UPDATE, resolveTopic(event));
    }

    public void createErrorEvent(EventErrorDto event) {
        log.info("Notify android that event: {} was not created successfully, please try again", event);
        firebaseEventService.sendEventNotification(event, Actions.ERROR_CREATE, resolveTopic(event));
    }

    public void updateErrorEvent(EventErrorDto event) {
        log.info("Notify android that event: {} was not updated successfully, please try again", event);
        firebaseEventService.sendEventNotification(event, Actions.ERROR_UPDATE, resolveTopic(event));
    }

    public void getAllEvents(List<EventRequestDto> events) {
        log.info("Notify android full sync for {} events", events.size());

        firebaseEventService.addMultipleEventsBatch(events);

        sendPublicSync(events);
        sendOrganizationSync(events);
        sendUnitSync(events);
    }

    private void sendPublicSync(List<EventRequestDto> events) {
        List<EventRequestDto> publicEvents = events.stream()
                .filter(event -> event.getScope() == EventScope.PUBLIC)
                .toList();

        if (publicEvents.isEmpty()) {
            return;
        }

        EventSyncDto payload = EventSyncDto.builder()
                .scope(EventScope.PUBLIC)
                .events(publicEvents)
                .build();

        firebaseEventService.sendEventNotification(payload, Actions.GET_ALL, "public");
    }

    private void sendOrganizationSync(List<EventRequestDto> events) {
        Map<Long, List<EventRequestDto>> eventsByOrganization = events.stream()
                .filter(event -> event.getScope() == EventScope.ORGANIZATION)
                .filter(event -> event.getOrganizationId() != null)
                .collect(Collectors.groupingBy(EventRequestDto::getOrganizationId));

        eventsByOrganization.forEach((organizationId, organizationEvents) -> {
            EventSyncDto payload = EventSyncDto.builder()
                    .scope(EventScope.ORGANIZATION)
                    .organizationId(organizationId)
                    .events(organizationEvents)
                    .build();

            firebaseEventService.sendEventNotification(
                    payload,
                    Actions.GET_ALL,
                    "org_" + organizationId
            );
        });
    }

    private void sendUnitSync(List<EventRequestDto> events) {
        Map<Long, List<EventRequestDto>> eventsByUnit = events.stream()
                .filter(event -> event.getScope() == EventScope.ORGANIZATION_UNIT)
                .filter(event -> event.getOrganizationUnitId() != null)
                .collect(Collectors.groupingBy(EventRequestDto::getOrganizationUnitId));

        eventsByUnit.forEach((unitId, unitEvents) -> {
            EventSyncDto payload = EventSyncDto.builder()
                    .scope(EventScope.ORGANIZATION_UNIT)
                    .organizationUnitId(unitId)
                    .events(unitEvents)
                    .build();

            firebaseEventService.sendEventNotification(
                    payload,
                    Actions.GET_ALL,
                    "unit_" + unitId
            );
        });
    }

    private String resolveTopic(EventTopicPayload event) {
        return switch (event.getScope()) {
            case PUBLIC -> "public";
            case ORGANIZATION -> "org_" + event.getOrganizationId();
            case ORGANIZATION_UNIT -> "unit_" + event.getOrganizationUnitId();
        };
    }
}
