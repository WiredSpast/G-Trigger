package reactions;

import gearth.extensions.ExtensionBase;
import gearth.protocol.HPacket;

public class PacketToClientReaction extends PacketReaction {
    public PacketToClientReaction(String packetString) {
        super(packetString);
    }

    @Override
    public ReactionType getType() {
        return ReactionType.PACKETTOCLIENT;
    }

    @Override
    public void doReaction(ExtensionBase ext) {
        HPacket packet = this.getPacket();
        if(packet.getBytesLength() != 0) {
            ext.sendToClient(packet);
        }
    }

    public static boolean testValue(String value) {
        return PacketReaction.testValue(value) && !value.startsWith("{out:");
    }
}
