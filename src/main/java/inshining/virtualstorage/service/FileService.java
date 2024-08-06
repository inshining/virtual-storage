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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
@Service
public class FileService {
    private final FileMetaDataService fileMetaDataService;
    private final StorageService storageService;

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


    /**
     * Move file to new folder
     * @param username : 사용자 이름 (파일 소유자)
     * @param srcFileName : 파일 이름 (옮기려는 파일 이름)
     * @param destFolderName : 새로운 폴더 이름 (옮기고자 하는 파일 이름)
     * @return
     */
    public SuccessResponse moveFile(String username, String srcFileName, String destFolderName) {
        Path destPath = Paths.get(destFolderName);

        // Move file in virtual storage (DB)
        boolean isMoved = fileMetaDataService.moveFile(username, srcFileName, destPath);

        if (!isMoved) {
            throw new NullPointerException("Failed to move file: MetaData not found");
        }

        // Move file in real storage
        Path realSrcPath = Paths.get(username, srcFileName);
        Path realDestPath = Paths.get(username, destFolderName);

        boolean isStorageMove = storageService.move(username, realSrcPath, realDestPath);

        if (!isStorageMove) {
            throw new NullPointerException("Failed to move file: File not found");
        }

        return new SuccessResponse(true, "File moved successfully");
    }
}
