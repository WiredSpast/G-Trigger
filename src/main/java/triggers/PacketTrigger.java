package triggers;

import gearth.protocol.HPacket;
import gearth.services.packet_info.PacketInfoManager;
import util.ComparisonResult;
import util.VariableUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PacketTrigger extends Trigger<HPacket> {

    protected PacketTrigger(String packetString) {
        super(packetString);
    }

    public HPacket getCompletedSimplifiedPacket(PacketInfoManager packetInfoManager) {
        HPacket packet = new HPacket(VariableUtil.removeVariablesFromPacket(this.getValue()));
        if(!packet.isPacketComplete()) {
            if(packet.canComplete(packetInfoManager)) {
                packet.completePacket(packetInfoManager);
            } else {
                return new HPacket(0);
            }
        }

        return packet;
    }

    public static boolean testValue(String value) {
        HPacket packet = new HPacket(VariableUtil.removeVariablesFromPacket(value));
        return !packet.isCorrupted();
    }

    @Override
    public ComparisonResult compare(HPacket packet) {
        packet.resetReadIndex();
        HashMap<String, String> variables = new HashMap<>();

        try {
            Pattern pattern = Pattern.compile("\\{[sibuldf]:([^\"]+?|\".*?\")}");
            Matcher matcher = pattern.matcher(getValue());
            while (matcher.find()) {
                String packetPart = VariableUtil.escapeRegExChars(matcher.group());
                System.out.println(packetPart);
                List<String> variablesInPart = VariableUtil.findVariables(packetPart);
                String packetPartRegEx = packetPart;
                for (String var : variablesInPart) {
                    while (packetPartRegEx.contains(var)) {
                        packetPartRegEx = packetPartRegEx.replace(var, "(.+)");
                    }
                }

                Pattern comparisonPattern = Pattern.compile(packetPartRegEx);
                List<String> comparisonStrings = new ArrayList<>();
                if (packetPart.startsWith("\\{s:\"")) {
                    System.out.println("String");
                    comparisonStrings.add(String.format("{s:\"%s\"}", packet.readString()));
                } else if (packetPart.equals("\\{b:false\\}") || packetPart.equals("\\{b:true\\}")) {
                    System.out.println("Boolean");
                    comparisonStrings.add(String.format("{b:%b}", packet.readBoolean()));
                } else if (packetPart.startsWith("\\{i:")) {
                    System.out.println("Integer");
                    comparisonStrings.add(String.format("{i:%d}", packet.readInteger()));
                } else if(packetPart.startsWith("\\{s:")) {
                    System.out.println("Short");
                    comparisonStrings.add(String.format("{s:%f}", packet.readDouble()));
                } else if(packetPart.startsWith("\\{f:")) {
                    System.out.println("Float");
                    comparisonStrings.add(String.format("{f:%f}", packet.readFloat()));
                } else if(packetPart.startsWith("\\{u:")) {
                    System.out.println("UShort");
                    comparisonStrings.add(String.format("{u:%d}", packet.readUshort()));
                } else if(packetPart.startsWith("\\{l:")) {
                    System.out.println("Long");
                    comparisonStrings.add(String.format("{l:%d}", packet.readLong()));
                } else if(packetPart.startsWith("\\{b:")) {
                    System.out.println("Byte or boolean");
                    byte byteValue = packet.readByte();
                    comparisonStrings.add(String.format("{b:%b}", byteValue != 0));
                    comparisonStrings.add(String.format("{b:%d}", byteValue));
                }

                comparisonStrings.forEach(System.out::println);
                if(comparisonStrings.size() == 0) {
                    return new ComparisonResult("No comparisonString defined");
                }

                for(String comparisonString : comparisonStrings) {
                    Matcher comparisonMatcher = comparisonPattern.matcher(comparisonString);

                    if(!comparisonMatcher.matches()) {
                        if(comparisonStrings.indexOf(comparisonString) == comparisonStrings.size() - 1) {
                            return new ComparisonResult("Part '" + packetPart + "' doesn't match '" + comparisonString + "'");
                        } else {
                            continue;
                        }
                    }

                    if(comparisonMatcher.find(0)) {
                        for(int i = 1; i <= comparisonMatcher.groupCount(); i++) {
                            if(!comparisonMatcher.group(i).equals(variables.getOrDefault(variablesInPart.get(i - 1), comparisonMatcher.group(i)))) {
                                return new ComparisonResult(String.format("Variable '%s' occurs more than once with different values: %s ≠ %s", variablesInPart.get(i - 1), variables.getOrDefault(variablesInPart.get(i - 1), comparisonMatcher.group(i)), comparisonMatcher.group(i)));
                            }
                            variables.put(variablesInPart.get(i - 1), comparisonMatcher.group(i));
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            return new ComparisonResult(e.toString());
        }

        return new ComparisonResult(variables);
    }

    public static void main(String[] args) {
        System.out.println(PacketTrigger.testValue("{out:Test}{s:\"($(abc)\"}{s:\"$(def)\"}{i:4$(ghi)}{b:t$(d)}"));

        PacketToServerTrigger trigger = new PacketToServerTrigger("{out:Test}{s:\"($(abc)\"}{s:\"$(def)\"}{i:4$(ghi)}{b:fa$(d)se}");
        HPacket packet = new HPacket("{out:Test}{s:\"(def\"}{s:\"ls\"}{i:485}{b:false}");

        System.out.println(trigger.compare(packet));

        packet = new HPacket("{out:Test}{s:\"(((def\"}{s:\"6\"}{i:46}{b:0}");
        System.out.println(trigger.compare(packet));
    }
}
