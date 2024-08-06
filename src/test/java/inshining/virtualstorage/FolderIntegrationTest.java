package inshining.virtualstorage;

import inshining.virtualstorage.dto.FolderRequestBody;
import inshining.virtualstorage.dto.MoveRequest;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FolderIntegrationTest {
    private final String LOCAL_STORAGE_PATH = "upload/";
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MetaDataRepository metaDataRepository;
    
    private String username = "testUser";

    @AfterEach
    void tearDown(){
        Path path = Paths.get(LOCAL_STORAGE_PATH);
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }    }

    @Test
    void testCreateFolder() {
        String folderName = "testFolder";
        FolderRequestBody request = new FolderRequestBody(username, folderName);

        ResponseEntity response = restTemplate.postForEntity("/folder/", request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        MetaData metaData =metaDataRepository.findByOriginalFilenameAndUsername(folderName, username);
        assertNotNull(metaData);
        assertEquals(username, metaData.getUsername());
        assertEquals(folderName, metaData.getOriginalFilename());

        Path path = Paths.get(LOCAL_STORAGE_PATH, username, folderName);
        FileDeletor.delete(path, 2);
    }

    @DisplayName("성공: 폴더 이동")
    @Test
    void moveFolderTest(){
        //given
        String srcFolder = "testFolder1";
        String destFolder = "destFolder";
        
        restTemplate.postForEntity("/folder/", new FolderRequestBody(username, srcFolder), String.class);
        restTemplate.postForEntity("/folder/", new FolderRequestBody(username, destFolder), String.class);
        
        // when
        restTemplate.postForEntity("/folder/move", new MoveRequest(username, srcFolder, destFolder), String.class);
        
        // then
        Path expectedPath = Paths.get(LOCAL_STORAGE_PATH, username, destFolder, srcFolder);
        Assertions.assertTrue(Files.exists(expectedPath));

    }
}

