package util;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

import java.util.HashSet;

public class ValidNativeKeys {
    private static final HashSet<String> nativeKeys = new HashSet<>();

    static {
        synchronized (nativeKeys) {
            for (int i = 0; i <= 65535; i++) {
                String keyText = NativeKeyEvent.getKeyText(i);
                if(!keyText.contains("Unknown")) {
                    nativeKeys.add(keyText);
                }
            }
        }
    }

    public static boolean isNativeKey(String keyText) {
        return nativeKeys.contains(keyText);
    }
}
