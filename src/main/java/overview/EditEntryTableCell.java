package overview;

import extension.GTrigger;
import gearth.extensions.ExtensionFormLauncher;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import util.EditingMode;

import java.util.Objects;

public class EditEntryTableCell extends TableCell<TriggerReactionEntry, Long> {
    private final ImageView edit = new ImageView(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonEdit.png"))));
    private final ImageView delete = new ImageView(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonDelete.png"))));

    @Override
    protected void updateItem(Long id, boolean empty) {
        super.updateItem(id, empty);

        if(id == null || empty) {
            setGraphic(null);
            return;
        }

        edit.setOnMouseClicked(event -> System.out.println("edit" + id));
        edit.setOnMouseEntered(event -> edit.setImage(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonEditHover.png")))));
        edit.setOnMouseExited(event -> edit.setImage(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonEdit.png")))));
        edit.setOnMouseClicked(event -> {
            EditingMode.editingId = EditingMode.editingId == id ? -1 : id;
            EditingMode.propped = false;
            synchronized (GTrigger.entryLock) {
                getTableView().getItems().set(getIndex(), getTableView().getItems().get(getIndex()));
            }
        });

        delete.setOnMouseClicked(event -> System.out.println("delete" + id));
        delete.setOnMouseEntered(event -> delete.setImage(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonDeleteHover.png")))));
        delete.setOnMouseExited(event -> delete.setImage(new Image(Objects.requireNonNull(ExtensionFormLauncher.class.getResourceAsStream("/gearth/ui/files/ButtonDelete.png")))));
        delete.setOnMouseClicked(event -> {
            if(EditingMode.editingId == id) {
                EditingMode.editingId = -1;
            }

            synchronized (GTrigger.entryLock) {
                getTableView().getItems().removeIf(entry -> entry.getId().equals(id));
            }
        });

        HBox graphic = new HBox(edit, delete);
        graphic.setAlignment(Pos.CENTER);
        graphic.setSpacing(10);
        setGraphic(graphic);
    }
}
