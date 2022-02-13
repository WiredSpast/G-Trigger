package reactions;

import gearth.extensions.ExtensionBase;
import gearth.protocol.HPacket;

import java.util.HashMap;
import java.util.List;

public class PacketToClientReaction extends PacketReaction {
    public PacketToClientReaction(String packetString) {
        super(packetString);
    }

    @Override
    public ReactionType getType() {
        return ReactionType.PACKETTOCLIENT;
    }

    @Override
    public void doReaction(ExtensionBase ext, HashMap<String, String> variables) {
        HPacket packet = this.getPacket(variables);
        if(packet.getBytesLength() != 0) {
            ext.sendToClient(packet);
        }
    }

    public static boolean testValue(String value, List<String> variables) {
        return PacketReaction.testValue(value, variables) && !value.startsWith("{out:");
    }
}
