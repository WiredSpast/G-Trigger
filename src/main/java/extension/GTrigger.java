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
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import reactions.*;
import triggers.*;
import overview.*;
import util.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@ExtensionInfo(
        Title = "G-Trigger",
        Description = "Send packets on trigger events",
        Version = "0.2.4",
        Author = "WiredSpast"
)
public class GTrigger extends ExtensionForm implements NativeKeyListener {
    public static Stage primaryStage;

    public TableView<TriggerReactionEntry> entryOverview;
    public ComboBox<TriggerType> triggerTypeBox;
    public ComboBox<ReactionType> reactionTypeBox;
    public TextField delayBox, descriptionBox, triggerValueBox, reactionValueBox;
    public Button addButton, saveButton, loadButton;

    public CheckMenuItem chkAlwaysOnTop;

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
    }

    private void onChatOut(HMessage hMessage) {
        onChat(hMessage, hMessage.getPacket().readString(StandardCharsets.UTF_8), YouSayCommandTrigger.class);
    }

    private void onChatIn(HMessage hMessage) {
        hMessage.getPacket().readInteger();
        onChat(hMessage, hMessage.getPacket().readString(StandardCharsets.UTF_8), AnyoneSaysCommandTrigger.class);
    }

    private<T extends CommandSaidTrigger> void onChat(HMessage hMessage, String chatMsg, Class<T> triggerClass) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems()
                    .filtered(entry -> entry.isActive().get())
                    .filtered(entry -> triggerClass.isInstance(entry.getTrigger()))
                    .forEach(entry -> {
                        CommandSaidTrigger trigger = entry.getTrigger();
                        ComparisonResult res = trigger.compare(chatMsg);

                        if (res.isValid()) {
                            entry.triggerReaction(this, res.getVariables());

                            if (entry.consumesTrigger()) {
                                hMessage.setBlocked(true);
                            }
                        }
                    });
        }
    }

    private void onPacketToClient(HMessage hMessage) {
        onPacket(hMessage, PacketToClientTrigger.class);
    }

    private void onPacketToServer(HMessage hMessage) {
        onPacket(hMessage, PacketToServerTrigger.class);
    }

    private<T extends PacketTrigger> void onPacket(HMessage hMessage, Class<T> triggerClass) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems()
                    .filtered(entry -> entry.isActive().get())
                    .filtered(entry -> triggerClass.isInstance(entry.getTrigger()))
                    .filtered(entry -> {
                        PacketTrigger trigger = entry.getTrigger();
                        return trigger.getCompletedSimplifiedPacket(getPacketInfoManager())
                                .headerId() == hMessage.getPacket().headerId();
                    })
                    .forEach(entry -> {
                        PacketTrigger trigger = entry.getTrigger();
                        ComparisonResult res = trigger.compare(hMessage.getPacket());

                        if (res.isValid()) {
                            entry.triggerReaction(this, res.getVariables());

                            if (entry.consumesTrigger()) {
                                hMessage.setBlocked(true);
                            }
                        }
                    });
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent event) {
        if (triggerValueBox.isFocused()) {
            if(triggerTypeBox.getValue() == TriggerType.KEYPRESS) {
                triggerValueBox.setText(NativeKeyEvent.getKeyText(event.getKeyCode()));
            }
        } else {
            synchronized (GTrigger.entryLock) {
                entryOverview.getItems()
                        .filtered(entry -> entry.isActive().get())
                        .filtered(entry -> entry.getTrigger() instanceof KeyTrigger)
                        .filtered(entry -> ((KeyTrigger) entry.getTrigger()).compare(event).isValid())
                        .forEach(entry -> entry.triggerReaction(this, new HashMap<>()));
            }
        }
    }

    private void setupGUI() {
        entryOverview.setSelectionModel(null);

        delayBox.setOnKeyTyped(e -> {
            if(!"0123456789".contains(e.getCharacter())) e.consume();
        });

        delayBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                delayBox.setText("0");
                return;
            }

            int numericValue = Integer.parseInt(newValue);
            if (numericValue > 9999) {
                delayBox.setText("9999");
            }
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
        } else if(reactionTypeBox.getValue().testValue(reactionValueBox.getText(), VariableUtil.findVariables(triggerValueBox.getText()))) {
            reactionValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, lime, 5, 0, 0, 0);");
        } else {
            reactionValueBox.setStyle("-fx-effect: dropshadow(three-pass-box, red, 5, 0, 0, 0);");
        }

        evaluateButton();
    }

    private void evaluateButton() {
        addButton.setDisable(reactionValueBox.getText().trim().isEmpty() || !reactionTypeBox.getValue().testValue(reactionValueBox.getText(), VariableUtil.findVariables(triggerValueBox.getText())) ||
                             triggerValueBox.getText().trim().isEmpty() || !triggerTypeBox.getValue().testValue(triggerValueBox.getText()));
    }

    private void setupChangeListener() {
        entryOverview.getItems().addListener((ListChangeListener<TriggerReactionEntry>) c -> {
            synchronized (GTrigger.entryLock) {
                entryOverview.getItems().forEach(TriggerReactionEntry::deselect);

                if(entryOverview.getItems().stream().noneMatch(item -> item.getId() == EditingMode.editingId)) {
                    setAddMode();
                } else {
                    entryOverview.getItems().stream().filter(item -> item.getId() == EditingMode.editingId).forEach(this::setEditMode);
                }

                saveButton.setDisable(entryOverview.getItems().isEmpty());
            }
        });
    }

    public void editOrAddEntry(ActionEvent ignoredEvent) {
        Trigger<?> trigger = triggerTypeBox.getValue().construct(triggerValueBox.getText());
        Reaction reaction = reactionTypeBox.getValue().construct(reactionValueBox.getText());
        switch(editingMode) {
            case Add:
                addEntry(new TriggerReactionEntry(trigger, reaction, descriptionBox.getText(), Integer.parseInt(delayBox.getText())));
                break;
            case Edit:
                entryOverview.getItems().stream().filter(entry -> entry.getId() == EditingMode.editingId).forEach(entry -> {
                    EditingMode.editingId = -1;
                    entry.deselect();
                    entryOverview.getItems().set(
                                    entryOverview.getItems().indexOf(entry),
                                    new TriggerReactionEntry(trigger, reaction, descriptionBox.getText(), Integer.parseInt(delayBox.getText()), entry.isActive().get(), entry.isConsumed().get())
                    );
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
            delayBox.setText("" + entry.getDelay());
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
        TableColumn<TriggerReactionEntry, AtomicBoolean> activeColumn = createColumn("Active", "active", 58);
        activeColumn.setCellFactory(tc -> new AtomicCheckBoxTableCell());
        entryOverview.getColumns().add(activeColumn);

        TableColumn<TriggerReactionEntry, AtomicBoolean> consumedColumn = createColumn("Consume", "consumed", 60);
        consumedColumn.setCellFactory(tc -> new ConsumedCheckBoxTableCell());
        entryOverview.getColumns().add(consumedColumn);

        entryOverview.getColumns().add(createColumn("Trigger", "triggerDescription", 180));

        entryOverview.getColumns().add(createColumn("Reaction", "reactionDescription", 180));

        entryOverview.getColumns().add(createColumn("Description", "description", 180));

        TableColumn<TriggerReactionEntry, Integer> delayColumn = createColumn("Delay", "delay", 40);
        delayColumn.setStyle( "-fx-alignment: CENTER;");
        entryOverview.getColumns().add(delayColumn);

        TableColumn<TriggerReactionEntry, Long> editColumn = createColumn("Edit", "id", 50);
        editColumn.setCellFactory(tc -> new EditEntryTableCell());
        entryOverview.getColumns().add(editColumn);
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

    public void saveEntries(ActionEvent ignoredEvent) {
        synchronized (GTrigger.entryLock) {
            FileManager.saveEntriesToFile(new ArrayList<>(entryOverview.getItems()));
        }
    }

    public void loadEntriesFromFiles(ActionEvent ignoredEvent) {
        synchronized (GTrigger.entryLock) {
            Set<TriggerReactionEntry> entries = FileManager.loadEntriesFromFiles();
            if (!entries.isEmpty()) {
                entryOverview.getItems().clear();
                entryOverview.getItems().addAll(entries);
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("No valid entries found on load from file(s)");
                errorAlert.showAndWait();
            }
        }
    }

    public void onFileOver(DragEvent dragEvent) {
        if (dragEvent.getDragboard().hasFiles()) {
            if (dragEvent.getDragboard().getFiles().stream().allMatch(f -> f.getName().endsWith(".gTrig"))) {
                dragEvent.acceptTransferModes(TransferMode.COPY);
            }
        }
        dragEvent.consume();
    }

    public void onFileDrop(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            entryOverview.getItems()
                    .addAll(db.getFiles()
                            .stream()
                            .flatMap(file -> FileManager.getEntriesFromFile(file).stream())
                            .collect(Collectors.toCollection(HashSet::new))
                    );
            success = true;
        }

        dragEvent.setDropCompleted(success);
    }

    public void toggleAlwaysOnTop(ActionEvent ignoredEvent) {
        GTrigger.primaryStage.setAlwaysOnTop(chkAlwaysOnTop.isSelected());
    }

    public void manipulateEntries(Class<? extends Trigger> triggerClass, Consumer<TriggerReactionEntry> manipulation) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems()
                    .filtered(entry -> triggerClass.isInstance(entry.getTrigger()))
                    .forEach(manipulation);

            entryOverview.refresh();
        }
    }

    public void removeEntries(Class<? extends Trigger> triggerClass) {
        synchronized (GTrigger.entryLock) {
            entryOverview.getItems()
                    .removeIf(entry -> triggerClass.isInstance(entry.getTrigger()));
        }
    }

    public void disableAllEntries(ActionEvent ignoredEvent) {
        manipulateEntries(Trigger.class, entry -> entry.setActive(false));
    }

    public void enableAllEntries(ActionEvent ignoredEvent) {
        manipulateEntries(Trigger.class, entry -> entry.setActive(true));
    }

    public void removeAllEntries(ActionEvent ignoredEvent) {
        removeEntries(Trigger.class);
    }

    public void disableAllKeyTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(KeyTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllKeyTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(KeyTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllKeyTriggers(ActionEvent ignoredEvent) {
        removeEntries(KeyTrigger.class);
    }

    public void disableAllPacketTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllPacketTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllPacketTriggers(ActionEvent ignoredEvent) {
        removeEntries(PacketTrigger.class);
    }

    public void disableAllPacketToServerTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketToServerTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllPacketToServerTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketToServerTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllPacketToServerTriggers(ActionEvent ignoredEvent) {
        removeEntries(PacketToServerTrigger.class);
    }

    public void disableAllPacketToClientTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketToClientTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllPacketToClientTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(PacketToClientTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllPacketToClientTriggers(ActionEvent ignoredEvent) {
        removeEntries(PacketToClientTrigger.class);
    }

    public void disableAllCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(CommandSaidTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(CommandSaidTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllCommandTriggers(ActionEvent ignoredEvent) {
        removeEntries(CommandSaidTrigger.class);
    }

    public void disableAllYouSayCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(YouSayCommandTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllYouSayCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(YouSayCommandTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllYouSayCommandTriggers(ActionEvent ignoredEvent) {
        removeEntries(YouSayCommandTrigger.class);
    }

    public void disableAllAnyoneSaysCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(AnyoneSaysCommandTrigger.class, entry -> entry.setActive(false));
    }

    public void enableAllAnyoneSaysCommandTriggers(ActionEvent ignoredEvent) {
        manipulateEntries(AnyoneSaysCommandTrigger.class, entry -> entry.setActive(true));
    }

    public void removeAllAnyoneSaysCommandTriggers(ActionEvent ignoredEvent) {
        removeEntries(AnyoneSaysCommandTrigger.class);
    }
}
