package triggers;

public abstract class CommandSaidTrigger extends Trigger {
    public CommandSaidTrigger(String value) {
        super(value);
    }

    public static boolean testValue(String value) {
        return true;
    }
}
