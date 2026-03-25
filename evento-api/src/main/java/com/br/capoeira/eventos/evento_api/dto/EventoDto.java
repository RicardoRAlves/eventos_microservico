package com.br.capoeira.eventos.evento_api.dto;

import com.br.capoeira.eventos.evento_api.enums.TipoContato;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventoDto {
    private String titulo;
    private String descricao;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dataHora;
    private String endereco;
    private TipoContato tipoContato;
    private String contato;
    private String imagem;
}
