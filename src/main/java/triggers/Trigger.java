package triggers;

import org.json.JSONObject;

public abstract class Trigger {
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

    public JSONObject getAsJSONObject() {
        return new JSONObject()
                .put("type", getType())
                .put("value", getValue());
    }

    public static boolean testValue(String value) {
        return false;
    }
}