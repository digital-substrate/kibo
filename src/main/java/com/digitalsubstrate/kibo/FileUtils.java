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
        // Le chemin d'une sortie porte maintenant un répertoire pour les cibles dont un
        // module EST un répertoire. Le créer ici plutôt qu'à l'appel : il y a un seul
        // endroit où un fichier est écrit, et c'est le seul qui sait s'il en faut un.
        createDirectoryForFile(path);
        var output = new PrintWriter(path.toString());
        output.write(code);
        output.close();
    }

    public static String userDirectory() throws Exception {
        return new File(System.getProperty("user.dir") + "/..").getCanonicalPath();
    }

}
