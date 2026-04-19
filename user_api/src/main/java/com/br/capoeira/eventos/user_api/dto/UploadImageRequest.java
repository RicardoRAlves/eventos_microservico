package com.br.capoeira.eventos.user_api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class UploadImageRequest {

    @Schema(type = "string", format = "binary")
    private MultipartFile image;

}
