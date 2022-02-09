package reactions;

import gearth.extensions.ExtensionBase;
import gearth.protocol.HPacket;

public class PacketToServerReaction extends PacketReaction {
    public PacketToServerReaction(String packetString) {
        super(packetString);
    }

    @Override
    public ReactionType getType() {
        return ReactionType.PACKETTOSERVER;
    }

    @Override
    public void doReaction(ExtensionBase ext) {
        HPacket packet = this.getPacket();
        if(packet.getBytesLength() != 0) {
            ext.sendToServer(packet);
        }
    }

    public static boolean testValue(String value) {
        return PacketReaction.testValue(value) && !value.startsWith("{in:");
    }
}
