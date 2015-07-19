package kitty2;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import mydocking.Tab;
import mydocking.TabColors;

public class MainFrame extends javax.swing.JFrame {

   // title 
   // desni klik
   // ugasena aplikaicja
   // glass frame for focus
   private static boolean errorDialogShown = false;
   private final User32 user32 = User32.INSTANCE;
   private final int CUSTOM_MENU_FIRST_ID = 0xFFFF;
   private int CUSTOM_MENU_CURRENT_ID = CUSTOM_MENU_FIRST_ID;
   private final Map<Integer, CustomMenuActionListener> mapCustomMenuHandlers = new HashMap<>();
   private final String kittyHome = "C:\\Users\\vanja\\Desktop\\KiTTY";

   public MainFrame() {
      initComponents();

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowOpened(WindowEvent e) {
            try {
               File f = new File(kittyHome);
               if (f.isDirectory()) {
                  for (File listFile : f.listFiles()) {
                     if (listFile.isDirectory() && "Sessions".equals(listFile.getName())) {
                        System.out.println("krece " + listFile);
                        WinDef.HWND hwnd = new WinDef.HWND(Native.getWindowPointer(MainFrame.this));
                        WinDef.HMENU systemMenu = user32.GetSystemMenu(hwnd, new WinDef.BOOL(0));
                        int pos = loadSessions(systemMenu, mapCustomMenuHandlers, "", listFile, 0);
                        WinUtil.InsertMenuSeparator(systemMenu, pos, ++CUSTOM_MENU_CURRENT_ID);
                     }
                  }
               }

               WinDef.HWND hwnd = new WinDef.HWND(Native.getWindowPointer(MainFrame.this));
               user32.SetWindowLongPtr(hwnd, User32.GWLP_WNDPROC, new WinUtil.WindowProcCallback(hwnd) {
                  @Override
                  public WinDef.LRESULT callback(WinDef.HWND hwnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
                     if (WinUser.WM_SYSCOMMAND == Msg.intValue()) {
                        if (mapCustomMenuHandlers.containsKey(wParam.intValue())) {
                           CustomMenuActionListener act = mapCustomMenuHandlers.get(wParam.intValue());
                           if (act != null) {
                              act.actionPerformed();
                           }
                           return new WinDef.LRESULT(0);
                        }
                     }

                     return super.callback(hwnd, Msg, wParam, lParam);
                  }
               });
            } catch (UnsupportedEncodingException ex) {
               throw new RuntimeException("Erro in startup", ex);
            }
         }
      });
   }

   @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      tabManager1 = new mydocking.TabManager();

      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
      setTitle("KiTTY2");

      tabManager1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 3));

      javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
      getContentPane().setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(34, 34, 34)
            .addComponent(tabManager1, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE)
            .addGap(74, 74, 74))
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
            .addGap(39, 39, 39)
            .addComponent(tabManager1, javax.swing.GroupLayout.DEFAULT_SIZE, 423, Short.MAX_VALUE)
            .addGap(99, 99, 99))
      );

      setBounds(0, 0, 1000, 600);
   }// </editor-fold>//GEN-END:initComponents

   private int loadSessions(WinDef.HMENU menu, Map<Integer, CustomMenuActionListener> mmm, final String folder, File directory, int pos) throws UnsupportedEncodingException {
      List<File> filesss = new ArrayList<>(Arrays.asList(directory.listFiles()));
      Collections.sort(filesss, new Comparator<File>() {
         @Override
         public int compare(File o1, File o2) {
            if (o1.isDirectory() && o2.isFile()) {
               return -1;
            }
            if (o1.isFile() && o2.isDirectory()) {
               return 1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
         }
      });

      for (final File listFile : filesss) {
         if (listFile.isFile()) {
            final String text = URLDecoder.decode(listFile.getName(), "cp1252");
            if ("Default Settings".equals(text)) {
               continue;
            }
            CUSTOM_MENU_CURRENT_ID++;
            mmm.put(CUSTOM_MENU_CURRENT_ID, new CustomMenuActionListener() {
               @Override
               public void actionPerformed() {
                  String folderparam = " ";
                  if (folder.length() > 1) {
                     folderparam = " -folder \"" + folder.substring(1) + "\"";
                  }

                  String command = kittyHome + "\\kitty_portable.exe" + folderparam + " -load \"" + text + "\"";

                  WinBase.STARTUPINFO startupInfo = new WinBase.STARTUPINFO();
                  final WinBase.PROCESS_INFORMATION processInformation = new WinBase.PROCESS_INFORMATION();

                  //Kernel32.INSTANCE.CreateProcess(null, command, null, null, false, new WinDef.DWORD(WinBase.DETACHED_PROCESS), null, kittyHome, startupInfo, processInformation);
                  Kernel32.INSTANCE.CreateProcess(null, command, null, null, false, new WinDef.DWORD(0), null, kittyHome, startupInfo, processInformation);

                  long l = System.currentTimeMillis();
                  WinDef.DWORD result = user32.WaitForInputIdle(processInformation.hProcess, new WinDef.DWORD(2000));
                  System.out.println("elapsed " + (System.currentTimeMillis() - l));
                  System.out.println("result=" + result);
                  if (result.intValue() != 0) {
                     throw new RuntimeException("cannot start process WaitForInputIdle=" + result.intValue());
                  }
                  System.out.println(kittyHome + "\\kitty_portable.exe" + folderparam + " -load \"" + text + "\"");
                  System.out.println(processInformation.dwProcessId);
                  System.out.println(Pointer.nativeValue(processInformation.hProcess.getPointer()));

                  com.sun.jna.platform.win32.User32.INSTANCE.EnumWindows(new WinUser.WNDENUMPROC() {

                     @Override
                     public boolean callback(WinDef.HWND hWnd, Pointer data) {
                        IntByReference i = new IntByReference();
                        com.sun.jna.platform.win32.User32.INSTANCE.GetWindowThreadProcessId(hWnd, i);
                        //System.out.println("enum="+i.getValue());
                        if (processInformation.dwProcessId.intValue() == i.getValue()) {
                           //System.out.println("MATCHHHHHHHHHHHHHHH");

                           final KittyPanel kp = new KittyPanel(hWnd);
                           final String title = kp.getTitle();
                           System.out.println(title);

                           SwingUtilities.invokeLater(new Runnable() {

                              @Override
                              public void run() {
                                 final Tab tab = tabManager1.addNewTab(title, kp, TabColors.PURPLE);

                                 kp.take();

                              }
                           });

                           WinNT.HANDLE hl = user32.SetWinEventHook(new WinDef.UINT(0x800C), new WinDef.UINT(0x800C), null, new StdCallLibrary.StdCallCallback() {
                              public void callback(WinNT.HANDLE hWinEventHook, WinDef.DWORD event, WinDef.HWND hwnd, long idObject, long idChild, WinDef.DWORD dwEventThread, WinDef.DWORD dwmsEventTime) {
                                 System.out.println("callback " + idObject + "     " + idChild);
                                 if (idObject == 0) {
                                    char[] buf = new char[com.sun.jna.platform.win32.User32.INSTANCE.GetWindowTextLength(hwnd) + 1];
                                    int l = com.sun.jna.platform.win32.User32.INSTANCE.GetWindowText(hwnd, buf, buf.length);
                                    String newTitle = new String(buf, 0, l);
                                    System.out.println(newTitle);
                                    //tab.setTitle(newTitle);
                                 }
                              }
                           }, processInformation.dwProcessId, processInformation.dwThreadId, new WinDef.UINT(0));

                           //kp.take();
                           return false;
                        }
                        return true;
                     }
                  }, Pointer.NULL);

               }
            });

            //System.out.println("adding " + pos + "    " + CUSTOM_MENU_CURRENT_ID + "    " + text);
            WinUtil.InsertMenuItem(menu, pos, CUSTOM_MENU_CURRENT_ID, text);
            pos++;
         } else if (listFile.isDirectory()) {
            final String text = URLDecoder.decode(listFile.getName(), "cp1252");
            WinDef.HMENU popup = user32.CreatePopupMenu();
            loadSessions(popup, mmm, folder + "\\" + text, listFile, 0);
            WinUtil.InsertMenuPopup(menu, pos, popup, text);
            pos++;
         }

      }
      return pos;
   }

   public static void main(String args[]) {
      Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
         @Override
         public void uncaughtException(Thread t, Throwable e) {
            e.printStackTrace();
            if (errorDialogShown) {
               return;
            }
            errorDialogShown = true;

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setPreferredSize(new Dimension(800, 300));
            JTextArea textArea = new JTextArea(sw.toString());
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane.getViewport().setView(textArea);

            int option = JOptionPane.showOptionDialog(
                  null,
                  scrollPane,
                  "Unhandled exception has been thrown!",
                  JOptionPane.DEFAULT_OPTION,
                  JOptionPane.ERROR_MESSAGE,
                  null,
                  new Object[]{"Dismiss", "Close application"},
                  null
            );
            errorDialogShown = false;
            if (option == 1) {
               System.exit(1);
            }
         }
      });

      try {
         for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Windows".equals(info.getName())) {
               UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (Exception ex) {
         throw new RuntimeException(ex);
      }

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            new MainFrame().setVisible(true);
         }
      });
   }

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private mydocking.TabManager tabManager1;
   // End of variables declaration//GEN-END:variables
}
