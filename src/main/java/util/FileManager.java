package util;

import extension.GTrigger;
import extension.GTriggerLauncher;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import overview.TriggerReactionEntry;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class FileManager {
    public static final String dir;

    static {
        String tryDir;
        try {
            tryDir = new File(GTriggerLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            tryDir = null;
        }
        dir = tryDir;
    }

    public static void saveEntriesToFile(List<TriggerReactionEntry> entries) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save G-Trigger file");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("G-Trigger file (*.gTrig)", "*.gTrig"));
            fileChooser.setInitialDirectory(new File(dir));

            File file = fileChooser.showSaveDialog(GTrigger.primaryStage);

            FileOutputStream fileStream = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileStream);

            out.writeObject(entries);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Error while saving file");
            errorAlert.showAndWait();
        }
    }

    public static Set<TriggerReactionEntry> loadEntriesFromFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load G-Trigger file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("G-Trigger file (*.gTrig)", "*.gTrig"));
        fileChooser.setInitialDirectory(new File(dir));

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(GTrigger.primaryStage);

        if(selectedFiles == null) {
            return new TreeSet<>();
        }

        return selectedFiles
                .stream()
                .flatMap(file -> getEntriesFromFile(file).stream())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static List<TriggerReactionEntry> getEntriesFromFile(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileInputStream);

            return (List<TriggerReactionEntry>) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Error while loading file");
            errorAlert.setContentText(String.format("Couldn't load in file: %s", file.getName()));
            errorAlert.showAndWait();

            return new ArrayList<>();
        }
    }
}
