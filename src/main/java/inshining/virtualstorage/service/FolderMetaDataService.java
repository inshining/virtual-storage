package inshining.virtualstorage.service;

import exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FolderMetaDataService {

    private final MetaDataRepository metaDataRepository;

    public FolderCreateResponse createFolder(String user, String folderPath) {
        String[] path = folderPath.split("/");
        String folderName = path[path.length - 1];
        String parentPath = folderPath.substring(0, folderPath.length() - folderName.length());
        if (parentPath.length() == 0) {
            parentPath = "/";
        }
        if (metaDataRepository.existsByOriginalFilenameAndUsernameInFolders(folderName, user)) {
            throw new DuplicateFileNameException();
        }
        MetaData folder = new FolderMetaData(user, folderName);
        metaDataRepository.save(folder);
        return new FolderCreateResponse(user, folderName, parentPath);
    }
}
