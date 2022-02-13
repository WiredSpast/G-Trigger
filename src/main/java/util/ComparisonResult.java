package util;

import java.util.HashMap;

public class ComparisonResult {
    private final boolean valid;
    private final HashMap<String, String> variables;
    private final String reason;

    public ComparisonResult(HashMap<String, String> variables) {
        this.valid = true;
        this.variables = variables;
        this.reason = null;
    }

    public ComparisonResult(String errorMessage) {
        this.valid = false;
        this.variables = null;
        this.reason = errorMessage;
    }

    public boolean isValid() {
        return valid;
    }

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "ComparisonResult {\n" +
                "\tvalid=" + valid +
                ((variables != null) ? (",\n\tvariables=" + variables) : "") +
                ((reason != null) ? (",\n\treason='" + reason + '\'') : "") +
                "\n}";
    }
}
