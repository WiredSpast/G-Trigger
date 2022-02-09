package triggers;

public class PacketToClientTrigger extends PacketTrigger {
    public PacketToClientTrigger(String packetString) {
        super(packetString);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.PACKETTOCLIENT;
    }

    public static boolean testValue(String value) {
        return PacketTrigger.testValue(value) && !value.startsWith("{out:");
    }
}
