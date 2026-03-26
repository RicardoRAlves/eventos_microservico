package com.br.capoeira.eventos.banco_api.entities;

import com.br.capoeira.eventos.banco_api.entities.enums.TipoContato;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Integer orderId;
    private String titulo;
    private String descricao;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
    private String endereco;
    @Enumerated(EnumType.STRING)
    private TipoContato tipoContato;
    private String contato;
    private String imagem;
}