package inshining.virtualstorage.service.metadata;

import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.dto.FolderMetaResponse;
import inshining.virtualstorage.dto.MetaDataDTO;
import inshining.virtualstorage.exception.NoExistFolderException;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FolderMetaDataService {

    private final MetaDataRepository metaDataRepository;

    public FolderCreateResponse createFolder(String user, String folderPath) {
        String[] path = folderPath.split("/");
        String folderName = path[path.length - 1];
        String parentPath = folderPath.substring(0, folderPath.length() - folderName.length());
        if (metaDataRepository.existsByOriginalFilenameAndUsernameInFolders(folderName, user)) {
            throw new DuplicateFileNameException();
        }
        UUID uuid = UUID.randomUUID();

        FolderMetaData parentFolder;
        MetaData folder;
        if (parentPath.length() != 0) {
            parentFolder = metaDataRepository.findFolderByPathAndUsername(parentPath, user);
            folder = new FolderMetaData(uuid, user, folderName, parentPath, parentFolder);
        } else{
            parentPath = "/";
            folder = new FolderMetaData(uuid, user, folderName, parentPath, null);
        }

        metaDataRepository.save(folder);
        return new FolderCreateResponse(user, folderName, parentPath);
    }

    public FolderMetaResponse listMetadataInFolder(String user, String folder) {
        MetaData metaData = metaDataRepository.findByOriginalFilenameAndUsername(folder, user);
        List<MetaDataDTO> metaDataDTOList = new ArrayList<>();
        for (MetaData data : metaDataRepository.findAllByParent(metaData)) {
            metaDataDTOList.add(new MetaDataDTO(data.getUsername(), data.getOriginalFilename(), data.getContentType(), data.getSize(), data.getCreatedAt(), data.getUpdatedAt()));
        }
        String path = metaData.getPath();

        return new FolderMetaResponse(user, folder,path, metaDataDTOList);
    }

    public FolderCreateResponse renameFolder(String username, String originalName, String changedName) throws NoExistFolderException{
        MetaData metaData = metaDataRepository.findByOriginalFilenameAndUsername(originalName, username);
        if (metaData == null) {
            throw new NoExistFolderException();
        }
        metaData.setOriginalFilename(changedName);
        MetaData changedMetaData = metaDataRepository.save(metaData);
        return new FolderCreateResponse(changedMetaData.getUsername(), changedMetaData.getOriginalFilename(), changedMetaData.getPath());
    }

    public boolean deleteFolder(String username, String originalName) {
        MetaData metaData = metaDataRepository.findByOriginalFilenameAndUsername(originalName, username);
        if (metaData == null) {
            return false;
        }
        // 탈출 조건: Folder 타입이 아니면 삭제하지 않음
        if (!metaData.getContentType().equals(FolderMetaData.CONTENT_TYPE)) {
            return false;
        }

        // 하위 폴더 및 파일 삭제
        searchSubMetaData(metaData).forEach(data -> metaDataRepository.delete(data));
        metaDataRepository.delete(metaData);
        return true;
    }

    private List<MetaData> searchSubMetaData(MetaData metaData) {
        List<MetaData> result = new ArrayList<>();
        List<MetaData> subMetaDataByParent = metaDataRepository.findAllByParent(metaData);

        if (subMetaDataByParent.size() == 0) {
            return result;
        }

        for (MetaData data : subMetaDataByParent) {
            result.add(data);
            if (data.getContentType().equals(FolderMetaData.CONTENT_TYPE)) {
                result.addAll(searchSubMetaData(data));
            }
        }
        return result;
    }

    /**
     * 폴더 이동
     * @param sourcePath
     * @param destPath
     * @return
     *
     */
    public boolean move(String username, Path sourcePath, Path destPath) {
        // 메타 데이터 조회
        FolderMetaData sourceFolder = (FolderMetaData) metaDataRepository.findByOriginalFilenameAndUsername(sourcePath.getFileName().toString(), username);
        FolderMetaData destFolder = (FolderMetaData) metaDataRepository.findByOriginalFilenameAndUsername(destPath.getFileName().toString(), username);

        // 폴더가 존재하지 않을 경우
        if (sourceFolder == null || destFolder == null) {
            return false;
        }

        // 폴더 이동
        sourceFolder.setParent(destFolder);
        sourceFolder.setPath(destPath.toString());

        moveSubMetaData(sourceFolder);

        metaDataRepository.save(sourceFolder);

        return true;
    }

    private void moveSubMetaData(FolderMetaData folderMetaData) {
        List<MetaData> subMetaDataByParent = metaDataRepository.findAllByParent(folderMetaData);

        if (subMetaDataByParent.size() == 0) {
            return;
        }

        Path parentPath = Paths.get(folderMetaData.getPath(), folderMetaData.getOriginalFilename());

        for (MetaData data : subMetaDataByParent) {
            data.setParent(folderMetaData);
            data.setPath(parentPath.toString());
            metaDataRepository.save(data);
            if (data.getContentType().equals(FolderMetaData.CONTENT_TYPE)) {
                FolderMetaData folder = (FolderMetaData) data;
                moveSubMetaData(folder);
            }
        }
    }
}
