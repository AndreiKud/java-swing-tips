package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,3));
        JList list1 = new JList(makeModel()) {
            @Override public void updateUI() {
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                super.updateUI();
            }
            @Override protected void processMouseMotionEvent(MouseEvent e) {
                super.processMouseMotionEvent(convertMouseEvent(e));
            }
            @Override protected void processMouseEvent(MouseEvent e) {
                if(!getCellBounds(0, getModel().getSize()-1).contains(e.getPoint())) {
                    e.consume();
                    requestFocusInWindow();
                }else{
                    super.processMouseEvent(convertMouseEvent(e));
                }
            }
            private MouseEvent convertMouseEvent(MouseEvent e) {
                //Thread: JList where mouse click acts like ctrl-mouse click
                //http://forums.oracle.com/forums/thread.jspa?messageID=5692411
                return new MouseEvent(
                    (Component) e.getSource(),
                    e.getID(), e.getWhen(),
                    e.getModifiers() | InputEvent.CTRL_MASK,
                    e.getX(), e.getY(),
                    e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(),
                    e.isPopupTrigger(),
                    e.getButton());
            }
        };
        JList list2 = new JList(makeModel()) {
            private ClearSelectionListener listener;
            @Override public void updateUI() {
                removeMouseListener(listener);
                removeMouseMotionListener(listener);
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                super.updateUI();
                if(listener==null) listener = new ClearSelectionListener();
                addMouseListener(listener);
                addMouseMotionListener(listener);
            }
            @Override public void setSelectionInterval(int anchor, int lead) {
                if(anchor==lead && lead>=0 && anchor>=0) {
                    if(listener.isDragging) {
                        addSelectionInterval(anchor, anchor);
                    }else if(!listener.isInCellDragging) {
                        if(isSelectedIndex(anchor)) {
                            removeSelectionInterval(anchor, anchor);
                        }else{
                            addSelectionInterval(anchor, anchor);
                        }
                        listener.isInCellDragging = true;
                    }
                }else{
                    super.setSelectionInterval(anchor, lead);
                }
            }
        };
        add(makeTitledPanel("Default", new JList(makeModel())));
        add(makeTitledPanel("MouseEvent", list1));
        add(makeTitledPanel("SelectionInterval", list2));
        setPreferredSize(new Dimension(320, 240));
    }
    private DefaultListModel makeModel() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("aaaaaaa");
        model.addElement("bbbbbbbbbbbbb");
        model.addElement("cccccccccc");
        model.addElement("ddddddddd");
        model.addElement("eeeeeeeeee");
        return model;
    }
    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(c));
        return p;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ClearSelectionListener extends MouseAdapter {
    private static void clearSelectionAndFocus(JList list) {
        list.getSelectionModel().clearSelection();
        list.getSelectionModel().setAnchorSelectionIndex(-1);
        list.getSelectionModel().setLeadSelectionIndex(-1);
    }
    private static boolean contains(JList list, Point pt) {
        for(int i=0;i<list.getModel().getSize();i++) {
            Rectangle r = list.getCellBounds(i, i);
            if(r.contains(pt)) return true;
        }
        return false;
    }
    private boolean startOutside = false;
    private int     startIndex = -1;
    public boolean  isDragging = false;
    public boolean  isInCellDragging = false;
    @Override public void mousePressed(MouseEvent e) {
        JList list = (JList)e.getSource();
        startOutside = !contains(list, e.getPoint());
        startIndex = list.locationToIndex(e.getPoint());
        if(startOutside) {
            clearSelectionAndFocus(list);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        startOutside = false;
        isDragging = false;
        isInCellDragging = false;
        startIndex = -1;
    }
    @Override public void mouseDragged(MouseEvent e) {
        JList list = (JList)e.getSource();
        if(!isDragging && startIndex == list.locationToIndex(e.getPoint())) {
            isInCellDragging = true;
        }else{
            isDragging = true;
            isInCellDragging = false;
        }
        if(contains(list, e.getPoint())) {
            startOutside = false;
        }else if(startOutside) {
            clearSelectionAndFocus(list);
        }
    }
}
