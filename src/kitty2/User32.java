package kitty2;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends Library {

   public static final User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
   // napraviti da radi sa svim W32APIOptions opcijama
   public static final WinDef.UINT MF_BYPOSITION = new WinDef.UINT(0x00000400L);
   public static final WinDef.UINT MF_POPUP = new WinDef.UINT(0x00000010L);
   public static final WinDef.UINT MF_STRING = new WinDef.UINT(0x00000000L);
   public static final WinDef.UINT MF_SEPARATOR = new WinDef.UINT(0x00000800L);
   public static final int GWLP_WNDPROC = -4;
   //int GetClassName(WinDef.HWND hWnd, char[] lpClassName, int nMaxCount);

   WinDef.HMENU GetSystemMenu(WinDef.HWND hWnd, WinDef.BOOL bRevert);

   //WinDef.BOOL AppendMenu(WinDef.HMENU hMenu, WinDef.UINT uFlags, WinDef.UINT_PTR uIDNewItem, String lpNewItem);
   WinDef.BOOL InsertMenu(WinDef.HMENU hMenu, WinDef.UINT uPosition, WinDef.UINT uFlags, WinDef.UINT_PTR uIDNewItem, String lpNewItem);

   BaseTSD.LONG_PTR GetWindowLongPtr(WinDef.HWND hWnd, int nIndex);

   BaseTSD.LONG_PTR SetWindowLongPtr(WinDef.HWND hWnd, int nIndex, Callback callback);

   //WinDef.LRESULT DefWindowProc(WinDef.HWND hWnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
   WinDef.LRESULT CallWindowProc(BaseTSD.LONG_PTR lpPrevWndFunc, WinDef.HWND hWnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);

   WinDef.HMENU CreatePopupMenu();

   WinDef.DWORD WaitForInputIdle(WinNT.HANDLE hProcess, WinDef.DWORD dwMilliseconds);

   WinNT.HANDLE SetWinEventHook(
         WinDef.UINT eventMin,
         WinDef.UINT eventMax,
         WinDef.HMODULE hmodWinEventProc,
         StdCallLibrary.StdCallCallback lpfnWinEventProc,
         WinDef.DWORD idProcess,
         WinDef.DWORD idThread,
         WinDef.UINT dwflags
   );
}
