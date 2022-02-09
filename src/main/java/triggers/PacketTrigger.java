package triggers;

import gearth.protocol.HPacket;
import gearth.services.packet_info.PacketInfoManager;

public abstract class PacketTrigger extends Trigger{
    protected PacketTrigger(String packetString) {
        super(packetString);
    }

    public HPacket getCompletedPacket(PacketInfoManager packetInfoManager) {
        HPacket packet = new HPacket(this.getValue());
        if(!packet.isPacketComplete()) {
            if(packet.canComplete(packetInfoManager)) {
                packet.completePacket(packetInfoManager);
            }
        } else {
            return new HPacket(new byte[0]);
        }

        return packet;
    }

    public static boolean testValue(String value) {
        HPacket packet = new HPacket(value);
        return !packet.isCorrupted();
    }
}
