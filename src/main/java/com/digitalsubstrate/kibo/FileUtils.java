// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.kibo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class FileUtils {

    private FileUtils() {
    }

    public static void createDirectoryForFile(Path path) throws IOException {
        if (path.getParent() != null)
            Files.createDirectories(path.getParent());
    }

    public static void emptyOrCreateDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            File directory = path.toFile();
            for (File file : Objects.requireNonNull(directory.listFiles()))
                if (file.isDirectory())
                    deleteDirectory(file);
                else
                    file.delete();

        } else {
            Files.createDirectories(path);
        }
    }

    public static boolean deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }

    public static void saveSource(String code, Path folder, Path filename) throws Exception {
        saveSource(code, Paths.get(folder.toString(), filename.toString()));
    }

    public static void saveSource(String code, Path path) throws Exception {
        var output = new PrintWriter(path.toString());
        output.write(code);
        output.close();
    }

    public static String userDirectory() throws Exception {
        return new File(System.getProperty("user.dir") + "/..").getCanonicalPath();
    }

}
