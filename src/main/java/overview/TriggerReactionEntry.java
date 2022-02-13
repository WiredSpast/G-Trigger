package overview;

import gearth.extensions.ExtensionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import reactions.Reaction;
import triggers.Trigger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriggerReactionEntry implements Serializable, Comparable<TriggerReactionEntry> {
    static final long serialVersionUID = 956165194645L;

    protected static Long IdCounter = 0L;

    private transient Long id;
    private transient AtomicBoolean active = new AtomicBoolean(false);
    private final AtomicBoolean consumed;
    private final Trigger<?> trigger;
    private final Reaction reaction;
    private final String description;
    private final int delay;
    private transient final Timer timer = new Timer();


    private transient TableRow<TriggerReactionEntry> row;

    public TriggerReactionEntry(Trigger<?> trigger, Reaction reaction, String description, int delay) {
        this.id = TriggerReactionEntry.IdCounter++;
        this.trigger = trigger;
        this.reaction = reaction;
        this.description = description;
        this.delay = delay;
        this.active.set(true);
        this.consumed = new AtomicBoolean(false);
    }

    public TriggerReactionEntry(Trigger<?> trigger, Reaction reaction, String description, int delay, boolean active, boolean consumed) {
        this(trigger, reaction, description, delay);

        this.active.set(active);
        this.consumed.set(consumed);
    }

    public void setRow(TableRow<TriggerReactionEntry> row) {
        this.row = row;
    }

    public void deselect() {
        if(row != null) {
            row.setStyle("");
        }
    }

    public void select() {
        if(row != null) {
            row.setStyle("-fx-background-color: #faebcc;");
        }
    }

    public Long getId() {
        if (this.id == null) {
            this.id = TriggerReactionEntry.IdCounter++;
        }
        return this.id;
    }

    public AtomicBoolean isActive() {
        if (this.active == null) {
            this.active = new AtomicBoolean(false);
        }

        return this.active;
    }

    public<T extends Trigger<?>> T getTrigger() {
        return (T) this.trigger;
    }

    public<R extends Reaction> R getReaction() {
        return (R) this.reaction;
    }

    public void triggerReaction(ExtensionBase ext, HashMap<String, String> variables) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getReaction().doReaction(ext, variables);
            }
        }, this.delay);
    }

    public String getTriggerDescription() {
        return this.trigger.toString();
    }

    public String getReactionDescription() {
        return this.reaction.toString();
    }

    public String getDescription() {
        return this.description;
    }

    public int getDelay() {
        return this.delay;
    }

    public void setActive(boolean val) {
        this.active = new AtomicBoolean(val);
    }

    public AtomicBoolean isConsumed() {
        return this.consumed;
    }

    public boolean consumesTrigger() {
        return this.consumed.get();
    }

    public void reload(TableView<TriggerReactionEntry> entryOverview) {
        int index = entryOverview.getItems().indexOf(this);
        if(index != -1) {
            entryOverview.getItems().set(index, this);
        }
    }

    @Override
    public int compareTo(TriggerReactionEntry entry) {
        return this.trigger.equals(entry.trigger)
                && this.reaction.equals(entry.reaction)
                && this.delay == entry.delay
                && this.description.equals(entry.description) ? 0 : 1;
    }
}
