package reactions;

import gearth.extensions.ExtensionBase;
import overview.TriggerReactionEntry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public abstract class Reaction implements Serializable {
    static final long serialVersionUID = 956165194645L;

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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reaction)) return false;
        Reaction reaction = (Reaction) o;
        return this.value.equals(reaction.value)
                && this.getType() == reaction.getType();
    }
}
