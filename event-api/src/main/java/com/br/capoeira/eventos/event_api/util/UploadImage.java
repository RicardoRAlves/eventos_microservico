package com.br.capoeira.eventos.event_api.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadImage {

    // TODO Corrigir o erro aqui, não está salvando
    public static String fazendoUpload(MultipartFile file)  {
        try {
            String workspaceProjeto = "C:\\Users\\Ricardo\\Desktop\\Projetos\\evento-api\\src\\main\\resources\\static\\images\\";
            File dir = new File(workspaceProjeto);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String pathName = workspaceProjeto + file.getOriginalFilename();
            Path path = Files.write(Paths.get(pathName), file.getBytes());
            return path.toString();
        } catch (IOException e) {
            System. out .println(e.getMessage());
            return "";
        }
    }
}
