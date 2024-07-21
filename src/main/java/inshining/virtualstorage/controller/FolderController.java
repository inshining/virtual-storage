package inshining.virtualstorage.controller;

import inshining.virtualstorage.dto.FolderRenameRequest;
import inshining.virtualstorage.exception.DuplicateFileNameException;
import inshining.virtualstorage.dto.FolderCreateRequest;
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
    public ResponseEntity createFolder(@RequestBody FolderCreateRequest request) {
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
}
