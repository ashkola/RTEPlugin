package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.SSLData;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTESampler extends AbstractSampler implements ThreadListener {

  public static final String CONFIG_PORT = "RTEConnectionConfig.port";
  public static final String CONFIG_SERVER = "RTEConnectionConfig.server";
  public static final String CONFIG_USER = "RTEConnectionConfig.user";
  public static final String CONFIG_PASS = "RTEConnectionConfig.pass";
  public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.protocol";
  public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.sslType";
  public static final String CONFIG_CONNECTION_TIMEOUT = "RTEConnectionConfig.connectTimeout";
  public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.terminalType";
  public static final Action DEFAULT_ACTION = Action.ENTER;
  public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
  public static final TerminalType DEFAULT_TERMINAL_TYPE = TerminalType.IBM_3179_2;
  public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;
  private static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60000;
  private static final int DEFAULT_PORT = 23;

  private static final String CONFIG_STABLE_TIMEOUT = "RTEConnectionConfig.stableTimeout";
  private static final long DEFAULT_STABLE_TIMEOUT_MILLIS = 1000;
  private static final String DISCONNECT_PROPERTY = "RTESampler.disconnect";
  private static final String SEND_INPUTS_PROPERTY = "RTESampler.SendInputs";
  private static final String WAIT_SYNC_PROPERTY = "RTESampler.waitSync";
  private static final String WAIT_SYNC_TIMEOUT_PROPERTY = "RTESampler.waitSyncTimeout";
  private static final long DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS = 60000;
  private static final String WAIT_CURSOR_PROPERTY = "RTESampler.waitCursor";
  private static final String WAIT_CURSOR_ROW_PROPERTY = "RTESampler.waitCursorRow";
  private static final String WAIT_CURSOR_COLUMN_PROPERTY = "RTESampler.waitCursorColumn";
  private static final String WAIT_CURSOR_TIMEOUT_PROPERTY = "RTESampler.waitCursorTimeout";
  private static final long DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS = 30000;
  private static final String WAIT_SILENT_PROPERTY = "RTESampler.waitSilent";
  private static final String WAIT_SILENT_TIME_PROPERTY = "RTESampler.waitSilentTime";
  private static final long DEFAULT_WAIT_SILENT_TIME_MILLIS = 1000;
  private static final String WAIT_SILENT_TIMEOUT_PROPERTY = "RTESampler.waitSilentTimeout";
  private static final long DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS = 60000;
  private static final String WAIT_TEXT_PROPERTY = "RTESampler.waitText";
  private static final String WAIT_TEXT_REGEX_PROPERTY = "RTESampler.waitTextRegex";
  private static final String WAIT_TEXT_AREA_TOP_PROPERTY = "RTESampler.waitTextAreaTop";
  private static final String WAIT_TEXT_AREA_LEFT_PROPERTY = "RTESampler.waitTextAreaLeft";
  private static final String WAIT_TEXT_AREA_BOTTOM_PROPERTY = "RTESampler.waitTextAreaBottom";
  private static final String WAIT_TEXT_AREA_RIGHT_PROPERTY = "RTESampler.waitTextAreaRight";
  private static final String WAIT_TEXT_TIMEOUT_PROPERTY = "RTESampler.waitTimeout";
  private static final long DEFAULT_WAIT_TEXT_TIMEOUT = 30000;
  private static final String ACTION_PROPERTY = "RTESampler.action";

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);
  private static ThreadLocal<Map<String, RteProtocolClient>> connections = ThreadLocal
      .withInitial(HashMap::new);
  private final Function<Protocol, RteProtocolClient> protocolFactory;
  private SampleResult sampleResult;

  public RTESampler() {
    this(Protocol::createProtocolClient);
  }

  public RTESampler(Function<Protocol, RteProtocolClient> protocolFactory) {
    setName("RTE");
    this.protocolFactory = protocolFactory;
  }

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  @Override
  public void setName(String name) {
    if (name != null) {
      setProperty(TestElement.NAME, name);
    }
  }

  private Protocol getProtocol() {
    return Protocol.valueOf(getPropertyAsString(CONFIG_PROTOCOL));
  }

  private String getServer() {
    return getPropertyAsString(CONFIG_SERVER);
  }

  private int getPort() {
    return getPropertyAsInt(CONFIG_PORT, DEFAULT_PORT);
  }

  private TerminalType getTerminalType() {
    return TerminalType.valueOf(getPropertyAsString(CONFIG_TERMINAL_TYPE));
  }

  private long getConnectionTimeout() {
    return getPropertyAsLong(CONFIG_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

  private long getStableTimeout() {
    return getPropertyAsLong(CONFIG_STABLE_TIMEOUT, DEFAULT_STABLE_TIMEOUT_MILLIS);
  }

  public void setPayload(Inputs payload) {
    setProperty(new TestElementProperty(Inputs.INPUTS, payload));
  }

  private String getUser() {
    return getPropertyAsString(CONFIG_USER);
  }

  private String getPass() {
    return getPropertyAsString(CONFIG_PASS);
  }

  private SSLType getSSLType() {
    return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
  }

  public Action getAction() {
    if (getPropertyAsString(ACTION_PROPERTY).isEmpty()) {
      return DEFAULT_ACTION;
    }
    return Action.valueOf(getPropertyAsString(ACTION_PROPERTY));
  }

  public void setAction(Action action) {
    setProperty(ACTION_PROPERTY, action.name());
  }

  public boolean getDisconnect() {
    return getPropertyAsBoolean(DISCONNECT_PROPERTY);
  }

  public void setDisconnect(boolean disconnect) {
    setProperty(DISCONNECT_PROPERTY, disconnect);
  }

  public boolean getSendInputs() {
    return getPropertyAsBoolean(SEND_INPUTS_PROPERTY, true);
  }

  public void setSendInputs(boolean sendInputs) {
    setProperty(SEND_INPUTS_PROPERTY, sendInputs);
  }

  public boolean getWaitSync() {
    return getPropertyAsBoolean(WAIT_SYNC_PROPERTY, true);
  }

  public void setWaitSync(boolean waitSync) {
    setProperty(WAIT_SYNC_PROPERTY, waitSync);
  }

  public String getWaitSyncTimeout() {
    return getPropertyAsString(WAIT_SYNC_TIMEOUT_PROPERTY, "" + DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public void setWaitSyncTimeout(String waitTimeoutSync) {
    setProperty(WAIT_SYNC_TIMEOUT_PROPERTY, waitTimeoutSync);
  }

  private long getWaitSyncTimeoutValue() {
    return getPropertyAsLong(WAIT_SYNC_TIMEOUT_PROPERTY, DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public boolean getWaitCursor() {
    return getPropertyAsBoolean(WAIT_CURSOR_PROPERTY);
  }

  public void setWaitCursor(boolean waitCursor) {
    setProperty(WAIT_CURSOR_PROPERTY, waitCursor);
  }

  public String getWaitCursorRow() {
    return getPropertyAsString(WAIT_CURSOR_ROW_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorRow(String row) {
    setProperty(WAIT_CURSOR_ROW_PROPERTY, row);
  }

  public String getWaitCursorColumn() {
    return getPropertyAsString(WAIT_CURSOR_COLUMN_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorColumn(String row) {
    setProperty(WAIT_CURSOR_COLUMN_PROPERTY, row);
  }

  public String getWaitCursorTimeout() {
    return getPropertyAsString(WAIT_CURSOR_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS));
  }

  public void setWaitCursorTimeout(String waitTimeoutCursor) {
    setProperty(WAIT_CURSOR_TIMEOUT_PROPERTY, waitTimeoutCursor);
  }

  public boolean getWaitSilent() {
    return getPropertyAsBoolean(WAIT_SILENT_PROPERTY);
  }

  public void setWaitSilent(boolean waitSilent) {
    setProperty(WAIT_SILENT_PROPERTY, waitSilent);
  }

  public String getWaitSilentTime() {
    return getPropertyAsString(WAIT_SILENT_TIME_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIME_MILLIS));
  }

  public void setWaitSilentTime(String waitSilentTime) {
    setProperty(WAIT_SILENT_TIME_PROPERTY, waitSilentTime);
  }

  public String getWaitSilentTimeout() {
    return getPropertyAsString(WAIT_SILENT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS));
  }

  public void setWaitSilentTimeout(String waitSilentTimeout) {
    setProperty(WAIT_SILENT_TIMEOUT_PROPERTY, waitSilentTimeout);
  }

  public boolean getWaitText() {
    return getPropertyAsBoolean(WAIT_TEXT_PROPERTY);
  }

  public void setWaitText(boolean waitText) {
    setProperty(WAIT_TEXT_PROPERTY, waitText);
  }

  public String getWaitTextRegex() {
    return getPropertyAsString(WAIT_TEXT_REGEX_PROPERTY);
  }

  public void setWaitTextRegex(String regex) {
    setProperty(WAIT_TEXT_REGEX_PROPERTY, regex);
  }

  public String getWaitTextAreaTop() {
    return getPropertyAsString(WAIT_TEXT_AREA_TOP_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaTop(String row) {
    setProperty(WAIT_TEXT_AREA_TOP_PROPERTY, row);
  }

  private int getWaitTextAreaTopValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_TOP_PROPERTY, 1);
  }

  public String getWaitTextAreaLeft() {
    return getPropertyAsString(WAIT_TEXT_AREA_LEFT_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaLeft(String column) {
    setProperty(WAIT_TEXT_AREA_LEFT_PROPERTY, column);
  }

  private int getWaitTextAreaLeftValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_LEFT_PROPERTY, 1);
  }

  public String getWaitTextAreaBottom() {
    return getPropertyAsString(WAIT_TEXT_AREA_BOTTOM_PROPERTY);
  }

  public void setWaitTextAreaBottom(String row) {
    setProperty(WAIT_TEXT_AREA_BOTTOM_PROPERTY, row);
  }

  private int getWaitTextAreaBottomValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_BOTTOM_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextAreaRight() {
    return getPropertyAsString(WAIT_TEXT_AREA_RIGHT_PROPERTY);
  }

  public void setWaitTextAreaRight(String column) {
    setProperty(WAIT_TEXT_AREA_RIGHT_PROPERTY, column);
  }

  private int getWaitTextAreaRightValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_RIGHT_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextTimeout() {
    return getPropertyAsString(WAIT_TEXT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_TEXT_TIMEOUT));
  }

  public void setWaitTextTimeout(String timeout) {
    setProperty(WAIT_TEXT_TIMEOUT_PROPERTY, timeout);
  }

  private long getWaitTextTimeoutValue() {
    return getPropertyAsLong(WAIT_TEXT_TIMEOUT_PROPERTY, DEFAULT_WAIT_TEXT_TIMEOUT);
  }

  @Override
  public SampleResult sample(Entry entry) {

    sampleResult = new SampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.sampleStart();

    try {
      RteProtocolClient client = getClient();
      try {
        if (getSendInputs()) {
          client.send(getCoordInputs(), getAction(), getWaitersList());
        }
        sampleResult.setSuccessful(true);
        sampleResult.setResponseData(client.getScreen(), "utf-8");
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.sampleEnd();
        return sampleResult;
      } finally {
        if (getDisconnect()) {
          disconnect(client);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return errorResult("The sampling has been interrupted", e);
    } catch (Exception e) {
      return errorResult("Error while sampling the remote terminal", e);
    }
  }

  private RteProtocolClient getClient()
      throws RteIOException, InterruptedException, TimeoutException {
    String clientId = buildConnectionId();
    Map<String, RteProtocolClient> clients = connections.get();

    if (clients.containsKey(clientId)) {
      return clients.get(clientId);
    }

    RteProtocolClient client = protocolFactory.apply(getProtocol());
    SSLData ssldata = new SSLData(DEFAULT_SSLTYPE, null, null);
    client.connect(getServer(), getPort(), ssldata, getTerminalType(), getConnectionTimeout(),
        getStableTimeout()); //TODO: Change hardcoded values from
    // functions in order to take the values from GUI
    clients.put(clientId, client);
    sampleResult.connectEnd();
    return client;
  }

  private String buildConnectionId() {
    return getServer() + ":" + getPort();
  }

  private List<CoordInput> getCoordInputs() {
    List<CoordInput> inputs = new ArrayList<>();
    for (JMeterProperty p : getInputs()) {
      CoordInputRowGUI c = (CoordInputRowGUI) p.getObjectValue();
      inputs.add(c.toCoordInput());
    }
    return inputs;
  }

  public Inputs getInputs() {
    return (Inputs) getProperty(Inputs.INPUTS).getObjectValue();
  }

  private List<WaitCondition> getWaitersList() {
    List<WaitCondition> waiters = new ArrayList<>();
    if (getWaitSync()) {
      waiters.add(new SyncWaitCondition(getWaitSyncTimeoutValue(), getStableTimeout()));
    }
    return waiters;
  }

  private void disconnect(RteProtocolClient client) throws RteIOException {
    client.disconnect();
    connections.get().remove(buildConnectionId());
  }

  private SampleResult errorResult(String message, Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sampleResult.setDataType(SampleResult.TEXT);
    sampleResult.setResponseCode(e.getClass().getName());
    sampleResult.setResponseMessage(e.getMessage());
    sampleResult.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    sampleResult.setSuccessful(false);
    sampleResult.sampleEnd();
    LOG.error(message, e);
    return sampleResult;
  }

  @Override
  public void threadStarted() {
  }

  @Override
  public void threadFinished() {
    closeConnections();
  }

  private void closeConnections() {
    connections.get().values().forEach(c -> {
      try {
        c.disconnect();
      } catch (Exception e) {
        LOG.error("Problem while closing RTE connection", e);
      }
    });
    connections.get().clear();
  }

}
