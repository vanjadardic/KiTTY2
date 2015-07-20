package kitty2;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import mydocking.Tab;
import mydocking.TabColors;
import mydocking.TabManager;

public class KittyPanel extends JPanel {

   private static final User32 user32 = User32.INSTANCE;
   private static final Kernel32 kernel32 = Kernel32.INSTANCE;
   //private final TabManager tabManager;
   private Tab tab;
   private WinDef.HWND kittyHwnd;
   //private Canvas canvas;

   public KittyPanel(TabManager tabManager, String kittyHome, String folderParam, String loadParam) {
      setLayout(new BorderLayout());
      Canvas canvas = new Canvas();
      add(canvas, BorderLayout.CENTER);
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            onResize();
         }
      });

      //this.tabManager = tabManager;
      WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
      startupInfo.dwFlags = WinBase.STARTF_USESHOWWINDOW;
      startupInfo.wShowWindow = new WinDef.WORD(WinUser.SW_HIDE);
      WinBase.PROCESS_INFORMATION processInformation = new WinBase.PROCESS_INFORMATION();

      String command = kittyHome + "\\kitty_portable.exe" + folderParam + loadParam;
      kernel32.CreateProcess(null, command, null, null, new WinDef.BOOL(0), new WinDef.DWORD(0), null, kittyHome, startupInfo, processInformation);
      WinDef.DWORD idleResult = user32.WaitForInputIdle(processInformation.hProcess, new WinDef.DWORD(2000));
      if (idleResult.intValue() != 0) {
         throw new RuntimeException("Can't start process (WaitForInputIdle=" + idleResult.intValue() + ")!");
      }

      user32.SetWinEventHook(User32.EVENT_OBJECT_SHOW, User32.EVENT_OBJECT_SHOW, null, new Callback() {
         public void callback(WinNT.HANDLE hWinEventHook, WinDef.DWORD event, WinDef.HWND hwnd, long idObject, long idChild, WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
            user32.EnumWindows(new Callback() {
               public boolean callback(WinDef.HWND hWnd, WinDef.LPARAM data) {
                  IntByReference processId = new IntByReference();
                  user32.GetWindowThreadProcessId(hWnd, processId);
                  if (processInformation.dwProcessId.intValue() == processId.getValue()) {
                     kittyHwnd = hWnd;
                     SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           tab = tabManager.addNewTab(WinUtil.getWindowTitle(kittyHwnd), KittyPanel.this, TabColors.BLUE);
                           tab.addMouseListener(new MouseAdapter() {
                              @Override
                              public void mouseReleased(MouseEvent e) {
                                 if (e.getButton() == MouseEvent.BUTTON3) {
                                    WinDef.HWND mainHwnd = new WinDef.HWND(Native.getWindowPointer((Window) SwingUtilities.getRoot(tab)));
                                    user32.PostMessage(mainHwnd, User32.WM_USER, new WinDef.WPARAM(0), new WinDef.LPARAM(Integer.parseInt(tab.getId())));
                                 }
                              }
                           });

                           WinDef.HWND canvasHwnd = new WinDef.HWND(Native.getComponentPointer(canvas));
                           user32.SetParent(kittyHwnd, canvasHwnd);
                           Rectangle bounds = getBounds();
                           user32.SetWindowPos(kittyHwnd, null, 0, 0, bounds.width, bounds.height, WinUtil.orUINT(User32.SWP_NOZORDER, User32.SWP_SHOWWINDOW));
                           repaint();
                        }
                     });
                     return false;
                  }
                  return true;
               }
            }, new WinDef.LPARAM(0));
            user32.UnhookWinEvent(hWinEventHook);
         }
      }, processInformation.dwProcessId, processInformation.dwThreadId, new WinDef.UINT(0));

      user32.SetWinEventHook(User32.EVENT_OBJECT_NAMECHANGE, User32.EVENT_OBJECT_NAMECHANGE, null, new Callback() {
         public void callback(WinNT.HANDLE hWinEventHook, WinDef.DWORD event, WinDef.HWND hwnd, long idObject, long idChild, WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
            if (tab != null && idObject == 0) {
               tab.setTitle(WinUtil.getWindowTitle(kittyHwnd));
            }
         }
      }, processInformation.dwProcessId, processInformation.dwThreadId, new WinDef.UINT(0));
   }

   public void onResize() {
      Rectangle bounds = getBounds();
      user32.SetWindowPos(kittyHwnd, new WinDef.HWND(Pointer.NULL), 0, 0, bounds.width, bounds.height, new WinDef.UINT(0));
   }

   public WinDef.HWND getKittyHwnd() {
      return kittyHwnd;
   }
}
