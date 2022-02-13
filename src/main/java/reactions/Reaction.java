package reactions;

import gearth.extensions.ExtensionBase;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public abstract class Reaction {
    private final String value;

    public Reaction(String value) {
        this.value = value;
    }

    public abstract ReactionType getType();

    public String getValue() {
        return value;
    }

    public String getCompletedValue(HashMap<String, String> variables) {
        String val = this.getValue();
        for (String varName : variables.keySet()) {
            while (val.contains(varName)) {
                val = val.replace(varName, variables.get(varName));
            }
        }

        return val;
    }

    public abstract void doReaction(ExtensionBase ext, HashMap<String, String> variables);

    public static boolean testValue(String value, List<String> variables) {
        return false;
    }

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
