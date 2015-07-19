package kitty2;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import sun.awt.windows.WComponentPeer;

public class KittyPanel extends JPanel {

   User32 u32 = User32.INSTANCE;
   WinDef.HWND kittyHwnd;
   Canvas canvas;
   Rectangle originalBounds;

   public KittyPanel(WinDef.HWND hwnd) {
      this.kittyHwnd = hwnd;
      setLayout(new BorderLayout());
      canvas = new Canvas();
      add(canvas, BorderLayout.CENTER);
      addComponentListener(new ComponentAdapter() {
         @Override
         public void componentResized(ComponentEvent e) {
            onResize();
         }
      });
   }

   public void take() {
      WinDef.RECT rect = new WinDef.RECT();
      u32.GetWindowRect(kittyHwnd, rect);
      originalBounds = rect.toRectangle();
      System.out.println("canvaspeer="+canvas.getPeer());
      WinDef.HWND canvasHwnd = new WinDef.HWND(new Pointer(((WComponentPeer) canvas.getPeer()).getHWnd()));
      u32.SetParent(kittyHwnd, canvasHwnd);
      repaint();
   }

   public void release() {
      u32.SetParent(kittyHwnd, new WinDef.HWND(Pointer.NULL));
      u32.SetWindowPos(kittyHwnd, new WinDef.HWND(Pointer.NULL), originalBounds.x, originalBounds.y, originalBounds.width, originalBounds.height, 0);
   }

   public String getTitle() {
      char[] buf = new char[u32.GetWindowTextLength(kittyHwnd) + 1];
      int l = u32.GetWindowText(kittyHwnd, buf, buf.length);
      return new String(buf, 0, l);
   }

   public void onResize() {
      Rectangle bounds = getBounds();
      int x = getX();
      int y = getY();
      System.out.println(bounds);
      System.out.println("x:" + x);
      System.out.println("y:" + y);
      u32.SetWindowPos(kittyHwnd, new WinDef.HWND(Pointer.NULL), 0, 0, bounds.width, bounds.height, 0);
   }
}
