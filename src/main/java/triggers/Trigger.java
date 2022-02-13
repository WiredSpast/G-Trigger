package triggers;

import reactions.Reaction;
import util.ComparisonResult;

import java.io.Serializable;

public abstract class Trigger<T> implements Serializable {
    static final long serialVersionUID = 956165194645L;

    private String value;

    public Trigger(String value) {
        this.value = value;
    }

    public abstract TriggerType getType();

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getType().desc + " - " + getValue();
    }

    public static boolean testValue(String value) {
        return false;
    }

    public abstract ComparisonResult compare(T value);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Trigger)) return false;
        Trigger<?> trigger = (Trigger<?>) o;
        return this.value.equals(trigger.value)
                && this.getType() == trigger.getType();
    }
}
