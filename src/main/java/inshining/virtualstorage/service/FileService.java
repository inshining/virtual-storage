package inshining.virtualstorage.service;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.SuccessResponse;
import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.service.metadata.FileMetaDataService;
import inshining.virtualstorage.service.storage.StorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileService {
    private final FileMetaDataService fileMetaDataService;
    private final StorageService storageService;

    @Transactional
    public SuccessResponse uploadFile(MultipartFile file, String username) throws IOException {

        FileMetaData metaData = new FileMetaData(file, username);

        boolean isWriteFile = storageService.uploadFile(metaData.getStoragePath(), file);

        if (!isWriteFile){
            return new SuccessResponse(false, "Failed to upload file: File is not written");
        }

        // Save metadata to database
        fileMetaDataService.createFile(metaData);


        return new SuccessResponse(true, "File uploaded successfully");
    }

    public SuccessResponse deleteFile(String filename, String username) {

        // find file in meta data from database
        MetaData metaData = fileMetaDataService.findByOriginalFilenameAndUsername(filename, username);
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

        fileMetaDataService.delete(metaData);

        if (!isDeleted) {
            return new SuccessResponse(false, "Failed to delete file");
        }
        return new SuccessResponse(true, "File deleted successfully");
    }


    public FileDownloadDTO downloadFile(String filename, String username) throws IOException, NullPointerException{
        MetaData metaData = fileMetaDataService.findByOriginalFilenameAndUsername(filename, username);
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


}
