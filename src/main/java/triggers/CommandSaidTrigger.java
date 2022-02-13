package triggers;

import gearth.protocol.HPacket;
import util.ComparisonResult;
import util.VariableUtil;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CommandSaidTrigger extends Trigger<String> {
    public CommandSaidTrigger(String value) {
        super(value);
    }

    public static boolean testValue(String value) {
        return true;
    }

    @Override
    public ComparisonResult compare(String command) {
        HashMap<String, String> variables = new HashMap<>();

        try {
            List<String> variablesInCommand = VariableUtil.findVariables(getValue());
            String commandRegEx = getValue();
            for (String var : variablesInCommand) {
                while (commandRegEx.contains(var)) {
                    commandRegEx = commandRegEx.replace(var, "(.+)");
                }
            }

            Pattern comparisonPattern = Pattern.compile(commandRegEx);
            Matcher comparisonMatcher = comparisonPattern.matcher(command);

            if (!comparisonMatcher.matches()) {
                return new ComparisonResult("Part '" + commandRegEx + "' doesn't match with '" + command + "'");
            }

            if (comparisonMatcher.find(0)) {
                for (int i = 1; i <= comparisonMatcher.groupCount(); i++) {
                    if (!comparisonMatcher.group(i).equals(variables.getOrDefault(variablesInCommand.get(i - 1), comparisonMatcher.group(i)))) {
                        return new ComparisonResult(String.format("Variable '%s' occurs more than once with different values: %s ≠ %s", variablesInCommand.get(i - 1), variables.getOrDefault(variablesInCommand.get(i - 1), comparisonMatcher.group(i)), comparisonMatcher.group(i)));
                    }
                    variables.put(variablesInCommand.get(i - 1), comparisonMatcher.group(i));
                }
            }
        } catch (Exception e) {
            return new ComparisonResult(e.toString());
        }

        return new ComparisonResult(variables);
    }
}
