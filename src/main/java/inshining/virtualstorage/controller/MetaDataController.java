package inshining.virtualstorage.controller;

import inshining.virtualstorage.dto.FileUploadResponse;
import inshining.virtualstorage.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/file")
public class MetaDataController {
    private final MetaDataService metaDataService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file, @RequestParam("user") String username) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload");
        }
        FileUploadResponse response =  metaDataService.uploadFile(file, username);
        if (response.isUploaded()){
            return ResponseEntity.ok(response.message());
        } else {
            return ResponseEntity.badRequest().body(response.message());
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<String> delete(@RequestParam("file") String filename, @RequestParam("user") String username) {
        String body = metaDataService.deleteFile(filename, username);
        return ResponseEntity.ok(body);
    }
}
