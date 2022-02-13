package triggers;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import util.ComparisonResult;
import util.ValidNativeKeys;

import java.util.HashMap;

public class KeyTrigger extends Trigger<NativeKeyEvent> {

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

    @Override
    public ComparisonResult compare(NativeKeyEvent value) {
        if (NativeKeyEvent.getKeyText(value.getKeyCode()).equals(this.getValue())) {
            return new ComparisonResult(new HashMap<>());
        } else {
            return new ComparisonResult("Not a match");
        }
    }
}
