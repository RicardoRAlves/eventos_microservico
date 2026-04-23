package com.br.capoeira.eventos.notification.dto;

import com.br.capoeira.eventos.notification.dto.enums.EventScope;
import com.br.capoeira.eventos.notification.dto.enums.TypeContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDocument {
    private Long id;
    private String transactionId;
    private String title;
    private String description;
    private Date dateStarted;
    private Date dateFinished;
    private String locationName;
    private String address;
    private TypeContact typeContact;
    private String contact;
    private String image;
    private String categoryName;
    private EventScope scope;
    private Long organizationId;
    private Long organizationUnitId;
    private Boolean active;
}