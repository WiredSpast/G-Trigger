package reactions;

import gearth.extensions.ExtensionBase;
import org.json.JSONObject;

public abstract class Reaction {
    private final String value;

    public Reaction(String value) {
        this.value = value;
    }

    public abstract ReactionType getType();

    public String getValue() {
        return value;
    }

    public abstract void doReaction(ExtensionBase ext);

    @Override
    public String toString() {
        return getType().desc + " - " + getValue();
    }

    public JSONObject getAsJSONObject() {
        return new JSONObject()
                .put("type", getType())
                .put("value", getValue());
    }
}
