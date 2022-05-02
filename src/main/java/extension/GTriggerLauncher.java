package extension;

import gearth.extensions.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;

public class GTriggerLauncher extends ThemedExtensionFormCreator {
    @Override
    protected String getTitle() {
        return "G-Trigger " + GTrigger.class.getAnnotation(ExtensionInfo.class).Version();
    }

    @Override
    protected URL getFormResource() {
        return getClass().getResource("/gtrigger.fxml");
    }

    @Override
    protected void initialize(Stage primaryStage) {
        GTrigger.primaryStage = primaryStage;
        primaryStage.getScene().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
    }

    public static void main(String[] args) {
        runExtensionForm(args, GTriggerLauncher.class);
    }
}
