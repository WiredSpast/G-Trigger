package triggers;

import java.lang.reflect.InvocationTargetException;

public enum TriggerType {
    KEYPRESS("Key pressed", KeyTrigger.class),
    PACKETTOSERVER("Packet to server", PacketToServerTrigger.class),
    PACKETTOCLIENT("Packet to client", PacketToClientTrigger.class),
    YOUSAYCOMMAND("You say command", YouSayCommandTrigger.class),
    ANYONESAYSCOMMAND("Anyone says cmd", AnyoneSaysCommandTrigger.class);

    public final String desc;
    private final Class triggerClass;

    <T extends Trigger> TriggerType(String desc, Class<T> triggerClass) {
        this.desc = desc;
        this.triggerClass = triggerClass;
    }

    public<T extends Trigger> T construct(String value) {
        try {
            return (T) triggerClass.getConstructor(String.class).newInstance(value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        throw new Error("Couldn't find constructor for TriggerType." + this);
    }

    public boolean testValue(String value) {
        switch (this) {
            case PACKETTOSERVER:
                return PacketToServerTrigger.testValue(value);
            case PACKETTOCLIENT:
                return PacketToClientTrigger.testValue(value);
            case KEYPRESS:
                return KeyTrigger.testValue(value);
            case ANYONESAYSCOMMAND:
            case YOUSAYCOMMAND:
                return CommandSaidTrigger.testValue(value);
        }

        return false;
    }

    @Override
    public String toString() {
        return desc;
    }
}
