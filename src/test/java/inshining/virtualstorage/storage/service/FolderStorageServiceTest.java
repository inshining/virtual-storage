package inshining.virtualstorage.storage.service;

import inshining.virtualstorage.service.storage.LocalStorageService;
import inshining.virtualstorage.util.FileDeletor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs({OS.MAC, OS.LINUX})
public class FolderStorageServiceTest {
    private static final String storageLocation = "upload/";
    private static final LocalStorageService LocalStorageService = new LocalStorageService();

    private static final String USERNAME = "testUser";

    private static final String FOLDER_NAME = "testFolder";
    private static final String CHANGED_FOLDER_NAME = "changedFolder";

    @AfterEach
    void tearDown(){
        Path path = Paths.get(storageLocation);
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

    @DisplayName("폴더 1개 만들기")
    @Test
    void createFolderTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, "testFolder"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder");
        assertTrue(Files.exists(path));
    }

    @DisplayName("존재하지 않은 하위 폴더 모두 만들기")
    @Test
    void createSubFoldersTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, "testFolder1/testFolder2/testFolder3"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder1/testFolder2/testFolder3");
        assertTrue(Files.exists(path));

    }

    @DisplayName("이미 존재하는 폴더 만들기")
    @Test
    void createExistingFolderTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, "testFolder"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder");
        assertTrue(Files.exists(path));

        assertFalse(LocalStorageService.createFolder(USERNAME,"testFolder"));

        FileDeletor.delete(path, 2);

        assertTrue(LocalStorageService.createFolder(USERNAME,"testFolder/testFolder2"));

        Path path2 = Paths.get(storageLocation, USERNAME,"testFolder/testFolder2");
        assertTrue(Files.exists(path2));

        assertFalse(LocalStorageService.createFolder(USERNAME,"testFolder"));
    }

    @DisplayName("폴더 이름 변경")
    @Test
    void changeFolderNameTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, FOLDER_NAME ));

        Path path = Paths.get(storageLocation, USERNAME, FOLDER_NAME);
        assertTrue(Files.exists(path));

        assertTrue(LocalStorageService.renameFolderName(USERNAME, FOLDER_NAME, CHANGED_FOLDER_NAME));

        Path changedPath = Paths.get(storageLocation, USERNAME, CHANGED_FOLDER_NAME);
        assertTrue(Files.exists(changedPath));
    }

    @DisplayName("실패: 존재하지 않는 폴더 이름 변경")
    @Test
    void noExistFolderNameTest(){
        String noExistFolderName = "noExistFolder";
        assertTrue(LocalStorageService.createFolder(USERNAME, FOLDER_NAME ));

        Path path = Paths.get(storageLocation, USERNAME, FOLDER_NAME);
        assertTrue(Files.exists(path));

        assertFalse(LocalStorageService.renameFolderName(USERNAME, noExistFolderName, CHANGED_FOLDER_NAME));
    }

    @DisplayName("실패: 존재하지 않는 유저의 폴더 이름 변경")
    @Test
    void noExistUsernameTest(){
        String noExistUsername = "noExistUser";
        assertTrue(LocalStorageService.createFolder(USERNAME, FOLDER_NAME ));

        Path path = Paths.get(storageLocation, USERNAME, FOLDER_NAME);
        assertTrue(Files.exists(path));

        assertFalse(LocalStorageService.renameFolderName(noExistUsername, FOLDER_NAME, CHANGED_FOLDER_NAME));
    }

    @DisplayName("성공: 단일 폴더 삭제")
    @Test
    void deleteFolderTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, FOLDER_NAME));

        Path path = Paths.get(storageLocation, USERNAME, FOLDER_NAME);
        assertTrue(Files.exists(path));

        assertTrue(LocalStorageService.deleteFolder(USERNAME, FOLDER_NAME));
        assertFalse(Files.exists(path));
    }

    @DisplayName("성공: 하위 폴더 모두 삭제")
    @Test
    void deleteSubFoldersTest(){
        assertTrue(LocalStorageService.createFolder(USERNAME, "testFolder1/testFolder2/testFolder3"));

        Path path = Paths.get(storageLocation, USERNAME, "testFolder1/testFolder2/testFolder3");
        assertTrue(Files.exists(path));

        assertTrue(LocalStorageService.deleteFolder(USERNAME, "testFolder1"));
        assertFalse(Files.exists(path));
    }

    @DisplayName("실패: 존재하지 않는 폴더 삭제")
    @Test
    void noExistFolderDeleteTest(){
        String noExistFolderName = "noExistFolder";
        assertFalse(LocalStorageService.deleteFolder(USERNAME, noExistFolderName));
    }

    @DisplayName("성공: 폴더 이동 하기 (파일 이동)")
    @Test
    void moveFileFolderTest(){
        // given
        String sourcePath = "/";
        String destinationPath = "testFolder/";
        String filename = "testFile.txt";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath));

        Path source = Paths.get(storageLocation, USERNAME, sourcePath);
        assertTrue(Files.exists(source));

        // 파일 만들기
        Path file = Paths.get(storageLocation, USERNAME, filename);
        try {
            Files.createFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Path destination = Paths.get(storageLocation, USERNAME, destinationPath);

        assertTrue(LocalStorageService.createFolder(USERNAME, destinationPath));

        //when
        assertTrue(LocalStorageService.move(USERNAME, file, destination));

        assertTrue(Files.exists(destination));
    }

    @DisplayName("성공: 3중 이상 폴더일 경우 이동")
    @Test
    void moveSubFoldersTest(){
        // given
        String sourcePath = "testFolder1/testFolder2/testFolder3/";
        String destinationPath = "testFolder4/testFolder5/testFolder6/";
        String filename = "testFile.txt";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath));
        assertTrue(LocalStorageService.createFolder(USERNAME, destinationPath));

        Path source = Paths.get(storageLocation, USERNAME, sourcePath);
        Path dest = Paths.get(storageLocation, USERNAME, destinationPath);
        assertTrue(Files.exists(source));
        assertTrue(Files.exists(dest));

        // 파일 만들기
        Path file = Paths.get(source.toString(),filename);
        try {
            Files.createFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //when
        assertTrue(LocalStorageService.move(USERNAME, file, dest));

        // then
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(dest));

        Path destFilePath = Paths.get(dest.toString(), filename);
        assertTrue(Files.exists(destFilePath));

    }

    @DisplayName("성공: dest 폴더가 없을 경우 만들어서 이동")
    @Test
    void moveNoDestExistFolder_Then_Success(){
        // given
        String sourcePath = "testFolder1/testFolder2/testFolder3/";
        String destinationPath = "testFolder4/testFolder5/testFolder6/";
        String filename = "testFile.txt";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath));

        Path source = Paths.get(storageLocation, USERNAME, sourcePath);
        assertTrue(Files.exists(source));

        // 파일 만들기
        Path file = Paths.get(source.toString(),filename);
        try {
            Files.createFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Path dest = Paths.get(storageLocation, USERNAME, destinationPath);

        // when
        assertTrue(LocalStorageService.move(USERNAME, file, dest));

        // then
        assertFalse(Files.exists(file));
        assertTrue(Files.exists(dest));

        Path destFilePath = Paths.get(dest.toString(), filename);
        assertTrue(Files.exists(destFilePath));

    }
    @DisplayName("실패: 존재하지 않는 파일 이동")
    @Test
    void noExistFileMoveTest(){
        String sourcePath = "/";
        String destinationPath = "testFolder/";
        String filename = "testFile.txt";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath));

        Path destination = Paths.get(storageLocation, USERNAME, destinationPath);
        assertTrue(LocalStorageService.createFolder(USERNAME, destinationPath));

        Path file = Paths.get(storageLocation, USERNAME, filename);

        assertFalse(LocalStorageService.move(USERNAME, file, destination));
    }

    @DisplayName("성공: 단일 폴더 이동 하기 (폴더 이동)")
    @Test
    void moveFolderTest(){
        // given
        String sourcePath = "testFolder1/";
        String destinationPath = "testFolder2/";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath));
        assertTrue(LocalStorageService.createFolder(USERNAME, destinationPath));

        Path source = Paths.get(storageLocation, USERNAME, sourcePath);
        Path dest = Paths.get(storageLocation, USERNAME, destinationPath);
        assertTrue(Files.exists(source));
        assertTrue(Files.exists(dest));

        //when
        assertTrue(LocalStorageService.move(USERNAME, source, dest));

        // then
        assertFalse(Files.exists(source));
        assertTrue(Files.exists(dest));

        Path destPath = Paths.get(dest.toString(), sourcePath);
        assertTrue(Files.exists(destPath));
    }

    @DisplayName("성공: 3중 이상 폴더일 경우 이동")
    @Test
    void moveSubFolderTest(){
        // given
        String origin = "testFolder1/testFolder2/testFolder3/";
        Path sourcePath = Paths.get(origin);
        String destinationPath = "testFolder4";

        assertTrue(LocalStorageService.createFolder(USERNAME, sourcePath.toString()));
        assertTrue(LocalStorageService.createFolder(USERNAME, destinationPath));

        Path source = Paths.get(storageLocation, USERNAME, sourcePath.toString());
        Path dest = Paths.get(storageLocation, USERNAME, destinationPath);
        assertTrue(Files.exists(source));
        assertTrue(Files.exists(dest));

        Path testFolder5 = Paths.get(sourcePath.toString(), "testFolder5");
        Path testFolder6 = Paths.get(testFolder5.toString(), "testFolder6");

        assertTrue(LocalStorageService.createFolder(USERNAME, testFolder5.toString()));
        assertTrue(LocalStorageService.createFolder(USERNAME, testFolder6.toString()));

        //when
        assertTrue(LocalStorageService.move(USERNAME, source, dest));

        // then
        assertFalse(Files.exists(source));
        assertTrue(Files.exists(dest));

        Path destFolder6 = Paths.get(dest.toString(), "testFolder3", "testFolder5", "testFolder6");
        assertTrue(Files.exists(destFolder6));
        assertTrue(Files.exists(destFolder6.getParent()));
        assertTrue(Files.exists(destFolder6.getParent().getParent()));
    }
}
