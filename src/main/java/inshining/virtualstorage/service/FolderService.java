package inshining.virtualstorage.service;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import inshining.virtualstorage.service.storage.LocalStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Transactional
@Service
public class FolderService {
    private final FolderMetaDataService folderMetaDataService;
    private final LocalStorageService folderStorageService;

    public FolderCreateResponse createFolder(String user, String folder){
        FolderCreateResponse folderCreateResponse;
        try {
            folderStorageService.createFolder(user, folder);
            folderCreateResponse = folderMetaDataService.createFolder(user, folder);
        } catch (DuplicateFileNameException n) {
            throw n;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return folderCreateResponse;
    }

    public FolderMetaResponse getMetaDataInFolder(String username, String folderName) {
        return folderMetaDataService.listMetadataInFolder(username, folderName);
    }

    public FolderCreateResponse renameFolder(String username, String folderName, String newFolderName) throws NoExistFolderException {
        // TODO: metadat나 실제 storage 중 하나라도 실패하면 rollback 해야함
        FolderCreateResponse folderCreateResponse;
        try{
            folderCreateResponse = folderMetaDataService.renameFolder(username, folderName, newFolderName);
        } catch (NoExistFolderException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        boolean isSuccess = folderStorageService.renameFolderName(username, folderName, newFolderName);
        return folderCreateResponse;
    }

    public boolean deleteFolder(String username, String folderName) {
        boolean isSuccess = folderStorageService.deleteFolder(username, folderName);
        if (isSuccess) {
            folderMetaDataService.deleteFolder(username, folderName);
        }
        return isSuccess;
    }

    public boolean move(String username, String folderName, String destFolderName) {
        Path srcPath = Paths.get(folderName);
        Path destPath = Paths.get(destFolderName);
        boolean isSuccess = folderMetaDataService.move(username, srcPath, destPath);
        if (!isSuccess) {
            throw new NoExistFolderException();
        }
        // local storage 저장할 위치 설정 class 따로 분리해서 처리해야 할듯? static class
        String LOCAL_STORAGE_PATH = "upload/";
        Path realSrcPath = Paths.get(LOCAL_STORAGE_PATH, username, folderName);
        Path realDestPath = Paths.get(LOCAL_STORAGE_PATH, username, destFolderName);
        isSuccess = folderStorageService.move(username, realSrcPath, realDestPath);
        if (!isSuccess) {
            throw new NoExistFolderException();
        }
        return isSuccess;

    }
}
