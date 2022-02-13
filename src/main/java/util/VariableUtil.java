package util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableUtil {
    public static String escapeRegExChars(String str) {
        return str.replaceAll("\\$\\((\\w+)\\)", "Φ⁅$1⁆")
                .replaceAll("[<(\\[{\\\\^\\-=$!|\\]})?*+.>]", "\\\\$0")
                .replaceAll("Φ⁅(\\w+)⁆", "\\$($1)");
    }

    public static List<String> findVariables(String packetPart) {
        List<String> variables = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\$\\(\\w+\\)");
        Matcher matcher = pattern.matcher(packetPart);
        while (matcher.find()) {
            variables.add(matcher.group());
        }

        return variables;
    }

    public static String removeVariablesFromPacket(String value) {
        List<String> variables = VariableUtil.findVariables(value);
        String simplified = value;
            for(String var : variables) {
            while(simplified.contains(var)) {
                simplified = simplified.replace(var, "0");
            }
        }
        simplified = simplified.replaceAll("\\{b:[false]*0[false]*}", "{b:false}");
        simplified = simplified.replaceAll("\\{b:[true]*0[true]*}", "{b:true}");

        return simplified;
    }
}
