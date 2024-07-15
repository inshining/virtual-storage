package inshining.virtualstorage.service;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.SuccessResponse;
import inshining.virtualstorage.model.FileMetaData;
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

    private final MetaDataRepository metadataRepository;

    private final StorageService storageService;

    public SuccessResponse uploadFile(MultipartFile file, String username){

        try {
            // Get the file and save it somewhere
            FileMetaData metaData = initFileMetaData(file, username);

            boolean isWriteFile = storageService.uploadFile(metaData.getStoragePath(), file);

            if (!isWriteFile){
                return new SuccessResponse(false, "Failed to upload file: File is not written");
            }

            // Save metadata to database
            metadataRepository.save(metaData);

            Boolean isExit = metadataRepository.existsById(metaData.getId());

            if (!isExit){
                // 디비에 저장되지 않았다면 파일을 삭제해야함
                storageService.deleteFile(metaData.getStoragePath());
                return new SuccessResponse(false, "Failed to upload file: MetaData is not created");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new SuccessResponse(false, "Failed to upload file: " + e.getMessage());
        }
        return new SuccessResponse(true, "File uploaded successfully");
    }

    public SuccessResponse deleteFile(String filename, String username) {

        // find file in meta data from database
        MetaData metaData = metadataRepository.findByOriginalFilenameAndUsername(filename, username);
        if (metaData == null) {
            return new SuccessResponse(false, "File not found");
        }

        if (! metaData.getUsername().equals(username)) {
            return new SuccessResponse(false, "You are not authorized to delete this file");
        }

        boolean isDeleted;

        try {
            isDeleted = storageService.deleteFile(metaData.getStoragePath());
        } catch (IOException e) {
            e.printStackTrace();
            return new SuccessResponse(false, "Failed to delete file: " + e.getMessage());
        }

        metadataRepository.delete(metaData);

        if (!isDeleted) {
            return new SuccessResponse(false, "Failed to delete file");
        }
        return new SuccessResponse(true, "File deleted successfully");
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

    private FileMetaData initFileMetaData(MultipartFile file, String username){
        UUID uuid1 = UUID.randomUUID();
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        return new FileMetaData(uuid1, username, contentType, originalFilename, size);
    }

}
