package inshining.virtualstorage.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileDeletor {
    public static void delete(Path path, int depth) {
        if (depth <= 0) {
            return;
        }
        try {
            Files.delete(path);
            if (depth > 0) {
                delete(path.getParent(), depth - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
