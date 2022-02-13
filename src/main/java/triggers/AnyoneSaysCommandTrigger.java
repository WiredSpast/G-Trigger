package triggers;

public class AnyoneSaysCommandTrigger extends CommandSaidTrigger {
    public AnyoneSaysCommandTrigger(String value) {
        super(value);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.ANYONESAYSCOMMAND;
    }
}
