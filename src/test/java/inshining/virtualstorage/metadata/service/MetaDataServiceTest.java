package inshining.virtualstorage.metadata.service;

import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.MetaDataFileResponse;
import inshining.virtualstorage.model.MetaData;
import inshining.virtualstorage.repository.MetaDataRepository;
import inshining.virtualstorage.service.LocalStorageService;
import inshining.virtualstorage.service.MetaDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class MetaDataServiceTest {

    @Mock
    private MetaDataRepository metadataRepository;

    @Mock
    private LocalStorageService storageService;

    @InjectMocks
    private MetaDataService metaDataService;

    public MultipartFile file;


    @BeforeEach
    void setUp() {
        try{
            MockitoAnnotations.openMocks(this);

            String fileName = "testfile.txt";
            String content = "Hello, World!";
            String contentType = "text/plain";

            file = new MockMultipartFile(
                    "file",
                    fileName,
                    contentType,
                    content.getBytes()
            );
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void createMetadataObjectTest(){
        // Get the file and save it somewhere
        UUID uuid1 = UUID.randomUUID();
        String username = "testuser";
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        long size = file.getSize();
        MetaData metaData = new MetaData(uuid1, username, contentType, originalFilename, size);
        assertEquals(uuid1, metaData.getId());
        assertEquals(username, metaData.getUsername());
        assertEquals(contentType, metaData.getContentType());
        assertEquals(originalFilename, metaData.getOriginalFilename());
        assertEquals(size, metaData.getSize());
    }

    @Test
    public void testUploadFileSuccess() throws IOException {
        UUID uuid = UUID.randomUUID();
        MetaData metaData = new MetaData(uuid, "testuser", "text/plain", "testfile.txt", 12);
        when(metadataRepository.save(any(MetaData.class))).thenReturn(metaData);
        when(metadataRepository.existsById(any())).thenReturn(true);
        when(storageService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(true);

        MetaDataFileResponse response = metaDataService.uploadFile(file, "testuser");
        assertTrue(response.isSuccess());

        verify(metadataRepository, times(1)).save(any(MetaData.class));
    }

    @Test
    public void testUploadFileFailNoWriteFile() throws IOException {
        when(metadataRepository.save(any(MetaData.class))).thenReturn(null);
        when(metadataRepository.existsById(any())).thenReturn(false);
        when(storageService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(false);

        MetaDataFileResponse response = metaDataService.uploadFile(file, "testuser");
        assertFalse(response.isSuccess());
        assertEquals(response.message(), "Failed to upload file: File is not written");

    }

    @Test
    void uploadFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes()
        );
        String username = "testUser";


        when(metadataRepository.existsById(any(UUID.class))).thenReturn(true);
        when(storageService.uploadFile(anyString(), any(MultipartFile.class))).thenReturn(true);

        MetaDataFileResponse response = metaDataService.uploadFile(file, username);

        assertTrue(response.isSuccess());
        assertEquals("File uploaded successfully", response.message());
        verify(metadataRepository, times(1)).save(any(MetaData.class));
    }

    @Test
    void uploadFile_Failure() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes()
        );
        String username = "testUser";

        when(metadataRepository.existsById(any(UUID.class))).thenReturn(false);

        MetaDataFileResponse response = metaDataService.uploadFile(file, username);

        assertFalse(response.isSuccess());
        assertTrue(response.message().startsWith("Failed to upload file"));
    }

    @Test
    void deleteFile_Success() throws IOException{
        String filename = "test.txt";
        String username = "testUser";
        MetaData metaData = new MetaData(UUID.randomUUID(), username, "text/plain", filename, 100L);

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(metaData);
        when(storageService.deleteFile(metaData.getStoragePath())).thenReturn(true);

        MetaDataFileResponse response = metaDataService.deleteFile(filename, username);

        assertTrue(response.isSuccess());
        assertEquals("File deleted successfully", response.message());
        verify(metadataRepository, times(1)).delete(metaData);
    }

    @Test
    void deleteFile_FileNotFound() {
        String filename = "nonexistent.txt";
        String username = "testUser";

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(null);

        MetaDataFileResponse response = metaDataService.deleteFile(filename, username);

        assertFalse(response.isSuccess());
        assertEquals("File not found", response.message());
        verify(metadataRepository, never()).delete(any(MetaData.class));
    }

    @DisplayName("파일이 존재하지 않아서 삭제되지 않은 경우 테스트")
    @Test
    void deleteFileFailNoExistFile() throws IOException{
        String filename = "test.txt";
        String username = "testUser";
        MetaData metaData = new MetaData(UUID.randomUUID(), username, "text/plain", filename, 100L);

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(metaData);
        when(storageService.deleteFile(metaData.getStoragePath())).thenReturn(false);

        MetaDataFileResponse response = metaDataService.deleteFile(filename, username);

        assertFalse(response.isSuccess());
        assertTrue(response.message().startsWith("Failed to delete file"));
        verify(metadataRepository, times(1)).delete(metaData);
    }

    @Test
    void downloadFileSuccess() throws IOException {
        String filename = "test.txt";
        String username = "testUser";
        UUID fileId = UUID.randomUUID();
        MetaData metaData = new MetaData(fileId, username, "text/plain", filename, 100L);
        byte[] bytes = "Test content".getBytes();
        // Create a temporary file for testing
        Path tempFile = Files.createTempFile(fileId.toString(), ".txt");
        Files.write(tempFile, bytes);

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(metaData);
        when(storageService.getFileAsInputStream(metaData.getStoragePath())).thenReturn(Files.newInputStream(tempFile));

        FileDownloadDTO downloadDTO = metaDataService.downloadFile(filename, username);

        assertNotNull(downloadDTO);
        assertTrue(assertEqualsInputStream(new ByteArrayInputStream(bytes), downloadDTO.inputStream()));
        assertEquals(filename, downloadDTO.filename());
        assertEquals(MediaType.TEXT_PLAIN, downloadDTO.contentType());
        assertEquals(100L, downloadDTO.size());

        // Clean up
        Files.delete(tempFile);
    }

    @Test
    void downloadFileFailFileNotFound(){
        String filename = "nonexistent.txt";
        String username = "testUser";

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(null);

        assertThrows(NullPointerException.class, () -> metaDataService.downloadFile(filename, username));
    }

    @Test
    void downloadFileFailNoInputStream() throws IOException {
        String filename = "test.txt";
        String username = "testUser";
        UUID fileId = UUID.randomUUID();
        MetaData metaData = new MetaData(fileId, username, "text/plain", filename, 100L);

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(metaData);
        when(storageService.getFileAsInputStream(metaData.getStoragePath())).thenReturn(null);

        assertThrows(NullPointerException.class, () -> metaDataService.downloadFile(filename, username));
    }

    @Test
    void downloadFileFailIOException() throws IOException {
        String filename = "test.txt";
        String username = "testUser";
        UUID fileId = UUID.randomUUID();
        MetaData metaData = new MetaData(fileId, username, "text/plain", filename, 100L);

        when(metadataRepository.findByOriginalFilenameAndUsername(filename, username)).thenReturn(metaData);
        when(storageService.getFileAsInputStream(metaData.getStoragePath())).thenThrow(new IOException("Failed to read file"));

        assertThrows(IOException.class, () -> metaDataService.downloadFile(filename, username));
    }

    private boolean assertEqualsInputStream(InputStream inputStream1, InputStream inputStream2) throws IOException {
        if (inputStream1 == inputStream2) {
            return true;
        }
        if (inputStream1 == null || inputStream2 == null) {
            return false;
        }

        try (inputStream1; inputStream2) {
            int byte1, byte2;
            while ((byte1 = inputStream1.read()) != -1) {
                byte2 = inputStream2.read();
                if (byte1 != byte2) {
                    return false;
                }
            }
            // Check if the second stream has more bytes
            return inputStream2.read() == -1;
        }
    }
}
