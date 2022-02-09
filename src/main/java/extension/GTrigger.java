package extension;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.json.JSONObject;
import reactions.Reaction;
import reactions.ReactionType;
import triggers.*;
import overview.AtomicCheckBoxTableCell;
import overview.EditEntryTableCell;
import overview.TriggerReactionEntry;
import util.EditingMode;
import utils.Cacher;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@ExtensionInfo(
        Title = "G-Trigger",
        Description = "Send packets on trigger events",
        Version = "0.1",
        Author = "WiredSpast"
)
public class GTrigger extends ExtensionForm implements NativeKeyListener {
    public TableView<TriggerReactionEntry> entryOverview;
    public ComboBox<TriggerType> triggerTypeBox;
    public ComboBox<ReactionType> reactionTypeBox;
    public Spinner<Integer> delaySpinner;
    public TextField descriptionBox, triggerValueBox, reactionValueBox;
    public Button addButton;

    public EditingMode editingMode = EditingMode.Add;
    public final static Object entryLock = new Object();

    @Override
    protected void initExtension() {
        intercept(HMessage.Direction.TOCLIENT, this::onPacketToClient);
        intercept(HMessage.Direction.TOSERVER, this::onPacketToServer);

        intercept(HMessage.Direction.TOCLIENT, "Chat", this::onChatIn);
        intercept(HMessage.Direction.TOSERVER, "Chat", this::onChatOut);

        setupChangeListener();
        setupKeyListener();
        setupGUI();
        setupColumns();
        loadInCache();
    }

    private void onChatOut(HMessage hMessage) {
        synchronized (GTrigger.entryLock) {
            List<TriggerReactionEntry> youSayCommandEntries = entryOverview
                    .getItems()
                    .filtered(entry -> entry.isActive().get()
                            && entry.getTrigger() instanceof YouSayCommandTrigger);

            for (TriggerReactionEntry entry : youSayCommandEntries) {
                if (hMessage.getPacket().readString().equals(entry.getTrigger().getValue())) {
                    entry.triggerReaction(this);
                }
            }
        }
    }

    private void onChatIn(HMessage hMessage) {
        hMessage.getPacket().readInteger();
        synchronized (GTrigger.entryLock) {
            List<TriggerReactionEntry> someoneSaysCommandEntries = entryOverview
                    .getItems()
                    .filtered(entry -> entry.isActive().get()
                            && entry.getTrigger() instanceof AnyoneSaysCommandTrigger);

            for (TriggerReactionEntry entry : someoneSaysCommandEntries) {
                if (hMessage.getPacket().readString().equals(entry.getTrigger().getValue())) {
                    entry.triggerReaction(this);
                }
            }
        }
    }

    private void onPacketToClient(HMessage hMessage) {
        synchronized (GTrigger.entryLock) {
            List<TriggerReactionEntry> packetToClientEntries = entryOverview
                    .getItems()
                    .filtered(entry -> entry.isActive().get()
                            && entry.getTrigger() instanceof PacketToClientTrigger);

            for (TriggerReactionEntry entry : packetToClientEntries) {
                if (((PacketTrigger) entry.getTrigger())
                        .getCompletedPacket(getPacketInfoManager())
                        .equals(hMessage.getPacket())) {
                    entry.triggerReaction(this);
                }
            }
        }
    }

    private void onPacketToServer(HMessage hMessage) {
        synchronized (GTrigger.entryLock) {
            List<TriggerReactionEntry> packetToServerEntries = entryOverview
                    .getItems()
                    .filtered(entry -> entry.isActive().get()
                            && entry.getTrigger() instanceof PacketToServerTrigger);

            for (TriggerReactionEntry entry : packetToServerEntries) {
                if (((PacketTrigger) entry.getTrigger())
                        .getCompletedPacket(getPacketInfoManager())
                        .equals(hMessage.getPacket())) {
                    entry.triggerReaction(this);
                }
            }
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if(triggerValueBox.isFocused()) {
            if(triggerTypeBox.getValue() == TriggerType.KEYPRESS) {
                triggerValueBox.setText(NativeKeyEvent.getKeyText(event.getKeyCode()));
            }
        } else {
            synchronized (GTrigger.entryLock) {
                List<TriggerReactionEntry> keyPressedEntries = entryOverview
                        .getItems()
                        .filtered(entry -> entry.isActive().get()
                                && entry.getTrigger() instanceof KeyTrigger);

                for (TriggerReactionEntry entry : keyPressedEntries) {
                    if (NativeKeyEvent.getKeyText(event.getKeyCode()).equals(entry.getTrigger().getValue())) {
                        entry.triggerReaction(this);
                    }
                }
            }
        }
    }

    public void clearAll(ActionEvent event) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems().clear();
        }
    }

    private void setupGUI() {
        entryOverview.setSelectionModel(null);

        delaySpinner.getEditor().setOnKeyTyped(e -> {
            if(!"0123456789".contains(e.getCharacter())) e.consume();
        });

        triggerTypeBox.getItems().addAll(TriggerType.values());
        triggerTypeBox.setValue(TriggerType.PACKETTOSERVER);
        triggerTypeBox.valueProperty().addListener(this::evaluateTrigger);

        reactionTypeBox.getItems().addAll(ReactionType.values());
        reactionTypeBox.setValue(ReactionType.PACKETTOCLIENT);
        reactionTypeBox.valueProperty().addListener(this::evaluateReaction);

        triggerValueBox.setOnKeyPressed(e -> {
            if(triggerTypeBox.getValue() == TriggerType.KEYPRESS) e.consume();
        });
        triggerValueBox.setOnKeyTyped(e -> {
            if(triggerTypeBox.getValue() == TriggerType.KEYPRESS) e.consume();
        });
        triggerValueBox.textProperty().addListener(this::evaluateTrigger);

        reactionValueBox.textProperty().addListener(this::evaluateReaction);
    }

    private void evaluateTrigger(Observable e) {
        if(triggerValueBox.getText().trim().isEmpty()) {
            triggerValueBox.setStyle("");
        } else if(triggerTypeBox.getValue().testValue(triggerValueBox.getText())) {
            triggerValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, lime, 5, 0, 0, 0);");
        } else {
            triggerValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, red, 5, 0, 0, 0);");
        }

        evaluateButton();
    }

    private void evaluateReaction(Observable e) {
        if(reactionValueBox.getText().trim().isEmpty()) {
            reactionValueBox.setStyle("");
        } else if(reactionTypeBox.getValue().testValue(reactionValueBox.getText())) {
            reactionValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, lime, 5, 0, 0, 0);");
        } else {
            reactionValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, red, 5, 0, 0, 0);");
        }

        evaluateButton();
    }

    private void evaluateButton() {
        addButton.setDisable(reactionValueBox.getText().trim().isEmpty() || !reactionTypeBox.getValue().testValue(reactionValueBox.getText()) ||
                             triggerValueBox.getText().trim().isEmpty() || !triggerTypeBox.getValue().testValue(triggerValueBox.getText()));
    }

    private void setupChangeListener() {
        entryOverview.getItems().addListener((ListChangeListener<TriggerReactionEntry>) c -> {
            synchronized (GTrigger.entryLock) {
                Cacher.updateCache(new JSONObject().put("entries", entryOverview.getItems().stream().map(TriggerReactionEntry::getAsJSONObject).toArray()).toString(2), "entries.json");
                entryOverview.getItems().forEach(TriggerReactionEntry::deselect);

                if(entryOverview.getItems().stream().noneMatch(item -> item.getId() == EditingMode.editingId)) {
                    setAddMode();
                } else {
                    entryOverview.getItems().stream().filter(item -> item.getId() == EditingMode.editingId).forEach(this::setEditMode);
                }
            }
        });
    }

    public void editOrAddEntry(ActionEvent event) {
        Trigger trigger = triggerTypeBox.getValue().construct(triggerValueBox.getText());
        Reaction reaction = reactionTypeBox.getValue().construct(reactionValueBox.getText());
        TriggerReactionEntry newEntry = new TriggerReactionEntry(trigger, reaction, descriptionBox.getText(), delaySpinner.getValue());
        switch(editingMode) {
            case Add:
                addEntry(newEntry);
                break;
            case Edit:
                entryOverview.getItems().stream().filter(entry -> entry.getId() == EditingMode.editingId).forEach(entry -> {
                    EditingMode.editingId = -1;
                    entry.deselect();
                    entryOverview.getItems().set(entryOverview.getItems().indexOf(entry), newEntry);
                });
                break;
        }

        EditingMode.editingId = -1;
        editingMode = EditingMode.Add;
    }

    private void setAddMode() {
        EditingMode.editingId = -1;
        editingMode = EditingMode.Add;
        addButton.setText(editingMode.toString());
    }

    private void setEditMode(TriggerReactionEntry entry) {
        editingMode = EditingMode.Edit;
        addButton.setText(editingMode.toString());
        entry.select();
        if(!EditingMode.propped || entry.getId() != EditingMode.editingId) {
            EditingMode.editingId = entry.getId();
            EditingMode.propped = true;
            triggerTypeBox.setValue(entry.getTrigger().getType());
            triggerValueBox.setText(entry.getTrigger().getValue());
            reactionTypeBox.setValue(entry.getReaction().getType());
            reactionValueBox.setText(entry.getReaction().getValue());
            descriptionBox.setText(entry.getDescription());
            delaySpinner.getValueFactory().setValue(entry.getDelay());
        }
    }

    private void setupKeyListener() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(e.getMessage());
        }
    }

    private void setupColumns() {
        TableColumn<TriggerReactionEntry, AtomicBoolean> activeColumn = createColumn("Active", "active", 48);
        activeColumn.setCellFactory(tc -> new AtomicCheckBoxTableCell());
        entryOverview.getColumns().add(activeColumn);

        entryOverview.getColumns().add(createColumn("Trigger", "triggerDescription", 200));

        entryOverview.getColumns().add(createColumn("Reaction", "reactionDescription", 200));

        entryOverview.getColumns().add(createColumn("Description", "description", 200));

        TableColumn<TriggerReactionEntry, Integer> delayColumn = createColumn("Delay", "delay", 50);
        delayColumn.setStyle( "-fx-alignment: CENTER;");
        entryOverview.getColumns().add(delayColumn);

        TableColumn<TriggerReactionEntry, Long> editColumn = createColumn("Edit", "id", 50);
        editColumn.setCellFactory(tc -> new EditEntryTableCell());
        entryOverview.getColumns().add(editColumn);
    }

    private void loadInCache() {
        if (Cacher.cacheFileExists("entries.json")) {
            try {
                JSONObject cache = Cacher.getCacheContents("entries.json");
                cache.getJSONArray("entries")
                        .toList()
                        .stream()
                        .map(o -> (HashMap<String, Object>) o)
                        .map(JSONObject::new)
                        .map(TriggerReactionEntry::new)
                        .filter(TriggerReactionEntry::isValid)
                        .forEach(this::addEntry);
            } catch (Exception ignored){}
        }
    }

    private void addEntry(TriggerReactionEntry entry) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems().add(entry);
        }
    }

    private<T> TableColumn<TriggerReactionEntry, T> createColumn(String columnName, String propertyName, double width) {
        TableColumn<TriggerReactionEntry, T> column = new TableColumn<>(columnName);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setResizable(false);
        column.setPrefWidth(width);
        column.setSortable(false);
        column.impl_setReorderable(false);

        return column;
    }
}
