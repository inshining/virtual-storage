package inshining.virtualstorage.repository;


import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FakeFolderMetaDataRepository implements MetaDataRepository{

        private final HashMap<UUID, MetaData> store = new HashMap<>();

        @Override
        public MetaData save(MetaData metaData) {
                return store.put(metaData.getId(), metaData);
        }

        @Override
        public Boolean existsById(UUID id) {
                MetaData metaData = store.get(id);
                return metaData != null;
        }

        @Override
        public MetaData findByOriginalFilenameAndUsername(String filePath, String username) {
                String filename = filePath.substring(filePath.lastIndexOf("/") + 1);
                for (MetaData metaData : store.values()) {
                        if (metaData.getOriginalFilename().equals(filename) && metaData.getUsername().equals(username)) {
                                return metaData;
                        }
                }
                return null;
        }

        @Override
        public void delete(MetaData metaData) {
                store.remove(metaData.getId());
        }

        @Override
        public boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user) {
                for (MetaData metaData : store.values()) {
                        if (metaData.getOriginalFilename().equals(folderName) && metaData.getUsername().equals(user) && metaData.getContentType().equals(FolderMetaData.CONTENT_TYPE)) {
                                return true;
                        }
                }
                return false;
        }

        @Override
        public List<MetaData> findAllByParent(MetaData metaData) {
                ArrayList<MetaData> result = new ArrayList<>();
                for (MetaData data : store.values()) {
                        if (data.getParent() != null && data.getParent().equals(metaData)) {
                                result.add(data);
                        }
                }
                return result;
        }

        @Override
        public FolderMetaData findFolderByPathAndUsername(String path, String username) {
               return store.values().stream()
                        .filter(metaData -> metaData.getContentType().equals(FolderMetaData.CONTENT_TYPE))
                        .map(metaData -> (FolderMetaData) metaData)
                        .filter(folderMetaData -> path.startsWith(folderMetaData.getPath()) && (path.equals("/") || path.endsWith(folderMetaData.getOriginalFilename() + "/")) &&  folderMetaData.getUsername().equals(username))
                        .findFirst()
                        .orElse(null);
        }

        public List<MetaData> findAll(){
                return new ArrayList<>(store.values());
        }
}
