package triggers;

import util.ValidNativeKeys;

public class KeyTrigger extends Trigger {

    public KeyTrigger(String key) {
        super(key);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.KEYPRESS;
    }

    public static boolean testValue(String value) {
        return ValidNativeKeys.isNativeKey(value);
    }
}
