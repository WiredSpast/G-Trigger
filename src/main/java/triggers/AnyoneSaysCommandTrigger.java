package triggers;

public class AnyoneSaysCommandTrigger extends Trigger {
    public AnyoneSaysCommandTrigger(String value) {
        super(value);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.ANYONESAYSCOMMAND;
    }
}
