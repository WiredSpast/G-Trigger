package util;

public enum EditingMode {
    Add ("Add"),
    Edit ("Edit");

    public static long editingId = -1;
    public static boolean propped = false;

    private final String mode;

    EditingMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return mode;
    }
}
