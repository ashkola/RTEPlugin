package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
import com.blazemeter.jmeter.rte.recorder.RTERecorderPanel;
import com.blazemeter.jmeter.rte.recorder.RecordingStateListener;
import kg.apc.emulators.TestJMeterUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.VerificationCollector;


import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderGuiTest {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  private final String SERVER = "localhost";
  private final int PORT = 23;
  private final Protocol PROTOCOL = Protocol.TN5250;
  private final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient().getDefaultTerminalType();
  private final SSLType SSL_TYPE = SSLType.NONE;
  private final long TIMEOUT = 5000;

  public RTERecorderGui rteRecorderGui;

  @Mock
  private RTERecorder testElement;

  @Mock
  private RTERecorderPanel panel;

  @Mock
  private RecordingStateListener listener;

  @Before
  public void setup() {
    rteRecorderGui = new RTERecorderGui();
    prepareTestElement();
  }

  private void prepareTestElement(){
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(TIMEOUT);
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart() throws Exception {
    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();

    verify(testElement).onRecordingStart();
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop() throws Exception {
    prepareTestElement();

    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();
    rteRecorderGui.onRecordingStop();

    verify(testElement).onRecordingStop();
  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure(){
    RTERecorder configurationElement    = buildRTERecorderToBeConfigured();
    RTERecorderGui rteRecorderGui = new RTERecorderGui(panel);

    /*
    listener = Mockito.mock(RecordingStateListener.class);
    RTERecorderPanel mockRecordingPanel = new RTERecorderPanel(listener);
    RTERecorderPanel spyRecordingPanel = Mockito.spy(mockRecordingPanel);
    RTERecorderGui rteRecorderGuiWithMockPanel = new RTERecorderGui(spyRecordingPanel);
    */

    rteRecorderGui.configure(configurationElement);

    verify(panel).setServer(SERVER);
    verify(panel).setPort(Long.toString(PORT));
    verify(panel).setProtocol(PROTOCOL);
    verify(panel).setTerminalType(TERMINAL_TYPE);
    verify(panel).setSSLType(SSL_TYPE);
    verify(panel).setConnectionTimeout(Long.toString(TIMEOUT));
  }

  private RTERecorder buildRTERecorderToBeConfigured(){
    RTERecorder configurationElement = new RTERecorder();
    configurationElement.setServer(SERVER);
    configurationElement.setPort(String.valueOf(PORT));
    configurationElement.setProtocol(PROTOCOL);
    configurationElement.setTerminalType(TERMINAL_TYPE);
    configurationElement.setSSLType(SSL_TYPE);
    configurationElement.setConnectionTimeout(Long.toString(TIMEOUT));

    return configurationElement;
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {
    RTERecorder expected = buildRTERecorderToBeConfigured();
    RTERecorder modified = new RTERecorder();

    rteRecorderGui.configure(expected);
    rteRecorderGui.modifyTestElement(modified);

    assertRecorder(expected, modified);
  }

  private void assertRecorder(RTERecorder result, RTERecorder expected) {
    assertThat(result)
            .isEqualToComparingOnlyGivenFields(expected, "port", "server", "protocol", "terminalType", "connectionTimeout");
  }
}
