package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.dto.MetaDataFileResponse;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetadataRepository;
import inshining.virtualstorage.service.MetaDataService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static inshining.virtualstorage.service.MetaDataService.UPLOAD_DIR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class MetaDataMethodTest {


    @Mock
    private MetadataRepository metadataRepository;

    @InjectMocks
    private MetaDataService metaDataService;

    public MultipartFile file;


    @BeforeEach
    void setUp() {
        String fileName = "testfile.txt";
        String content = "Hello, World!";
        String contentType = "text/plain";

        file = new MockMultipartFile(
                "file",
                fileName,
                contentType,
                content.getBytes()
        );
    }

    @AfterAll
    static void tearDown() {
        deleteAllFilesInDirectory(UPLOAD_DIR);
    }

    @Test
    public void testUploadFileSuccess() {
        UUID uuid = UUID.randomUUID();
        MetaData metaData = new MetaData(uuid, "testuser", "text/plain", "testfile.txt", 12);
        when(metadataRepository.save(any(MetaData.class))).thenReturn(metaData);
        when(metadataRepository.existsById(any())).thenReturn(true);

        MetaDataFileResponse response = metaDataService.uploadFile(file, "testuser");
        assertTrue(response.isSuccess());

        verify(metadataRepository, times(1)).save(any(MetaData.class));
    }

    @Test
    public void testUploadFileFail() {
        when(metadataRepository.save(any(MetaData.class))).thenReturn(null);
        when(metadataRepository.existsById(any())).thenReturn(false);

        MetaDataFileResponse response = metaDataService.uploadFile(file, "testuser");
        assertFalse(response.isSuccess());

        verify(metadataRepository, times(1)).save(any(MetaData.class));
    }


    public static void deleteAllFilesInDirectory(String directoryPath) {
        Path dirPath = Paths.get(directoryPath);

        if (Files.exists(dirPath) && Files.isDirectory(dirPath)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path filePath : stream) {
                    if (Files.isRegularFile(filePath)) {
                        Files.delete(filePath);
                        System.out.println("Deleted file: " + filePath);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error deleting files: " + e.getMessage());
            }
        } else {
            System.err.println("Directory does not exist or is not a directory.");
        }
    }

}
