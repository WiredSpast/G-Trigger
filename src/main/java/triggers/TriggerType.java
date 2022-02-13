package triggers;

import java.lang.reflect.InvocationTargetException;

public enum TriggerType {
    KEYPRESS("Key pressed", KeyTrigger.class),
    PACKETTOSERVER("Packet to server", PacketToServerTrigger.class),
    PACKETTOCLIENT("Packet to client", PacketToClientTrigger.class),
    YOUSAYCOMMAND("You say command", YouSayCommandTrigger.class),
    ANYONESAYSCOMMAND("Anyone says cmd", AnyoneSaysCommandTrigger.class);

    public final String desc;
    private final Class<? extends Trigger<?>> triggerClass;

    <T extends Trigger<?>> TriggerType(String desc, Class<T> triggerClass) {
        this.desc = desc;
        this.triggerClass = triggerClass;
    }

    public<T extends Trigger<?>> T construct(String value) {
        try {
            return (T) triggerClass.getConstructor(String.class).newInstance(value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new Error("Couldn't find constructor for TriggerType." + this);
        }
    }

    public boolean testValue(String value) {
        try {
            return (boolean) triggerClass
                    .getMethod("testValue", String.class)
                    .invoke(null, value);
        } catch (Exception e) {
            throw new Error("Couldn't find testValue method for TriggerType." + this);
        }
    }

    @Override
    public String toString() {
        return desc;
    }
}
