package kitty2;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

public interface User32 extends Library {

   public static final User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);
   public static final WinDef.UINT MF_BYPOSITION = new WinDef.UINT(0x00000400L);
   public static final WinDef.UINT MF_POPUP = new WinDef.UINT(0x00000010L);
   public static final WinDef.UINT MF_STRING = new WinDef.UINT(0x00000000L);
   public static final WinDef.UINT MF_SEPARATOR = new WinDef.UINT(0x00000800L);
   public static final int GWLP_WNDPROC = -4;
   public static final WinDef.UINT EVENT_OBJECT_SHOW = new WinDef.UINT(0x00008002);
   public static final WinDef.UINT EVENT_OBJECT_NAMECHANGE = new WinDef.UINT(0x0000800C);
   public static final WinDef.UINT SWP_NOZORDER = new WinDef.UINT(0x00000004);
   public static final WinDef.UINT SWP_SHOWWINDOW = new WinDef.UINT(0x00000040);
   public static final WinDef.UINT TPM_RIGHTBUTTON = new WinDef.UINT(0x00000002L);
   public static final WinDef.UINT TPM_RETURNCMD = new WinDef.UINT(0x00000100L);
   public static final WinDef.UINT TPM_NONOTIFY = new WinDef.UINT(0x00000080L);
   public static final WinDef.UINT WM_USER = new WinDef.UINT(0x00000400L);

   public WinDef.HMENU GetSystemMenu(WinDef.HWND hWnd, WinDef.BOOL bRevert);

   public WinDef.BOOL InsertMenu(WinDef.HMENU hMenu, WinDef.UINT uPosition, WinDef.UINT uFlags, WinDef.UINT_PTR uIDNewItem, String lpNewItem);

   public BaseTSD.LONG_PTR GetWindowLongPtr(WinDef.HWND hWnd, int nIndex);

   public BaseTSD.LONG_PTR SetWindowLongPtr(WinDef.HWND hWnd, int nIndex, Callback callback);

   public WinDef.LRESULT CallWindowProc(BaseTSD.LONG_PTR lpPrevWndFunc, WinDef.HWND hWnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);

   public WinDef.HMENU CreatePopupMenu();

   public WinDef.DWORD WaitForInputIdle(WinNT.HANDLE hProcess, WinDef.DWORD dwMilliseconds);

   public WinNT.HANDLE SetWinEventHook(WinDef.UINT eventMin, WinDef.UINT eventMax, WinDef.HMODULE hmodWinEventProc, Callback lpfnWinEventProc, WinDef.DWORD idProcess, WinDef.DWORD idThread, WinDef.UINT dwflags);

   public WinDef.BOOL UnhookWinEvent(WinNT.HANDLE hWinEventHook);

   public WinDef.DWORD GetWindowThreadProcessId(WinDef.HWND hWnd, IntByReference lpdwProcessId);

   public int GetWindowText(WinDef.HWND hWnd, char[] lpString, int nMaxCount);

   public int GetWindowTextLength(WinDef.HWND hWnd);

   public WinDef.BOOL EnumWindows(Callback lpEnumFunc, WinDef.LPARAM data);

   public WinDef.BOOL SetWindowPos(WinDef.HWND hWnd, WinDef.HWND hWndInsertAfter, int X, int Y, int cx, int cy, WinDef.UINT uFlags);

   public WinDef.HWND SetParent(WinDef.HWND hWndChild, WinDef.HWND hWndNewParent);

   public WinDef.HWND PostMessage(WinDef.HWND hWnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);

   public WinDef.BOOL TrackPopupMenu(WinDef.HMENU hMenu, WinDef.UINT uFlags, int x, int y, int nReserved, WinDef.HWND hWnd, WinDef.RECT prcRect);

   public WinDef.BOOL ClientToScreen(WinDef.HWND hWnd, WinDef.POINT lpPoint);

   public WinDef.BOOL DeleteMenu(WinDef.HMENU hMenu, WinDef.UINT uPosition, WinDef.UINT uFlags);
}
