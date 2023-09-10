// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        return new Dimension(32, 32);
      }
    };
    label.setOpaque(true);
    label.setBackground(Color.RED);

    UIManager.put("ColorChooser.rgbHexCodeText", "#RGBA:");
    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      JColorChooser cc = new JColorChooser();
      cc.setColor(new Color(0xFF_FF_00_00, true));
      AbstractColorChooserPanel[] panels = cc.getChooserPanels();
      List<AbstractColorChooserPanel> choosers = new ArrayList<>(Arrays.asList(panels));
      AbstractColorChooserPanel ccp = choosers.get(3);
      // Java 9: if (ccp.isColorTransparencySelectionEnabled()) {
      for (Component c : ccp.getComponents()) {
        if (c instanceof JFormattedTextField) {
          removeFocusListeners(c);
          // c.removePropertyChangeListener("value", (PropertyChangeListener) ccp);
          // javax.swing.colorchooser.ValueFormatter.init(8, true, (JFormattedTextField) c);
          ValueFormatter.init((JFormattedTextField) c);
        }
      }
      cc.setChooserPanels(choosers.toArray(new AbstractColorChooserPanel[0]));

      ColorTracker ok = new ColorTracker(cc);
      Component parent = getRootPane();
      String title = "JColorChooser";
      JDialog dialog = JColorChooser.createDialog(parent, title, true, cc, ok, null);
      dialog.addComponentListener(new ComponentAdapter() {
        @Override public void componentHidden(ComponentEvent e) {
          ((Window) e.getComponent()).dispose();
        }
      });
      dialog.setVisible(true);
      Color color = ok.getColor();
      if (color != null) {
        label.setBackground(color);
      }
    });

    add(label);
    add(button);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void removeFocusListeners(Component c) {
    for (FocusListener l : c.getFocusListeners()) {
      if (l instanceof JFormattedTextField.AbstractFormatter) {
        c.removeFocusListener(l);
      }
    }
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser c) {
    chooser = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}

// copied from javax/swing/colorchooser/ValueFormatter.java
class ValueFormatter extends JFormattedTextField.AbstractFormatter implements FocusListener {
  // private final int length;
  // private final int radix;
  // private JFormattedTextField text;
  private final transient DocumentFilter filter = new DocumentFilter() {
    @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      if (isValidLength(fb.getDocument().getLength() - length)) {
        fb.remove(offset, length);
      }
    }

    @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet set) throws BadLocationException {
      if (isValidLength(fb.getDocument().getLength() + text.length() - length) && isValid(text)) {
        fb.replace(offset, length, text.toUpperCase(Locale.ENGLISH), set);
      }
    }

    @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet set) throws BadLocationException {
      if (isValidLength(fb.getDocument().getLength() + text.length()) && isValid(text)) {
        fb.insertString(offset, text.toUpperCase(Locale.ENGLISH), set);
      }
    }

    private boolean isValidLength(int len) {
      return 0 <= len && len <= 8;
    }

    private boolean isValid(String text) {
      int len = text.length();
      for (int i = 0; i < len; i++) {
        char ch = text.charAt(i);
        if (Character.digit(ch, 16) < 0) {
          return false;
        }
      }
      return true;
    }
  };

  // protected ValueFormatter(int length, boolean hex) {
  //   this.length = length;
  //   this.radix = hex ? 16 : 10;
  // }

  public static void init(JFormattedTextField text) {
    ValueFormatter formatter = new ValueFormatter();
    text.setColumns(8);
    text.setFormatterFactory(new DefaultFormatterFactory(formatter));
    text.setHorizontalAlignment(SwingConstants.RIGHT);
    text.setMinimumSize(text.getPreferredSize());
    text.addFocusListener(formatter);
  }

  @Override public Object stringToValue(String text) throws ParseException {
    try {
      int r = Integer.parseInt(text.substring(0, 2), 16);
      int g = Integer.parseInt(text.substring(2, 4), 16);
      int b = Integer.parseInt(text.substring(4, 6), 16);
      int a = Integer.parseInt(text.substring(6), 16);
      return (a << 24) | (r << 16) | (g << 8) | b;
      // return Integer.valueOf(argb, 16); // <- NumberFormatException
    } catch (NumberFormatException nfe) {
      ParseException pe = new ParseException("illegal format", 0);
      pe.initCause(nfe);
      throw pe;
    }
  }

  @Override public String valueToString(Object object) throws ParseException {
    if (object instanceof Integer) {
      int value = (Integer) object;
      // String str = "00" + Integer.toHexString(value).toUpperCase();
      char[] array = new char[8];
      for (int i = array.length - 1; i >= 0; i--) {
        array[i] = Character.forDigit(value & 0x0F, 16);
        value >>= 4;
      }
      // while (0 < index--) {
      //   array[index] = Character.forDigit(value & 0x0F, 16);
      //   value >>= 4;
      // }
      String argb = String.valueOf(array).toUpperCase(Locale.ENGLISH);
      return argb.substring(2) + argb.substring(0, 2);
    }
    throw new ParseException("illegal object", 0);
  }

  @Override protected DocumentFilter getDocumentFilter() {
    return filter;
  }

  @Override public void focusGained(FocusEvent e) {
    Object source = e.getSource();
    if (source instanceof JFormattedTextField) {
      JFormattedTextField text = (JFormattedTextField) source;
      SwingUtilities.invokeLater(text::selectAll);
    }
  }

  @Override public void focusLost(FocusEvent e) {
    // Do nothing
  }
}
