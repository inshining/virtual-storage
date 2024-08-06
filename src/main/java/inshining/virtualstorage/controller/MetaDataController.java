package inshining.virtualstorage.controller;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.MoveRequest;
import inshining.virtualstorage.dto.SuccessResponse;
import inshining.virtualstorage.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
public class MetaDataController {
    private final FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("user") String username) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        SuccessResponse response;
        try {
            response =  fileService.uploadFile(file, username);
        } catch (IOException e){
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
        if (response.isSuccess()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<String> delete(@RequestParam("file") String filename, @RequestParam("user") String username) {
        SuccessResponse response = fileService.deleteFile(filename, username);
        if (response.isSuccess()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("filename") String filename, @RequestParam("user") String username) {
        if (filename.isEmpty() || username.isEmpty()){
            return ResponseEntity.badRequest().body(null);
        }
        FileDownloadDTO fileDownload;
        try{
            fileDownload = fileService.downloadFile(filename, username);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
        InputStreamResource resource = new InputStreamResource(fileDownload.inputStream());
        return ResponseEntity.ok()
                .contentType(fileDownload.contentType())
                .contentLength(fileDownload.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileDownload.filename())
                .body(resource);
    }

    @PutMapping("/move")
    public ResponseEntity<String> move(@RequestBody MoveRequest moveRequest) {
        String username = moveRequest.user();
        String srcPath = moveRequest.srcPath();
        String destPath = moveRequest.destPath();

        SuccessResponse response = fileService.moveFile(username, srcPath, destPath);
        if (response.isSuccess()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }
}
