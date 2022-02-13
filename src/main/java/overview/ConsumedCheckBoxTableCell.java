package overview;

import triggers.KeyTrigger;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConsumedCheckBoxTableCell extends AtomicCheckBoxTableCell {
    @Override
    public void updateItem(AtomicBoolean item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            if (getTableView().getItems().get(getIndex()).getTrigger() instanceof KeyTrigger) {
                getGraphic().setDisable(true);
                getGraphic().setStyle("-fx-opacity: 0.3;");
            }
        }
    }
}
