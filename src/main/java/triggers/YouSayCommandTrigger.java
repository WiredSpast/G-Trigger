package triggers;

public class YouSayCommandTrigger extends CommandSaidTrigger {
    public YouSayCommandTrigger(String value) {
        super(value);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.YOUSAYCOMMAND;
    }
}
