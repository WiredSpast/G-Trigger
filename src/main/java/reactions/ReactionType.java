package reactions;

import java.lang.reflect.InvocationTargetException;

public enum ReactionType {
    PACKETTOSERVER("Packet to server", PacketToServerReaction.class),
    PACKETTOCLIENT("Packet to client", PacketToClientReaction.class);

    public final String desc;
    private final Class reactionClass;

    <T extends Reaction> ReactionType(String desc, Class<T> reactionClass) {
        this.desc = desc;
        this.reactionClass = reactionClass;
    }

    public<T extends Reaction> T construct(String value) {
        try {
            return (T) reactionClass.getConstructor(String.class).newInstance(value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }

        throw new Error("Couldn't find constructor for TriggerType." + this);
    }

    public boolean testValue(String value) {
        switch (this) {
            case PACKETTOSERVER:
                return PacketToServerReaction.testValue(value);
            case PACKETTOCLIENT:
                return PacketToClientReaction.testValue(value);
        }

        return false;
    }

    @Override
    public String toString() {
        return desc;
    }
}
