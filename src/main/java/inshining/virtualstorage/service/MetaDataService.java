package inshining.virtualstorage.service;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.MetaDataFileResponse;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MetaDataService {

    public static final String UPLOAD_DIR = "upload/";

    @Autowired
    private final MetadataRepository metadataRepository;

    public MetaDataFileResponse uploadFile(MultipartFile file, String username){

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            MetaData metaData = initMetaData(file, username);

            Path path = Paths.get(UPLOAD_DIR + metaData.getId());
            Files.write(path, bytes);

            // Save metadata to database
            metadataRepository.save(metaData);

            Boolean isExit = metadataRepository.existsById(metaData.getId());

            if (isExit == false) {
                // 디비에 저장되지 않았다면 파일을 삭제해야함
                deleteFileFromStorage(metaData.getId());
                return new MetaDataFileResponse(false, "Failed to upload file: MetaData is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MetaDataFileResponse(false, "Failed to upload file: " + e.getMessage());
        }
        return new MetaDataFileResponse(true, "File uploaded successfully");
    }

    public MetaDataFileResponse deleteFile(String filename, String username) {

        // find file in meta data from database
        MetaData metaData = metadataRepository.findByOriginalFilenameAndUsername(filename, username);
        if (metaData == null) {
            return new MetaDataFileResponse(false, "File not found");
        }

        if (! metaData.getUsername().equals(username)) {
            return new MetaDataFileResponse(false, "You are not authorized to delete this file");
        }

        UUID uuid = metaData.getId();
        Boolean isDeleted = deleteFileFromStorage(uuid);

        metadataRepository.delete(metaData);

        if (!isDeleted) {
            return new MetaDataFileResponse(false, "Failed to delete file");
        }
        return new MetaDataFileResponse(true, "File deleted successfully");
    }

    private MetaData initMetaData(MultipartFile file, String username){
        UUID uuid1 = UUID.randomUUID();
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        return new MetaData(uuid1, username, contentType, originalFilename, size);
    }

    private static Boolean deleteFileFromStorage(UUID uuid) {
        try {
            Path path = Paths.get(UPLOAD_DIR + uuid);
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public FileDownloadDTO downloadFile(String filename, String username) throws IOException {
        // TODO: null 반환하는 것은 추후에 처리하도록 변경
        MetaData metaData = metadataRepository.findByOriginalFilenameAndUsername(filename, username);
        if (metaData == null) {
            return null;
        }
        InputStream inputStream = getFileAsInputStream(metaData.getId().toString());
        if (inputStream == null) {
            return null;
        }
        return new FileDownloadDTO(inputStream, metaData.getOriginalFilename(), MediaType.parseMediaType(metaData.getContentType()), metaData.getSize());
    }

    private InputStream getFileAsInputStream(String storagePath) throws IOException{
        try {
            return Files.newInputStream(Paths.get(UPLOAD_DIR, storagePath));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
