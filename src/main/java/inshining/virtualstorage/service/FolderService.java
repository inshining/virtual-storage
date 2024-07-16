package inshining.virtualstorage.service;

import exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
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
}
