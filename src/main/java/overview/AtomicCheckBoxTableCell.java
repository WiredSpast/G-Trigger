package overview;

import extension.GTrigger;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtomicCheckBoxTableCell extends CheckBoxTableCell<TriggerReactionEntry, AtomicBoolean> {
    @Override
    public void updateItem(AtomicBoolean item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null)
            setGraphic(null);
        else {
            CheckBox cb = new CheckBox();
            setGraphic(cb);
            cb.setSelected(item.get());
            cb.selectedProperty().addListener((observable, oldValue, newValue) -> {
                item.set(newValue);
                getTableView().refresh();
            });
        }
    }
}
