package utils;

import extension.GTriggerLauncher;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

public class Cacher {
    public static final String dir;

    static {
        String tryDir;
        try {
            tryDir = new File(GTriggerLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent() + "/cache";
        } catch (URISyntaxException e) {
            e.printStackTrace();
            tryDir = null;
        }
        dir = tryDir;
    }

    public static void updateCache(JSONObject contents, String cache_filename) {
        updateCache(contents.toString(), cache_filename);
    }

    public static void updateCache(String content, String filename) {
        File parent_dir = new File(dir);
        parent_dir.mkdirs();

        try (FileWriter file = new FileWriter(new File(dir, filename))) {

            file.write(content);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean cacheFileExists(String cache_filename) {
        File f = new File(dir, cache_filename);
        return (f.exists() && !f.isDirectory());
    }

    public static JSONObject getCacheContents(String cache_filename) {
        if (cacheFileExists(cache_filename)) {
            try {
                File f = new File(dir, cache_filename);
                String contents = String.join("\n", Files.readAllLines(f.toPath()));

                return new JSONObject(contents);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new JSONObject();
    }
}