package inshining.virtualstorage.metadata.service;

public class FolderMetaDataService {
    public FolderCreateResponse createFolder(String user, String folderPath) {
        String[] path = folderPath.split("/");
        String folderName = path[path.length - 1];
        String parentPath = folderPath.substring(0, folderPath.length() - folderName.length());
        if (parentPath.length() == 0) {
            parentPath = "/";
        }
        return new FolderCreateResponse(user, folderName, parentPath);
    }
}
