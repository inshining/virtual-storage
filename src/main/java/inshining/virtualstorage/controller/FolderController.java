package inshining.virtualstorage.controller;

import inshining.virtualstorage.dto.FolderRenameRequest;
import inshining.virtualstorage.dto.MoveRequest;
import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderRequestBody;
import inshining.virtualstorage.dto.FolderCreateResponse;
import inshining.virtualstorage.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/folder")
public class FolderController {
    private final FolderService folderService;

    @PostMapping("/")
    public ResponseEntity createFolder(@RequestBody FolderRequestBody request) {
        String user = request.user();
        String folderName = request.folderName();
        FolderCreateResponse folderCreateResponse;
        try {
            folderCreateResponse = folderService.createFolder(user, folderName);
        } catch (DuplicateFileNameException d){
            return ResponseEntity.badRequest().body("Duplicate file name");
        } catch (Exception e){
            return ResponseEntity.badRequest().body( e.getMessage());
        }
        return ResponseEntity.ok(folderCreateResponse);
    }

    @PutMapping("/")
    public ResponseEntity renameFolder(@RequestBody FolderRenameRequest request) {
        FolderCreateResponse folderRenameRequest;
        String user = request.userName();
        String originFolderName = request.originFolderName();
        String targetFolderName = request.targetFolderName();

        try {
            folderRenameRequest = folderService.renameFolder(user, originFolderName, targetFolderName);
        } catch (DuplicateFileNameException d){
            return ResponseEntity.badRequest().body("Duplicate file name");
        } catch (Exception e){
            return ResponseEntity.badRequest().body( e.getMessage());
        }
        return ResponseEntity.ok(folderRenameRequest);
    }

    @GetMapping("get/")
    public ResponseEntity<String> getFolder() {
        return ResponseEntity.ok("Folder found");
    }

    @DeleteMapping("/")
    public ResponseEntity<String> deleteFolder(@RequestBody FolderRequestBody request) {
        String user = request.user();
        String folderName = request.folderName();
        boolean isSuccess = folderService.deleteFolder(user, folderName);
        if (isSuccess){
            return ResponseEntity.ok("Folder deleted");
        } else{
            return ResponseEntity.badRequest().body("Folder not found");
        }
    }

    @PostMapping("/move")
    public ResponseEntity<String> move(@RequestBody MoveRequest request) {
        String user = request.user();
        String srcPath = request.srcPath();
        String destPath = request.destPath();

        boolean isSuccess = folderService.move(user, srcPath, destPath);
        if (isSuccess){
            return ResponseEntity.ok("Folder moved");
        } else{
            return ResponseEntity.badRequest().body("Folder not found");
        }
    }
}
