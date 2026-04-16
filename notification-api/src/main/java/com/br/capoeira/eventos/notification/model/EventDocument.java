package com.br.capoeira.eventos.notification.model;

import com.br.capoeira.eventos.notification.model.enums.TypeContact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    private Boolean active;
}