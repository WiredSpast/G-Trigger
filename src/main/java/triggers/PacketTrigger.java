package triggers;

import gearth.protocol.HPacket;
import gearth.services.packet_info.PacketInfoManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public PacketComparisonResult comparePacket(HPacket packet) {
        packet.resetReadIndex();
        HashMap<String, String> variables = new HashMap<>();

        try {
            Pattern pattern = Pattern.compile("\\{[sibuldf]:([^\"]+?|\".*?\")}");
            Matcher matcher = pattern.matcher(getValue());
            while (matcher.find()) {
                String packetPart = matcher.group();
                System.out.println(packetPart);
                List<String> variablesInPart = findVariablesInPacketPart(packetPart);
                String packetPartRegEx = packetPart;
                for(String var : variablesInPart) {
                    packetPartRegEx = packetPartRegEx.replace(var, "(.+)");
                }

                Pattern comparisonPattern = Pattern.compile(String.format("\\%s", packetPartRegEx));
                String comparisonString = null;
                if(packetPart.startsWith("{s:\"")) {
                    System.out.println("String");
                    comparisonString = String.format("{s:\"%s\"}", packet.readString());
                } else if(packetPart.equals("{b:false}") || packetPart.equals("{b:true}")) {
                    System.out.println("Boolean");
                    comparisonString = String.format("{b:%b}", packet.readBoolean());
                } else if(packetPart.startsWith("{i:")) {
                    System.out.println("Integer");
                    comparisonString = String.format("{i:%d}", packet.readInteger());
                } else if(packetPart.startsWith("{s:")) {
                    System.out.println("Short");
                    comparisonString = String.format("{s:%f}", packet.readDouble());
                } else if(packetPart.startsWith("{f:")) {
                    System.out.println("Float");
                    comparisonString = String.format("{f:%f}", packet.readFloat());
                } else if(packetPart.startsWith("{u:")) {
                    System.out.println("UShort");
                    comparisonString = String.format("{u:%d}", packet.readUshort());
                } else if(packetPart.startsWith("{l:")) {
                    System.out.println("Long");
                    comparisonString = String.format("{l:%d}", packet.readLong());
                } else if(packetPart.matches("\\{b:\\d+}")) {
                    System.out.println("Byte");
                    comparisonString = String.format("{b:%d}", packet.readByte());
                }
                System.out.println(comparisonString);
                if(comparisonString == null) {
                    return new PacketComparisonResult("Undefined comparisonString");
                }

                Matcher comparisonMatcher = comparisonPattern.matcher(comparisonString);

                if(!comparisonMatcher.matches()) {
                    return new PacketComparisonResult("Part '" + packetPart + "' doesn't match '" + comparisonString + "'");
                }

                if(comparisonMatcher.find(0)) {
                    for(int i = 1; i <= comparisonMatcher.groupCount(); i++) {
                        if(!comparisonMatcher.group(i).equals(variables.getOrDefault(variablesInPart.get(i - 1), comparisonMatcher.group(i)))) {
                            return new PacketComparisonResult(String.format("Variable '%s' occurs more than once with different values: %s ≠ %s", variablesInPart.get(i - 1), variables.getOrDefault(variablesInPart.get(i - 1), comparisonMatcher.group(i)), comparisonMatcher.group(i)));
                        }
                        variables.put(variablesInPart.get(i - 1), comparisonMatcher.group(i));
                    }
                }
            }
        } catch (Exception e) {
            return new PacketComparisonResult(e.toString());
        }

        return new PacketComparisonResult(variables);
    }

    private List<String> findVariablesInPacketPart(String packetPart) {
        List<String> variables = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\$\\(\\w+\\)");
        Matcher matcher = pattern.matcher(packetPart);
        while (matcher.find()) {
            variables.add(matcher.group());
        }

        return variables;
    }

    public static class PacketComparisonResult {
        private final boolean result;
        private final HashMap<String, String> variables;
        private final String reason;

        private PacketComparisonResult(HashMap<String, String> variables) {
            this.result = true;
            this.variables = variables;
            this.reason = null;
        }

        private PacketComparisonResult(String errorMessage) {
            this.result = false;
            this.variables = null;
            this.reason = errorMessage;
        }

        @Override
        public String toString() {
            return "PacketComparisonResult {\n" +
                    "\tresult=" + result +
                    ((variables != null) ? (",\n\tvariables=" + variables) : "") +
                    ((reason != null) ? (",\n\treason='" + reason + '\'') : "") +
                    "\n}";
        }
    }

    public static void main(String[] args) {
        PacketToServerTrigger trigger = new PacketToServerTrigger("{out:Test}{s:\"$(abc)\"}{s:\"$(def)\"}{i:4$(ghi)}{b:fa$(def)e}");
        HPacket packet = new HPacket("{out:Test}{s:\"def\"}{s:\"ls\"}{i:485}{b:false}");

        System.out.println(trigger.comparePacket(packet));

        packet = new HPacket("{out:Test}{s:\"def\"}{s:\"6\"}{i:46}{b:0}");
        System.out.println(trigger.comparePacket(packet));
    }
}
