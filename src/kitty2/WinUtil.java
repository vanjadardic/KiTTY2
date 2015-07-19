package kitty2;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;

public class WinUtil {

   public static User32 user32 = User32.INSTANCE;

   public static WinDef.UINT orUINT(WinDef.UINT... values) {
      long or = 0;
      for (WinDef.UINT value : values) {
         or |= value.longValue();
      }
      return new WinDef.UINT(or);
   }

   static void InsertMenuSeparator(WinDef.HMENU menu, int position, int id) {
      user32.InsertMenu(menu, new WinDef.UINT(position), WinUtil.orUINT(User32.MF_BYPOSITION, User32.MF_SEPARATOR), new WinDef.UINT_PTR(id), "");
   }

   static void InsertMenuItem(WinDef.HMENU menu, int position, int id, String text) {
      user32.InsertMenu(menu, new WinDef.UINT(position), WinUtil.orUINT(User32.MF_BYPOSITION, User32.MF_STRING), new WinDef.UINT_PTR(id), text);
   }

   static void InsertMenuPopup(WinDef.HMENU menu, int position, WinDef.HMENU popup, String text) {
      user32.InsertMenu(menu, new WinDef.UINT(position), WinUtil.orUINT(User32.MF_BYPOSITION, User32.MF_POPUP), new WinDef.UINT_PTR(Pointer.nativeValue(popup.getPointer())), text);
   }

   public static class WindowProcCallback implements Callback {

      private final BaseTSD.LONG_PTR previousProcPtr;

      public WindowProcCallback(WinDef.HWND hwnd) {
         previousProcPtr = user32.GetWindowLongPtr(hwnd, User32.GWLP_WNDPROC);
      }

      public WinDef.LRESULT callback(WinDef.HWND hwnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
         return user32.CallWindowProc(previousProcPtr, hwnd, Msg, wParam, lParam);
      }
   }
}
