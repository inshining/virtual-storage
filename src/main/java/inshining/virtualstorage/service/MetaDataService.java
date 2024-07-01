package inshining.virtualstorage.service;

import inshining.virtualstorage.model.MetaData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class MetaDataService {

    private static final String UPLOAD_DIR = "upload/";

    public String uploadFile(MultipartFile file, String username){

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOAD_DIR + file.getOriginalFilename());
            Files.write(path, bytes);

            MetaData metaData = initMetaData(file, username);

            // Save metadata to database

        } catch (IOException e) {

            e.printStackTrace();
            return "Failed to upload file: " + e.getMessage();
        }
        return "File uploaded successfully: " + file.getOriginalFilename();
    }

    private MetaData initMetaData(MultipartFile file, String username){
        UUID uuid1 = UUID.randomUUID();
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        return new MetaData(uuid1, username, contentType, originalFilename, size);
    }
}
