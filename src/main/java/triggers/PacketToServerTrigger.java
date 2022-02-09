package triggers;

public class PacketToServerTrigger extends PacketTrigger {
    public PacketToServerTrigger(String packetString) {
        super(packetString);
    }

    @Override
    public TriggerType getType() {
        return TriggerType.PACKETTOSERVER;
    }

    public static boolean testValue(String value) {
        return PacketTrigger.testValue(value) && !value.startsWith("{in:");
    }
}
