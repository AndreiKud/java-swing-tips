// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane() {
      private final JPopupMenu popup1 = makeTabPopupMenu();
      private final JPopupMenu popup2 = makeTabAreaPopupMenu();

      @Override public void updateUI() {
        super.updateUI();
        EventQueue.invokeLater(() -> {
          SwingUtilities.updateComponentTreeUI(popup1);
          SwingUtilities.updateComponentTreeUI(popup2);
          setComponentPopupMenu(popup1);
        });
      }

      @Override public Point getPopupLocation(MouseEvent e) {
        int idx = indexAtLocation(e.getX(), e.getY());
        if (idx < 0 && getTabAreaBounds().contains(e.getPoint())) {
          setComponentPopupMenu(popup2);
        } else {
          setComponentPopupMenu(popup1);
        }
        return super.getPopupLocation(e);
      }

      private Rectangle getTabAreaBounds() {
        Rectangle tabbedRect = getBounds();
        Rectangle compRect = Optional.ofNullable(getSelectedComponent())
            .map(Component::getBounds)
            .orElseGet(Rectangle::new);
        int tabPlacement = getTabPlacement();
        if (isTopBottomTabPlacement(tabPlacement)) {
          tabbedRect.height = tabbedRect.height - compRect.height;
          if (tabPlacement == BOTTOM) {
            tabbedRect.y += compRect.y + compRect.height;
          }
        } else {
          tabbedRect.width = tabbedRect.width - compRect.width;
          if (tabPlacement == RIGHT) {
            tabbedRect.x += compRect.x + compRect.width;
          }
        }
        return tabbedRect;
      }

      private boolean isTopBottomTabPlacement(int tabPlacement) {
        return tabPlacement == TOP || tabPlacement == BOTTOM;
      }

      // @Override public JPopupMenu getComponentPopupMenu() {
      //   int idx = getSelectedIndex();
      //   Component c = getTabComponentAt(idx);
      //   JPopupMenu popup;
      //   if (idx>= 0 && c instanceof JComponent) {
      //     popup = ((JComponent) c).getComponentPopupMenu();
      //   } else {
      //     popup = super.getComponentPopupMenu();
      //   }
      //   return popup;
      // }
    };
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("Title: 0", new JScrollPane(new JTextArea()));
    add(tabbedPane);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makeTabPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String title = "Title: " + tabs.getTabCount();
      tabs.addTab(title, new JScrollPane(new JTextArea()));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    popup.addSeparator();
    JMenuItem rename = popup.add("Rename");
    rename.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String name = tabs.getTitleAt(tabs.getSelectedIndex());
      JTextField textField = new JTextField(name);
      int result = JOptionPane.showConfirmDialog(
          tabs, textField, rename.getText(),
          JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (result == JOptionPane.OK_OPTION) {
        String str = textField.getText().trim();
        if (!str.equals(name)) {
          tabs.setTitleAt(tabs.getSelectedIndex(), str);
        }
      }
    });
    popup.addSeparator();
    JMenuItem close = popup.add("Close");
    close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
    close.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.remove(tabs.getSelectedIndex());
    });
    JMenuItem closeAll = popup.add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      tabs.removeAll();
    });
    JMenuItem closeAllButActive = popup.add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      int idx = tabs.getSelectedIndex();
      String title = tabs.getTitleAt(idx);
      Component cmp = tabs.getComponentAt(idx);
      tabs.removeAll();
      tabs.addTab(title, cmp);
    });
    return popup;
  }

  private static JPopupMenu makeTabAreaPopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("New tab").addActionListener(e -> {
      JTabbedPane tabs = (JTabbedPane) popup.getInvoker();
      String title = "Title: " + tabs.getTabCount();
      tabs.addTab(title, new JScrollPane(new JTextArea()));
      tabs.setSelectedIndex(tabs.getTabCount() - 1);
    });
    popup.addSeparator();
    ButtonGroup group = new ButtonGroup();
    ItemListener handler = e -> {
      Component c = popup.getInvoker();
      if (e.getStateChange() == ItemEvent.SELECTED && c instanceof JTabbedPane) {
        ButtonModel m = group.getSelection();
        TabPlacement tp = TabPlacement.valueOf(m.getActionCommand());
        ((JTabbedPane) c).setTabPlacement(tp.getPlacement());
      }
    };
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler);
      item.setActionCommand(name);
      popup.add(item);
      group.add(item);
    });
    return popup;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

enum TabPlacement {
  TOP(SwingConstants.TOP),
  LEFT(SwingConstants.LEFT),
  BOTTOM(SwingConstants.BOTTOM),
  RIGHT(SwingConstants.RIGHT);

  private final int placement;

  TabPlacement(int placement) {
    this.placement = placement;
  }

  public int getPlacement() {
    return placement;
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
