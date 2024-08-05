package inshining.virtualstorage.service.metadata;

import inshining.virtualstorage.model.FileMetaData;
import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class FileMetaDataService {

    private final MetaDataRepository metadataRepository;

    public MetaData createFile(FileMetaData fileMetaData){
        return metadataRepository.save(fileMetaData);
    }


    public MetaData findByOriginalFilenameAndUsername(String filename, String username) {
        return metadataRepository.findByOriginalFilenameAndUsername(filename, username);
    }

    public void delete(MetaData metaData) {
        metadataRepository.delete(metaData);
    }

    public boolean existsById(UUID uuid) {
        return metadataRepository.existsById(uuid);
    }

    public boolean moveFile(String username, String sourceFilename, String destinationPath) {
        MetaData metaData = findByOriginalFilenameAndUsername(sourceFilename, username);

        Path dPath = Paths.get(destinationPath);

        // 파일 존재하는지 여부 확인
        if (metaData == null) {
            return false;
        }

        // 경로 변경
        metaData.setPath(destinationPath);
        metadataRepository.save(metaData);

        // 바꿀 파일명
        return true;
    }

    /**
     * 부모 폴더가 없으면 만들기
     * @param username
     * @param path
     * @return FolderMateData
     */
    private FolderMetaData makeParentFolder(String username, Path path) {
        if (path.getParent() == null) {
            return null;
        }

        String folderName = path.getFileName().toString();
        String pathName = path.getParent().toString();

        if (metadataRepository.existsByUsernameAndPathAndOriginalFilenameInFolder(username, pathName, folderName)) {
            return  metadataRepository.findFolderByPathAndUsername(path.toString(), username);
        } else{
            FolderMetaData parentFolderMetaData = makeParentFolder(username, path.getParent());
            FolderMetaData folderMetaData = new FolderMetaData(UUID.randomUUID(), username, folderName, pathName, parentFolderMetaData);
            metadataRepository.save(folderMetaData);
            return folderMetaData;
        }
    }

}
