package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xtn5250TerminalEmulator extends JFrame implements TerminalEmulator {

  private static final int SECRET_CREDENTIAL_ATTR = 39;
  private static final int DEFAULT_ATTR = 32;
  private static final Map<KeyEventMap, AttentionKey> KEY_EVENTS =
      new HashMap<KeyEventMap, AttentionKey>() {
        {
          put(new KeyEventMap(0, KeyEvent.VK_F1), AttentionKey.F1);
          put(new KeyEventMap(0, KeyEvent.VK_F2), AttentionKey.F2);
          put(new KeyEventMap(0, KeyEvent.VK_F3), AttentionKey.F3);
          put(new KeyEventMap(0, KeyEvent.VK_F4), AttentionKey.F4);
          put(new KeyEventMap(0, KeyEvent.VK_F5), AttentionKey.F5);
          put(new KeyEventMap(0, KeyEvent.VK_F6), AttentionKey.F6);
          put(new KeyEventMap(0, KeyEvent.VK_F7), AttentionKey.F7);
          put(new KeyEventMap(0, KeyEvent.VK_F8), AttentionKey.F8);
          put(new KeyEventMap(0, KeyEvent.VK_F9), AttentionKey.F9);
          put(new KeyEventMap(0, KeyEvent.VK_F10), AttentionKey.F10);
          put(new KeyEventMap(0, KeyEvent.VK_F11), AttentionKey.F11);
          put(new KeyEventMap(0, KeyEvent.VK_F12), AttentionKey.F12);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F1), AttentionKey.F13);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F2), AttentionKey.F14);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F3), AttentionKey.F15);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F4), AttentionKey.F16);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F5), AttentionKey.F17);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F6), AttentionKey.F18);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F7), AttentionKey.F19);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F8), AttentionKey.F20);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F9), AttentionKey.F21);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F10), AttentionKey.F22);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F11), AttentionKey.F23);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F12), AttentionKey.F24);
          put(new KeyEventMap(0, KeyEvent.VK_ENTER), AttentionKey.ENTER);
          put(new KeyEventMap(0, KeyEvent.VK_ESCAPE), AttentionKey.ATTN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F4), AttentionKey.CLEAR);
          put(new KeyEventMap(0, KeyEvent.VK_PAUSE), AttentionKey.CLEAR);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE), AttentionKey.SYSRQ);
          put(new KeyEventMap(0, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN), AttentionKey.ROLL_UP);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_UP), AttentionKey.ROLL_DN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F1), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F2), AttentionKey.PA2);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F3), AttentionKey.PA3);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_INSERT), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_HOME), AttentionKey.PA2);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_PAGE_UP), AttentionKey.PA3);
        }
      };
  private static final String TITLE = "Recorder";
  private static final Color BACKGROUND = Color.black;
  private static final int DEFAULT_FONT_SIZE = 14;
  private static final Logger LOG = LoggerFactory.getLogger(Xtn5250TerminalEmulator.class);
  private JButton waitForTextButton = createIconButton("waitForTextButton", "waitForText.png");
  private JButton copyButton = createIconButton("copyButton", "copy.png");
  private JButton pasteButton = createIconButton("pasteButton", "paste.png");
  private JButton labelButton = createIconButton("labelButton", "inputByLabel.png");
  private JButton assertionButton = createIconButton("assertionButton", "assertion.png");
  private JLabel sampleNameLabel = new JLabel("Sample name: ");
  private JTextField sampleNameField = SwingUtils
      .createComponent("sampleNameField", new JTextField());
  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean locked = false;
  private boolean shownCredentials = false;
  private boolean stopping;
  private StatusPanel statusPanel = new StatusPanel();
  private XI5250Crt xi5250Crt = new CustomXI5250Crt();
  private Set<AttentionKey> supportedAttentionKeys;
  private Map<Position, String> labelMap = new HashMap<>();

  public Xtn5250TerminalEmulator() {
    xi5250Crt.setName("Terminal");
    xi5250Crt.setDefBackground(BACKGROUND);
    xi5250Crt.setBlinkingCursor(true);
    xi5250Crt.setEnabled(true);
    setTitle(TITLE);
    setLayout(new BorderLayout());
    add(createToolsPanel(), BorderLayout.NORTH);
    add(xi5250Crt, BorderLayout.CENTER);
    add(statusPanel, BorderLayout.SOUTH);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        Dimension testSize = calculateCrtDefaultSize();
        xi5250Crt.setSize(testSize.width, testSize.height);
        pack();
        xi5250Crt.requestFocus();

      }

      @Override
      public void windowClosed(WindowEvent e) {
        if (!stopping) {
          for (TerminalEmulatorListener g : terminalEmulatorListeners) {
            g.onCloseTerminal();
          }
        }
        statusPanel.dispose();
      }
    });
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    statusPanel.getShowCredentials().addMouseListener(buildOnShowCredentials());
  }

  private static JButton createIconButton(String name, String iconResource) {
    return SwingUtils.createComponent(name, new ThemedIconButton(iconResource));
  }

  private MouseListener buildOnShowCredentials() {
    return new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        shownCredentials = !shownCredentials;
        LOG.info("Credentials are now {}", shownCredentials ? "visible" : "hidden");
        statusPanel.updateShowCredentials(shownCredentials);
        updateFieldsVisibility();
      }

      @Override
      public void mousePressed(MouseEvent e) {

      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {

      }

      @Override
      public void mouseExited(MouseEvent e) {

      }
    };
  }

  private void updateFieldsVisibility() {
    for (XI5250Field field : xi5250Crt.getFields()) {
      int attr = !shownCredentials ? ((ScreenField) field).originalAttr : DEFAULT_ATTR;
      updateFieldAttribute(field.getRow(), field.getCol(), attr);
      field.init();
    }
  }

  private void updateFieldAttribute(int row, int column, int attr) {
    xi5250Crt.drawString("\u0001", column - 1, row, attr);
  }

  private JPanel createToolsPanel() {
    JPanel toolsPanel = new JPanel();
    GroupLayout layout = new GroupLayout(toolsPanel);
    layout.setAutoCreateContainerGaps(true);
    toolsPanel.setLayout(layout);
    JPanel sampleNamePanel = buildSampleNamePanel();
    JPanel fillPanel = new JPanel();

    copyButton.setToolTipText("Copy");
    pasteButton.setToolTipText("Paste");
    labelButton.setToolTipText("Input by label");
    waitForTextButton.setToolTipText("Text wait condition");
    assertionButton.setToolTipText("Assertion");
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(copyButton)
        .addComponent(pasteButton)
        .addComponent(labelButton)
        .addComponent(waitForTextButton)
        .addComponent(assertionButton)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(fillPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup()
            .addComponent(sampleNamePanel)
        )
    );
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(sampleNamePanel, Alignment.CENTER)
        .addComponent(copyButton)
        .addComponent(pasteButton)
        .addComponent(labelButton)
        .addComponent(waitForTextButton)
        .addComponent(assertionButton)
        .addComponent(fillPanel)
    );
    return toolsPanel;
  }

  private JPanel buildSampleNamePanel() {
    JPanel sampleNamePanel = new JPanel();
    GroupLayout layout = new GroupLayout(sampleNamePanel);
    sampleNamePanel.setLayout(layout);
    sampleNameField.setFont(new Font("SansSerif", Font.PLAIN, 11));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(sampleNameLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(sampleNameField, GroupLayout.PREFERRED_SIZE, 150,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(sampleNameLabel)
        .addComponent(sampleNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE));

    return sampleNamePanel;
  }

  @Override
  public void start() {
    setVisible(true);
  }

  @Override
  public void setScreenSize(int columns, int rows) {
    xi5250Crt.setCrtSize(columns, rows);
  }

  private Dimension calculateCrtDefaultSize() {
    FontMetrics fm = xi5250Crt.getFontMetrics(
        new Font(xi5250Crt.getFont().getName(), xi5250Crt.getFont().getStyle(),
            DEFAULT_FONT_SIZE));
    return new Dimension(fm.charWidth('W') * xi5250Crt.getCrtSize().width,
        fm.getHeight() * xi5250Crt.getCrtSize().height);
  }

  @Override
  public void stop() {
    stopping = true;
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void setCursor(int row, int col) {
    xi5250Crt.setCursorPos(col - 1, row - 1);
    this.statusPanel.updateStatusBarCursorPosition(row, col);
  }

  @VisibleForTesting
  public String getScreen() {
    int height = xi5250Crt.getCrtSize().height;
    int width = xi5250Crt.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(xi5250Crt.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  @Override
  public synchronized void setScreen(Screen screen, String screenName) {
    Dimension screenSize = screen.getSize();
    setScreenSize(screenSize.width, screenSize.height);
    xi5250Crt.clear();
    xi5250Crt.removeFields();
    for (Screen.Segment s : screen.getSegments()) {
      int row = s.getStartPosition().getRow() - 1;
      int column = s.getStartPosition().getColumn() - 1;
      if (s.isEditable()) {
        int attr =
            !shownCredentials && s.isSecret() ? SECRET_CREDENTIAL_ATTR : DEFAULT_ATTR;
        updateFieldAttribute(row, column, attr);

        XI5250Field xi5250Field = new ScreenField(xi5250Crt, column,
            row, s.getText().length(), attr);

        xi5250Field.setString(s.getText());
        xi5250Field.resetMDT();

        xi5250Crt.addField(xi5250Field);
      } else {
        xi5250Crt
            .drawString(s.getText(), column,
                row, DEFAULT_ATTR);
      }
    }
    xi5250Crt.initAllFields();
    sampleNameField.setText(screenName);
  }

  @VisibleForTesting
  public void setSelectedArea(Rectangle rectangle) {
    xi5250Crt.setSelectedArea(rectangle);
  }

  @Override
  public void soundAlarm() {
    Toolkit.getDefaultToolkit().beep();
    statusPanel.soundAlarm();
  }

  @Override
  public void setKeyboardLock(boolean lock) {
    this.locked = lock;
    this.statusPanel.setKeyboardStatus(lock);
  }

  @Override
  public void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener) {
    terminalEmulatorListeners.add(terminalEmulatorListener);
  }

  @Override
  public void setSupportedAttentionKeys(Set<AttentionKey> supportedAttentionKeys) {
    this.supportedAttentionKeys = supportedAttentionKeys;
  }

  private boolean isAttentionKeyValid(AttentionKey attentionKey) {
    return supportedAttentionKeys.contains(attentionKey);
  }

  private List<Input> getInputFields() {
    List<Input> fields = new ArrayList<>();

    for (XI5250Field f : xi5250Crt.getFields()) {
      if (f.isMDTOn()) {

        Position fieldPosition = new Position(f.getRow() + 1, f.getCol() + 1);
        String label = labelMap.get(fieldPosition);
        String trimmedInput = trimNulls(f.getString());
        if (label != null) {
          fields.add(new LabelInput(label, trimmedInput));
        } else {
          fields.add(new CoordInput(fieldPosition, trimmedInput));
        }
      }
    }
    labelMap.clear();
    return fields;
  }

  private String trimNulls(String str) {
    if (str.isEmpty()) {
      return str;
    }
    int firstNotNull = 0;
    while (str.charAt(firstNotNull) == '\u0000') {
      firstNotNull++;
    }
    int lastNotNull = str.length() - 1;
    while (str.charAt(lastNotNull) == '\u0000') {
      lastNotNull--;
    }
    return str.substring(firstNotNull, lastNotNull + 1);
  }

  private static class ScreenField extends XI5250Field {

    private int originalAttr;

    private ScreenField(XI5250Crt aCrt, int aCol, int aRow, int aLen, int aAttr) {
      super(aCrt, aCol, aRow, aLen, aAttr);
      originalAttr = aAttr;
    }
  }

  private static class KeyEventMap {

    private final int modifier;
    private final int specialKey;

    KeyEventMap(int modifier, int specialKey) {
      this.modifier = modifier;
      this.specialKey = specialKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      KeyEventMap that = (KeyEventMap) o;
      return modifier == that.modifier &&
          specialKey == that.specialKey;
    }

    @Override
    public int hashCode() {
      return Objects.hash(modifier, specialKey);
    }
  }

  private class CustomXI5250Crt extends XI5250Crt {

    private boolean copyPaste = false;

    private CustomXI5250Crt() {
      copyButton.addActionListener(e -> {
        doCopy();
        xi5250Crt.requestFocus();
      });
      pasteButton.addActionListener(e -> {
        doPaste();
        xi5250Crt.requestFocus();
      });
      labelButton.addActionListener(e -> {
        String labelText = xi5250Crt.getStringSelectedArea();
        if (labelText == null) {
          warnUserOfNotScreenSelectedArea("input by label");
        } else if (labelText.contains("\n")) {
          showUserMessage("Please try again selecting one row");
          LOG.warn(
              "Input by label does not support multiple selected rows, "
                  + "please select just one row.");
        } else if (labelText.trim().isEmpty()) {
          showUserMessage("Please select a non empty or blank text \nto be used as input by label");
          LOG.warn(
              "Selected text is composed only by spaces.");
        } else {
          XI5250Field field = xi5250Crt
              .getNextFieldFromPos(xi5250Crt.getSelectedArea().x, xi5250Crt.getSelectedArea().y);

          if (isFieldValid(field)) {
            labelMap.put(new Position(field.getRow() + 1, field.getCol() + 1), labelText.trim());
          } else {
            showUserMessage("No input fields found near to \"" + labelText + "\".");
            LOG.warn("No field was found after specified label {}", labelText);
          }

        }

        xi5250Crt.requestFocus();
        xi5250Crt.clearSelectedArea();
      });

      waitForTextButton.addActionListener(e -> {
        String selectedText = xi5250Crt.getStringSelectedArea();

        if (selectedText != null) {
          for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
            listener.onWaitForText(selectedText);
          }
        } else {
          warnUserOfNotScreenSelectedArea("wait text condition");
        }
        xi5250Crt.requestFocus();
        xi5250Crt.clearSelectedArea();
      });

      assertionButton.addActionListener(e -> {
        String selectedText = xi5250Crt.getStringSelectedArea();
        if (selectedText != null) {
          String assertionName = requestAssertionName();
          if (assertionName != null) {
            Pattern pattern = JMeterUtils
                .getPattern(Perl5Compiler.quotemeta(selectedText).replace("\\\n", ".*\\n.*"));

            for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
              listener
                  .onAssertionScreen(assertionName, pattern.getPattern());
            }
          }
        } else {
          warnUserOfNotScreenSelectedArea("assertion");
        }
        xi5250Crt.requestFocus();
        xi5250Crt.clearSelectedArea();
      });
    }

    private boolean isFieldValid(XI5250Field field) {
      int width = xi5250Crt.getCrtSize().width;
      int linearLabelPosition = width * xi5250Crt.getSelectedArea().y + xi5250Crt
          .getSelectedArea().x;
      int linearFieldPosition = width * field.getRow() + field.getCol();
      return linearFieldPosition > linearLabelPosition;
    }

    private void showUserMessage(String msg) {
      JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warnUserOfNotScreenSelectedArea(String usage) {
      showUserMessage("Please select a part of the screen");
      LOG.warn(
          "The selection of a screen area is essential to "
              + "be used as {} later on.", usage);
    }

    private String requestAssertionName() {
      return JOptionPane.showInputDialog(this, "Insert name of assertion", "Response Assertion");
    }

    @Override
    protected synchronized void processKeyEvent(KeyEvent e) {
      AttentionKey attentionKey = null;
      if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_C
          && e.getModifiers() == getMenuShortcutKeyMask()) {
        doCopy();
        e.consume();
        copyPaste = true;
      } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_V
          && e.getModifiers() == getMenuShortcutKeyMask() && !locked) {
        doPaste();
        e.consume();
        copyPaste = true;
      } else if (e.getID() == KeyEvent.KEY_RELEASED &&
          e.getKeyCode() == getKeyCodeFromKeyMask(getMenuShortcutKeyMask()) && copyPaste) {
        copyPaste = false;
      } else if (isAnyKeyPressedOrControlKeyReleasedAndNotCopy(e)) {
        attentionKey = KEY_EVENTS
            .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));

        if (attentionKey != null) {
          if (isAttentionKeyValid(attentionKey)) {
            List<Input> fields = getInputFields();
            for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
              listener.onAttentionKey(attentionKey, fields, sampleNameField.getText());
            }
          } else {
            showUserMessage(attentionKey + " not supported for current protocol");
            e.consume();
          }
        }
      }
      if ((!locked && !e.isConsumed()) || attentionKey != null) {
        //By default XI5250Crt only move the cursor when the backspace key is pressed and delete
        // when shift mask is enabled, in this way always delete
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
          super.processKeyEvent(
              new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), KeyEvent.SHIFT_MASK,
                  e.getKeyCode(), e.getKeyChar(), e.getKeyLocation()));
        } else {
          super.processKeyEvent(e);
        }
        statusPanel
            .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
      }

    }

    private int getMenuShortcutKeyMask() {
      return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }

    private int getKeyCodeFromKeyMask(int keyMask) {
      if (keyMask == KeyEvent.META_MASK) {
        return KeyEvent.VK_META;
      } else {
        return KeyEvent.VK_CONTROL;
      }
    }
    //This is implemented in this way because ctrl key is used in a shortcut to
    //copy/paste and the attention key should be triggered only if have not
    //executed a copy paste before.

    private boolean isAnyKeyPressedOrControlKeyReleasedAndNotCopy(KeyEvent e) {
      return (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() != getKeyCodeFromKeyMask(
          getMenuShortcutKeyMask())) || (e.getID() == KeyEvent.KEY_RELEASED
          && e.getKeyCode() == getKeyCodeFromKeyMask(getMenuShortcutKeyMask()) && !copyPaste);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
      super.processMouseEvent(e);
      statusPanel
          .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
    }

    @Override
    public void paintComponent(Graphics g) {
      synchronized (Xtn5250TerminalEmulator.this) {
        super.paintComponent(g);
      }
    }

  }
}
