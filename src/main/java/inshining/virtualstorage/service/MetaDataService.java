package inshining.virtualstorage.service;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.MetaDataFileResponse;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MetaDataService {

    @Autowired
    private final MetaDataRepository metadataRepository;

    @Autowired
    private final StorageService storageService;

    public MetaDataFileResponse uploadFile(MultipartFile file, String username){

        try {
            // Get the file and save it somewhere
            MetaData metaData = initMetaData(file, username);

            boolean isWriteFile = storageService.uploadFile(metaData.getStoragePath(), file);

            if (!isWriteFile){
                return new MetaDataFileResponse(false, "Failed to upload file: File is not written");
            }

            // Save metadata to database
            metadataRepository.save(metaData);

            Boolean isExit = metadataRepository.existsById(metaData.getId());

            if (!isExit){
                // 디비에 저장되지 않았다면 파일을 삭제해야함
                storageService.deleteFile(metaData.getStoragePath());
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

        boolean isDeleted;

        try {
            isDeleted = storageService.deleteFile(metaData.getStoragePath());
        } catch (IOException e) {
            e.printStackTrace();
            return new MetaDataFileResponse(false, "Failed to delete file: " + e.getMessage());
        }

        metadataRepository.delete(metaData);

        if (!isDeleted) {
            return new MetaDataFileResponse(false, "Failed to delete file");
        }
        return new MetaDataFileResponse(true, "File deleted successfully");
    }


    public FileDownloadDTO downloadFile(String filename, String username) throws IOException, NullPointerException{
        MetaData metaData = metadataRepository.findByOriginalFilenameAndUsername(filename, username);
        if (metaData == null) {
            throw new NullPointerException("Failed to download file: MetaData not found");
        }

        InputStream inputStream;
        inputStream = storageService.getFileAsInputStream(metaData.getStoragePath());
        if (inputStream == null) {
            throw new NullPointerException("Failed to download file: File not found");
        }
        return new FileDownloadDTO(inputStream, metaData.getOriginalFilename(), MediaType.parseMediaType(metaData.getContentType()), metaData.getSize());
    }

    private MetaData initMetaData(MultipartFile file, String username){
        UUID uuid1 = UUID.randomUUID();
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        return new MetaData(uuid1, username, contentType, originalFilename, size);
    }

}
