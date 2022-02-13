package util;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

// source: https://stackoverflow.com/a/18275492/18114039
public class WindowWatcher {
    public static String getFrontMostWindowName() {
        if (Platform.isWindows()) {
            return getFrontMostWindowNameWindows();
        } else if (Platform.isLinux()) {
            return getFrontMostWindowNameLinux();
        } else if (Platform.isMac()) {
            return getFrontMostWindowNameMac();
        }
        return "";
    }

    private static String getFrontMostWindowNameWindows() {
        final int PROCESS_VM_READ = 0x0010;
        final int PROCESS_QUERY_INFORMATION = 0x0400;
        final User32 user32 = User32.INSTANCE;
        final Kernel32 kernel32 = Kernel32.INSTANCE;
        WinDef.HWND windowHandle = user32.GetForegroundWindow();
        IntByReference pid = new IntByReference();
        user32.GetWindowThreadProcessId(windowHandle, pid);
        WinNT.HANDLE processHandle = kernel32.OpenProcess(PROCESS_VM_READ | PROCESS_QUERY_INFORMATION, true, pid.getValue());

        byte[] filename = new byte[512];
        Psapi.INSTANCE.GetModuleBaseNameW(processHandle.getPointer(), Pointer.NULL, filename, filename.length);
        String name = new String(filename);
        return name.replaceAll("[^\\w.]", "");
    }

    private static String getFrontMostWindowNameLinux() {
        final X11 x11 = X11.INSTANCE;
        final XLib xlib = XLib.INSTANCE;
        X11.Display display = x11.XOpenDisplay(null);
        X11.Window window = new X11.Window();
        xlib.XGetInputFocus(display, window, Pointer.NULL);
        X11.XTextProperty name = new X11.XTextProperty();
        x11.XGetWMName(display, window, name);
        return name.toString();
    }

    private static String getFrontMostWindowNameMac() {
        try {
            final String script = "tell application \"System Events\"\n" +
                    "\tname of application processes whose frontmost is tru\n" +
                    "end";
            ScriptEngine appleScript = new ScriptEngineManager().getEngineByName("AppleScript");
            return (String) appleScript.eval(script);
        } catch (ScriptException exception) {
            return "";
        }
    }

    private interface Psapi extends StdCallLibrary {
        Psapi INSTANCE = Native.load("Psapi", Psapi.class);

        WinDef.DWORD GetModuleBaseNameW(Pointer hProcess, Pointer hModule, byte[] lpBaseName, int nSize);
    }

    private interface XLib extends StdCallLibrary {
        XLib INSTANCE = Native.load("XLib", XLib.class);

        int XGetInputFocus(X11.Display display, X11.Window focus_return, Pointer revert_to_return);
    }
}
