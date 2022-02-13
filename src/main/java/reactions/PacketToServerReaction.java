package reactions;

import gearth.extensions.ExtensionBase;
import gearth.protocol.HPacket;

import java.util.HashMap;
import java.util.List;

public class PacketToServerReaction extends PacketReaction {
    public PacketToServerReaction(String packetString) {
        super(packetString);
    }

    @Override
    public ReactionType getType() {
        return ReactionType.PACKETTOSERVER;
    }

    @Override
    public void doReaction(ExtensionBase ext, HashMap<String, String> variables) {
        HPacket packet = this.getPacket(variables);
        if(packet.getBytesLength() != 0) {
            ext.sendToServer(packet);
        }
    }

    public static boolean testValue(String value, List<String> variables) {
        return PacketReaction.testValue(value, variables) && !value.startsWith("{in:");
    }
}
