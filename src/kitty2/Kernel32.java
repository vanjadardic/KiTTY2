package kitty2;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

public interface Kernel32 extends Library {

   public static final Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

   public boolean CreateProcess(String lpApplicationName, String lpCommandLine, WinBase.SECURITY_ATTRIBUTES lpProcessAttributes, WinBase.SECURITY_ATTRIBUTES lpThreadAttributes, WinDef.BOOL bInheritHandles, WinDef.DWORD dwCreationFlags, Pointer lpEnvironment, String lpCurrentDirectory, WinBase.STARTUPINFO lpStartupInfo, WinBase.PROCESS_INFORMATION lpProcessInformation);
}
