package reactions;

import java.util.List;

public enum ReactionType {
    PACKETTOSERVER("Packet to server", PacketToServerReaction.class),
    PACKETTOCLIENT("Packet to client", PacketToClientReaction.class);

    public final String desc;
    private final Class<? extends Reaction> reactionClass;

    <T extends Reaction> ReactionType(String desc, Class<T> reactionClass) {
        this.desc = desc;
        this.reactionClass = reactionClass;
    }

    public<T extends Reaction> T construct(String value) {
        try {
            return (T) reactionClass.getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new Error("Couldn't find constructor for ReactionType." + this);
        }
    }

    public boolean testValue(String value, List<String> variables) {
        try {
            return (boolean) reactionClass
                    .getMethod("testValue", String.class, List.class)
                    .invoke(null, value, variables);
        } catch (Exception e) {
            throw new Error("Couldn't find testValue method for ReactionType." + this);
        }
    }

    @Override
    public String toString() {
        return desc;
    }
}
