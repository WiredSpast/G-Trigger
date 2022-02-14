package extension;

import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormCreator;
import gearth.extensions.ExtensionFormLauncher;
import gearth.extensions.ExtensionInfo;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GTriggerLauncher extends ExtensionFormCreator {
    @Override
    protected ExtensionForm createForm(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gtrigger.fxml"));
        Parent root = loader.load();
        GTrigger controller = loader.getController();

        stage.setTitle("G-Trigger " + controller.getClass().getAnnotation(ExtensionInfo.class).Version());
        stage.setScene(new Scene(root));
        stage.getScene().getStylesheets().add(Objects.requireNonNull(ExtensionFormCreator.class.getResource("/gearth/ui/bootstrap3.css")).toExternalForm());
        stage.getIcons().add(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/G-EarthLogoSmaller.png"))));
        stage.setResizable(false);
        stage.sizeToScene();

        GTrigger.primaryStage = stage;

        return controller;
    }

    public static void main(String[] args) {
        runExtensionForm(args, GTriggerLauncher.class);
    }
}
