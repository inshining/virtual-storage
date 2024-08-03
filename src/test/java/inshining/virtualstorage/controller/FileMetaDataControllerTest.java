package inshining.virtualstorage.controller;

import inshining.virtualstorage.controller.MetaDataController;
import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.SuccessResponse;
import inshining.virtualstorage.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(MetaDataController.class)
public class FileMetaDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;


    @Test
    public void testUpload() throws Exception{
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        when(fileService.uploadFile(any(MultipartFile.class), any(String.class))).thenReturn(new SuccessResponse(true, "File uploaded successfully"));

        mockMvc.perform(
                multipart("/file/upload").file(file)
                .param("user", "testuser")
                ).andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully"));
    }

    @Test
    public void testUploadFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        // Mock the service method to return false (failed upload)
        when(fileService.uploadFile(any(MultipartFile.class), any(String.class))).thenReturn(new SuccessResponse(false, "Failed to upload file"));

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to upload file"));
    }

    @Test
    public void testUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/file/upload")
                        .file(file)
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please select a file to upload"));
    }

    @Test
    void testDeleteSuccess() throws Exception {
        when(fileService.deleteFile("hello.txt", "test"))
                .thenReturn(new SuccessResponse(true, "File deleted successfully"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/file/")
                        .param("file", "hello.txt")
                        .param("user", "test"))
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted successfully"));
    }

    @Test
    void testDeleteFailNotFound() throws Exception {
        when(fileService.deleteFile("hello.txt", "test"))
                .thenReturn(new SuccessResponse(false, "File not found"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/file/")
                        .param("file", "hello.txt")
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File not found"));
    }
    @Test
    void testDeleteFailNoAuthorized() throws Exception {
        when(fileService.deleteFile("hello.txt", "test"))
                .thenReturn(new SuccessResponse(false, "You are not authorized to delete this file"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/file/")
                        .param("file", "hello.txt")
                        .param("user", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("You are not authorized to delete this file"));
    }

    @Test
    void testDownloadSuccess() throws Exception{
        when(fileService.downloadFile("hello.txt", "test"))
                .thenReturn(new FileDownloadDTO(new ByteArrayInputStream("Hello, World!".getBytes()), "hello.txt", MediaType.TEXT_PLAIN, 13));

        mockMvc.perform(MockMvcRequestBuilders.get("/file/download")
                        .param("filename", "hello.txt")
                        .param("user", "test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void testDownloadFailNoUsername() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/file/download")
                        .param("filename", "hello.txt"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void testDownloadFailNoFileName() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/file/download")
                        .param("user", "test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadFailNoFileNameAndUsername() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/file/download"))
                .andExpect(status().isBadRequest());
    }
}
