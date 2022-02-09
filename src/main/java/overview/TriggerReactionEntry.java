package overview;

import gearth.extensions.ExtensionBase;
import javafx.scene.control.TableRow;
import org.json.JSONObject;
import reactions.Reaction;
import reactions.ReactionType;
import triggers.Trigger;
import triggers.TriggerType;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class TriggerReactionEntry {
    protected static Long IdCounter = 0L;

    private Long id;
    private AtomicBoolean active;
    private Trigger trigger;
    private Reaction reaction;
    private String description;
    private int delay;
    private final Timer timer = new Timer();

    private boolean valid = true;

    private TableRow<TriggerReactionEntry> row;

    public TriggerReactionEntry(Trigger trigger, Reaction reaction, String description, int delay) {
        this.id = TriggerReactionEntry.IdCounter++;
        this.trigger = trigger;
        this.reaction = reaction;
        this.description = description;
        this.delay = delay;
        this.active = new AtomicBoolean(true);
    }

    public TriggerReactionEntry(JSONObject jsonObject) {
        try {
            this.active = new AtomicBoolean(jsonObject.getBoolean("active"));
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

    public Trigger getTrigger() {
        return this.trigger;
    }

    public Reaction getReaction() {
        return this.reaction;
    }

    public void triggerReaction(ExtensionBase ext) {
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getReaction().doReaction(ext);
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
                .put("active", active.get());
    }

    public boolean isValid() {
        return this.valid;
    }
}
