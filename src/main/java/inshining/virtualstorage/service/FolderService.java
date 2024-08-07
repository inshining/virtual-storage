package inshining.virtualstorage.service;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.service.metadata.FolderMetaDataService;
import inshining.virtualstorage.service.storage.FolderStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FolderService {
    private final FolderMetaDataService folderMetaDataService;
    private final FolderStorageService folderStorageService;

    public FolderCreateResponse createFolder(String user, String folder){
        FolderCreateResponse folderCreateResponse;
        try {
            folderCreateResponse = folderMetaDataService.createFolder(user, folder);
        } catch (DuplicateFileNameException n) {
            throw n;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        folderStorageService.createFolder(user, folder);
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
}
