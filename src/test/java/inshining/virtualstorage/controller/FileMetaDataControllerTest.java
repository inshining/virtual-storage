package inshining.virtualstorage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import inshining.virtualstorage.controller.MetaDataController;
import inshining.virtualstorage.dto.FileDownloadDTO;
import inshining.virtualstorage.dto.FolderRequestBody;
import inshining.virtualstorage.dto.MoveRequest;
import inshining.virtualstorage.dto.SuccessResponse;
import inshining.virtualstorage.service.FileService;
import org.junit.jupiter.api.DisplayName;
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

    @Autowired
    private ObjectMapper objectMapper;

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

    @DisplayName("성공: 파일 이동")
    @Test
    void testMoveFileSuccess() throws Exception {
        String user = "user";
        String srcPath = "hello.txt";
        String destPath = "test/folder";

        when(fileService.moveFile(user, srcPath, destPath))
                .thenReturn(new SuccessResponse(true, "File moved successfully"));

        String content = objectMapper.writeValueAsString(new MoveRequest(user, srcPath, destPath));

        mockMvc.perform(MockMvcRequestBuilders.post("/file/move")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("File moved successfully"));
    }

    @DisplayName("실패: 파일 이동")
    @Test
    void testMoveFileFail() throws Exception {
        String user = "user";
        String srcPath = "hello.txt";
        String destPath = "test/folder";

        when(fileService.moveFile(user, srcPath, destPath))
                .thenReturn(new SuccessResponse(false, "File moved failed"));

        String content = objectMapper.writeValueAsString(new MoveRequest(user, srcPath, destPath));

        mockMvc.perform(MockMvcRequestBuilders.put("/file/move")
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File moved failed"));
    }
}
