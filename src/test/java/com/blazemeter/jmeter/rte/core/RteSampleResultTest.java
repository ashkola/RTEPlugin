package com.blazemeter.jmeter.rte.core;

import static org.junit.Assert.assertEquals;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class RteSampleResultTest {

  private static final Position CURSOR_POSITION = new Position(1, 1);
  private static final String EXPECTED_HEADERS_REPONSE = "Input-inhibited: true\n" +
      "Cursor-position: 1,1";
  private static final String EXPECTED_HEADERS = "Server: Test Server\n" +
      "Port: 2123\n" +
      "Protocol: TN5250\n" +
      "Terminal-type: IBM-3179-2: 24x80\n" +
      "Security: NONE\n" +
      "Action: CONNECT\n";

  private static List<Input> CUSTOM_INPUTS = Collections
      .singletonList(new CoordInput(new Position(3, 2), "input"));

  @Test
  public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputInhibitedRequest(true);

    String expectedHeaders = EXPECTED_HEADERS +
        "Input-inhibited: true\n";
    assertEquals(expectedHeaders, rteSampleResult.getRequestHeaders());
  }

  private RteSampleResult buildBasicRTESampleResult() {
    RteSampleResult rteSampleResult = new RteSampleResult();
    rteSampleResult.setAction(Action.CONNECT);
    rteSampleResult.setProtocol(Protocol.TN5250);
    rteSampleResult.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
    rteSampleResult.setServer("Test Server");
    rteSampleResult.setPort(2123);
    rteSampleResult.setSslType(SSLType.NONE);
    rteSampleResult.setSoundedAlarm(true);
    rteSampleResult.setCursorPosition(CURSOR_POSITION);
    return rteSampleResult;
  }

  @Test
  public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setScreen(new Screen(new Dimension(10, 10)));
    assertEquals(EXPECTED_HEADERS, rteSampleResult.getRequestHeaders());
  }

  @Test
  public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    assertEquals("", rteSampleResult.getSamplerData());
  }

  @Test
  public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputs(CUSTOM_INPUTS);
    rteSampleResult.setAttentionKey(AttentionKey.ENTER);

    String expectedSamplerData = "AttentionKey: ENTER\n" +
        "Inputs:\n" +
        "3,2,input\n";
    assertEquals(expectedSamplerData, rteSampleResult.getSamplerData());
  }

  @Test
  public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithNoDisconnectAction() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputInhibitedResponse(true);

    String expectedResponseHeaders = EXPECTED_HEADERS_REPONSE + "\n" +
        "Sound-Alarm: true";
    assertEquals(expectedResponseHeaders, rteSampleResult.getResponseHeaders());
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseHeadersWithDisconnectAction() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setAction(Action.DISCONNECT);
    assertEquals("", rteSampleResult.getResponseHeaders());
  }

  @Test
  public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setSoundedAlarm(false);
    rteSampleResult.setInputInhibitedResponse(true);
    assertEquals(EXPECTED_HEADERS_REPONSE, rteSampleResult.getResponseHeaders());
  }

  @Test
  public void shouldGetScreenTextWhenGetResponseData() {
    Screen screen = new Screen(new Dimension(30, 1));
    String screenText = "Testing screen text";
    screen.addSegment(0, screenText);

    RteSampleResult rteSampleResult = new RteSampleResult();
    rteSampleResult.setScreen(screen);

    assertEquals(screenText, rteSampleResult.getResponseDataAsString());
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseDataWithoutScreen() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    assertEquals("", rteSampleResult.getResponseDataAsString());
  }

}