package extension;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionFormCreator;
import gearth.extensions.ExtensionFormLauncher;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class GTriggerLauncher extends ExtensionFormCreator implements NativeKeyListener {
    @Override
    protected ExtensionForm createForm(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gtrigger.fxml"));
        Parent root = loader.load();

        GTrigger controller = loader.getController();
        VBox vb = new VBox(setupMenuBar(stage, controller), root);
        stage.setTitle("G-Trigger");
        stage.setScene(new Scene(vb));
        stage.getScene().getStylesheets().add(Objects.requireNonNull(ExtensionFormCreator.class.getResource("/gearth/ui/bootstrap3.css")).toExternalForm());
        stage.getIcons().add(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/G-EarthLogoSmaller.png"))));
        stage.setResizable(false);
        stage.sizeToScene();

        GTrigger.primaryStage = stage;

        return controller;
    }

    public MenuBar setupMenuBar(Stage stage, GTrigger controller) {
        MenuBar mb = new MenuBar();
        Menu options = new Menu("Window");

        CheckMenuItem alwaysOnTop = new CheckMenuItem("Always on top");
        alwaysOnTop.setOnAction(e -> stage.setAlwaysOnTop(alwaysOnTop.isSelected()));
        options.getItems().add(alwaysOnTop);

        MenuItem disableAll = new MenuItem("Disable all (Ctrl + D)");
        disableAll.setOnAction(e -> controller.disableAllEntries());
        disableAll.setAccelerator(new KeyCombination() {
            @Override
            public boolean match(KeyEvent event) {
                return event.isControlDown() && event.getCode() == KeyCode.D;
            }
        });
        options.getItems().add(disableAll);

        MenuItem enableAll = new MenuItem("_Enable all (Ctrl + E)");
        enableAll.setOnAction(e -> controller.enableAllEntries());
        options.getItems().add(enableAll);

        mb.getMenus().add(options);

        return mb;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        System.out.println(nativeEvent.paramString());
    }

    public static void main(String[] args) {
        runExtensionForm(args, GTriggerLauncher.class);
    }
}
