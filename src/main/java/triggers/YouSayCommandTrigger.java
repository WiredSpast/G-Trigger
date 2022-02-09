package triggers;

public class YouSayCommandTrigger extends Trigger {
    public YouSayCommandTrigger(String value) {
        super(value);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.YOUSAYCOMMAND;
    }
}
