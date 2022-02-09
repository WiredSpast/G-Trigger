package reactions;

import gearth.protocol.HPacket;

public abstract class PacketReaction extends Reaction {
    public PacketReaction(String packetString) {
        super(packetString);
    }

    public HPacket getPacket() {
        return new HPacket(this.getValue());
    }

    public static boolean testValue(String value) {
        HPacket packet = new HPacket(value);
        return !packet.isCorrupted();
    }
}
