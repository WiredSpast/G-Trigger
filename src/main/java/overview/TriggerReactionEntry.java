package overview;

import extension.GTrigger;
import gearth.extensions.ExtensionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.json.JSONObject;
import reactions.Reaction;
import reactions.ReactionType;
import triggers.Trigger;
import triggers.TriggerType;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriggerReactionEntry {
    protected static Long IdCounter = 0L;

    private Long id;
    private AtomicBoolean active;
    private AtomicBoolean consumed;
    private Trigger<?> trigger;
    private Reaction reaction;
    private String description;
    private int delay;
    private final Timer timer = new Timer();

    private boolean valid = true;

    private TableRow<TriggerReactionEntry> row;

    public TriggerReactionEntry(Trigger<?> trigger, Reaction reaction, String description, int delay) {
        this.id = TriggerReactionEntry.IdCounter++;
        this.trigger = trigger;
        this.reaction = reaction;
        this.description = description;
        this.delay = delay;
        this.active = new AtomicBoolean(true);
        this.consumed = new AtomicBoolean(false);
    }

    public TriggerReactionEntry(JSONObject jsonObject) {
        try {
            this.active = new AtomicBoolean(jsonObject.getBoolean("active"));
            this.consumed = new AtomicBoolean(jsonObject.optBoolean("consumed", false));
            this.id = TriggerReactionEntry.IdCounter++;
            this.description = jsonObject.getString("description");
            this.delay = jsonObject.getInt("delay");
            JSONObject triggerJson = jsonObject.getJSONObject("trigger");
            this.trigger = TriggerType.valueOf(triggerJson.getString("type")).construct(triggerJson.getString("value"));
            JSONObject reactionJson = jsonObject.getJSONObject("reaction");
            this.reaction = ReactionType.valueOf(reactionJson.getString("type")).construct(reactionJson.getString("value"));
        } catch(Exception ignored) {
            this.valid = false;
        }
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
        return this.id;
    }

    public AtomicBoolean isActive() {
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

    public JSONObject getAsJSONObject() {
        return new JSONObject()
                .put("trigger", trigger.getAsJSONObject())
                .put("reaction", reaction.getAsJSONObject())
                .put("description", description)
                .put("delay", delay)
                .put("active", active.get())
                .put("consumed", consumed.get());
    }

    public boolean isValid() {
        return this.valid;
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
}
