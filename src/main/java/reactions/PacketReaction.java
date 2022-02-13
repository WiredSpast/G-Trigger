package reactions;

import gearth.protocol.HPacket;
import util.VariableUtil;

import java.util.HashMap;
import java.util.List;

public abstract class PacketReaction extends Reaction {
    public PacketReaction(String packetString) {
        super(packetString);
    }

    public HPacket getPacket(HashMap<String, String> variables) {
        return new HPacket(this.getCompletedValue(variables));
    }

    public static boolean testValue(String value, List<String> variableNames) {
        List<String> varsInValue = VariableUtil.findVariables(value);
        if (!variableNames.containsAll(varsInValue)) return false;

        HPacket packet = new HPacket(VariableUtil.removeVariablesFromPacket(value));
        return !packet.isCorrupted();
    }
}
