package kitty2;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import mydocking.Tab;
import mydocking.TabContainer;
import mydocking.TabManager;

public class MainFrame extends javax.swing.JFrame {

   private static boolean errorDialogShown = false;
   private final User32 user32 = User32.INSTANCE;
   private final int CUSTOM_MENU_FIRST_ID = 0xFFFF;
   private int CUSTOM_MENU_LAST_ID = CUSTOM_MENU_FIRST_ID;
   private final Map<Integer, CustomMenuActionListener> mapCustomMenuHandlers = new HashMap<>();
   private int sessionsSeparatorPos = 0;
   private Preferences pref;
   private String kittyHome;
   private int setKittyHomeOption;
   private mydocking.TabManager tabManager;

   public MainFrame() {
      tabManager = new TabManager();
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setTitle("KiTTY2");
      getContentPane().setLayout(new GridLayout(1, 1));
      getContentPane().add(tabManager);
      setBounds(0, 0, 1000, 600);

      List<Image> icons = new ArrayList<>();
      icons.add(new ImageIcon(getClass().getResource("/kitty2/images/kitty_portable_16.png")).getImage());
      icons.add(new ImageIcon(getClass().getResource("/kitty2/images/kitty_portable_32.png")).getImage());
      icons.add(new ImageIcon(getClass().getResource("/kitty2/images/kitty_portable_48.png")).getImage());
      icons.add(new ImageIcon(getClass().getResource("/kitty2/images/kitty_portable_256.png")).getImage());
      setIconImages(icons);

      pref = Preferences.userNodeForPackage(MainFrame.class);
      kittyHome = pref.get("kittyHome", "");
      if (kittyHome.isEmpty()) {
         kittyHome = showKittyHomeDialog("");
         if (kittyHome != null) {
            pref.put("kittyHome", kittyHome);
            try {
               pref.flush();
            } catch (BackingStoreException ex) {
               throw new RuntimeException("Can't save settings!", ex);
            }
         } else {
            kittyHome = "";
         }
      }

      addWindowListener(new WindowAdapter() {
         @Override
         public void windowOpened(WindowEvent e) {
            try {
               WinDef.HWND hwnd = new WinDef.HWND(Native.getWindowPointer(MainFrame.this));
               WinDef.HMENU systemMenu = user32.GetSystemMenu(hwnd, new WinDef.BOOL(0));
               File kittyHomeFile = new File(kittyHome);
               if (kittyHomeFile.isDirectory()) {
                  for (File sessions : kittyHomeFile.listFiles()) {
                     if (sessions.isDirectory() && "Sessions".equals(sessions.getName())) {
                        sessionsSeparatorPos = loadSessions(systemMenu, mapCustomMenuHandlers, "", sessions, 0);
                        if (sessionsSeparatorPos > 0) {
                           WinUtil.InsertMenuSeparator(systemMenu, sessionsSeparatorPos, ++CUSTOM_MENU_LAST_ID);
                        }
                     }
                  }
               }
               int nextPos = sessionsSeparatorPos;
               if (nextPos > 0) {
                  nextPos++;
               }
               setKittyHomeOption = ++CUSTOM_MENU_LAST_ID;
               WinUtil.InsertMenuItem(systemMenu, nextPos++, setKittyHomeOption, "Set KiTTY home");
               WinUtil.InsertMenuSeparator(systemMenu, nextPos, ++CUSTOM_MENU_LAST_ID);
               user32.SetWindowLongPtr(hwnd, User32.GWLP_WNDPROC, new WinUtil.WindowProcCallback(hwnd) {
                  @Override
                  public WinDef.LRESULT callback(WinDef.HWND hwnd, WinDef.UINT Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam) {
                     if (Msg.intValue() == WinUser.WM_SYSCOMMAND) {
                        if (mapCustomMenuHandlers.containsKey(wParam.intValue())) {
                           CustomMenuActionListener act = mapCustomMenuHandlers.get(wParam.intValue());
                           if (act != null) {
                              act.actionPerformed();
                           }
                           return new WinDef.LRESULT(0);
                        } else if (wParam.intValue() == setKittyHomeOption) {
                           SwingUtilities.invokeLater(new Runnable() {
                              @Override
                              public void run() {
                                 kittyHome = showKittyHomeDialog(kittyHome);
                                 reloadSessions();
                              }
                           });
                        }
                     } else if (Msg.intValue() == User32.WM_USER.intValue()) {
                        if (wParam.intValue() == 0) {
                           Tab tab = TabContainer.getTab(Integer.toString(lParam.intValue()));
                           if (tab != null) {
                              WinDef.HWND kittyHwnd = ((KittyPanel) tab.getComponent()).getKittyHwnd();
                              WinDef.HMENU popup = user32.GetSystemMenu(kittyHwnd, new WinDef.BOOL(0));
                              Point pointTmp = MouseInfo.getPointerInfo().getLocation();
                              SwingUtilities.convertPointFromScreen(pointTmp, MainFrame.this.getContentPane());
                              WinDef.POINT point = new WinDef.POINT(pointTmp.x, pointTmp.y);
                              user32.ClientToScreen(hwnd, point);
                              WinDef.BOOL command = User32.INSTANCE.TrackPopupMenu(popup, WinUtil.orUINT(User32.TPM_RETURNCMD, User32.TPM_NONOTIFY, User32.TPM_RIGHTBUTTON), point.x, point.y, 0, hwnd, null);
                              if (command.booleanValue()) {
                                 user32.PostMessage(kittyHwnd, new WinDef.UINT(WinUser.WM_SYSCOMMAND), new WinDef.WPARAM(command.longValue()), new WinDef.LPARAM(0));
                              }
                           }
                        }
                        return new WinDef.LRESULT(0);
                     }
                     return super.callback(hwnd, Msg, wParam, lParam);
                  }
               });
            } catch (UnsupportedEncodingException ex) {
               throw new RuntimeException("Error in startup!", ex);
            }
         }
      });
   }

   private int loadSessions(WinDef.HMENU menu, Map<Integer, CustomMenuActionListener> mapListeners, String currentFolder, File root, int position) throws UnsupportedEncodingException {
      List<File> files = new ArrayList<>(Arrays.asList(root.listFiles()));
      Collections.sort(files, new Comparator<File>() {
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

      for (File file : files) {
         String text = URLDecoder.decode(file.getName(), "cp1252");
         if (file.isFile()) {
            if ("Default Settings".equals(text)) {
               continue;
            }
            mapListeners.put(++CUSTOM_MENU_LAST_ID, new CustomMenuActionListener() {
               @Override
               public void actionPerformed() {
                  new KittyPanel(tabManager, kittyHome, (currentFolder.length() > 1) ? (" -folder \"" + currentFolder.substring(1) + "\"") : "", " -load \"" + text + "\"");
               }
            });
            WinUtil.InsertMenuItem(menu, position++, CUSTOM_MENU_LAST_ID, text);
         } else if (file.isDirectory()) {
            WinDef.HMENU popup = user32.CreatePopupMenu();
            loadSessions(popup, mapListeners, currentFolder + "\\" + text, file, 0);
            WinUtil.InsertMenuPopup(menu, position++, popup, text);
         }
      }

      return position;
   }

   private void reloadSessions() {
      WinDef.HWND hwnd = new WinDef.HWND(Native.getWindowPointer(MainFrame.this));
      WinDef.HMENU systemMenu = user32.GetSystemMenu(hwnd, new WinDef.BOOL(0));
      if (sessionsSeparatorPos > 0) {
         for (int i = sessionsSeparatorPos; i >= 0; i--) {
            user32.DeleteMenu(systemMenu, new WinDef.UINT(i), User32.MF_BYPOSITION);
         }
      }
      mapCustomMenuHandlers.clear();
      sessionsSeparatorPos = 0;
      try {
         File kittyHomeFile = new File(kittyHome);
         if (kittyHomeFile.isDirectory()) {
            for (File sessions : kittyHomeFile.listFiles()) {
               if (sessions.isDirectory() && "Sessions".equals(sessions.getName())) {
                  sessionsSeparatorPos = loadSessions(systemMenu, mapCustomMenuHandlers, "", sessions, 0);
                  if (sessionsSeparatorPos > 0) {
                     WinUtil.InsertMenuSeparator(systemMenu, sessionsSeparatorPos, ++CUSTOM_MENU_LAST_ID);
                  }
               }
            }
         }
      } catch (UnsupportedEncodingException ex) {
         throw new RuntimeException("Error reloading esssions!", ex);
      }
   }

   private String showKittyHomeDialog(String startValue) {
      JPanel panel = new JPanel(new BorderLayout(6, 6));
      JLabel label = new JLabel("Choose the directory where kitty_portable.exe is located:");
      panel.add(label, BorderLayout.NORTH);
      JTextField jtf = new JTextField(startValue);
      jtf.setPreferredSize(new Dimension(300, jtf.getPreferredSize().height));
      panel.add(jtf, BorderLayout.CENTER);
      JButton button = new JButton("...");
      button.setMargin(new Insets(2, 3, 2, 2));
      button.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser jfc = new JFileChooser(new File(jtf.getText()));
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
               jtf.setText(jfc.getSelectedFile().getAbsolutePath());
            }
         }
      });
      panel.add(button, BorderLayout.EAST);

      if (JOptionPane.showOptionDialog(
            this,
            panel,
            "Set KiTTY home",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            new Object[]{"Save", "Cancel"},
            null
      ) == 0) {
         return jtf.getText();
      }
      return null;
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
}
