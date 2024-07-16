package inshining.virtualstorage.service;

import inshining.virtualstorage.dto.FolderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FolderService {
    private final FolderMetaDataService folderMetaDataService;
//    private final FolderStorageService folderStorageService;

    public FolderCreateResponse createFolder(String user, String folder) {
        FolderCreateResponse folderCreateResponse = folderMetaDataService.createFolder(user, folder);
        return folderCreateResponse;
    }
}
