package inshining.virtualstorage.repository;


import inshining.virtualstorage.model.FolderMetaData;
import inshining.virtualstorage.model.MetaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FakeFolderMetaDataRepository implements MetaDataRepository{

        private final HashMap<UUID, MetaData> metaDataHashMap = new HashMap<>();

        @Override
        public MetaData save(MetaData metaData) {
                return metaDataHashMap.put(metaData.getId(), metaData);
        }

        @Override
        public Boolean existsById(UUID id) {
                MetaData metaData = metaDataHashMap.get(id);
                return metaData != null;
        }

        @Override
        public MetaData findByOriginalFilenameAndUsername(String filename, String username) {
                for (MetaData metaData : metaDataHashMap.values()) {
                        if (metaData.getOriginalFilename().equals(filename) && metaData.getUsername().equals(username)) {
                                return metaData;
                        }
                }
                return null;
        }

        @Override
        public void delete(MetaData metaData) {
                metaDataHashMap.remove(metaData.getId());
        }

        @Override
        public boolean existsByOriginalFilenameAndUsernameInFolders(String folderName, String user) {
                for (MetaData metaData : metaDataHashMap.values()) {
                        if (metaData.getOriginalFilename().equals(folderName) && metaData.getUsername().equals(user) && metaData.getContentType().equals(FolderMetaData.CONTENT_TYPE)) {
                                return true;
                        }
                }
                return false;
        }

        @Override
        public List<MetaData> findAllByParent(MetaData metaData) {
                ArrayList<MetaData> result = new ArrayList<>();
                for (MetaData data : metaDataHashMap.values()) {
                        if (data.getParent() != null && data.getParent().equals(metaData)) {
                                result.add(data);
                        }
                }
                return result;
        }
}
