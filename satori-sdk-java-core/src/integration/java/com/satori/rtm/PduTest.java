package com.satori.rtm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.satori.rtm.connection.Serializer;
import com.satori.rtm.model.InvalidJsonException;
import com.satori.rtm.model.Pdu;
import com.satori.rtm.model.PduRaw;
import com.satori.rtm.model.SubscribeRequest;
import com.satori.rtm.model.SubscriptionError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PduTest {
  @Test
  public void deserializePduIntoDifferentClasses() throws InvalidJsonException {
    Serializer serializer = RtmClientBuilder.createSerializer();
    PduRaw raw = serializer.parsePdu("{\"action\":\"rtm/subscription/error\"," +
        "\"body\":{\"subscription_id\":\"channel\",\"error\":\"error\",\"field\": 10}}");
    Pdu<SubscriptionError> pdu = raw.convertBodyTo(SubscriptionError.class);
    assertThat(pdu.getBody().getSubscriptionId(), equalTo("channel"));
    assertThat(pdu.getBody().getError(), equalTo("error"));
  }

  @Test
  public void subscribeRequestSerialization() throws InvalidJsonException {
    SubscribeRequest payload = new SubscribeRequest("channel", "some_next");

    Pdu<SubscribeRequest> request =
        new Pdu<SubscribeRequest>("action/subscribe", payload, "someId");

    Serializer jsonSerializer = RtmClientBuilder.createSerializer();
    PduRaw pduRaw = jsonSerializer.parsePdu(jsonSerializer.toJson(request));
    Pdu<SubscribeRequest> decoded = pduRaw.convertBodyTo(SubscribeRequest.class);

    assertThat(decoded.getId(), equalTo("someId"));
    assertThat(decoded.getAction(), equalTo("action/subscribe"));
    assertThat(decoded.getBody().getChannel(), equalTo("channel"));
    assertThat(decoded.getBody().getPosition(), equalTo("some_next"));
    assertThat(decoded.getBody().getHistory(), is(nullValue()));
  }

  @Test
  public void subscribeRequestSerializationWithoutId() throws InvalidJsonException {
    SubscribeRequest payload = new SubscribeRequest("channel", "some_next");

    Pdu<SubscribeRequest> request = new Pdu<SubscribeRequest>("action/subscribe", payload);

    Serializer jsonSerializer = RtmClientBuilder.createSerializer();
    PduRaw pduRaw = jsonSerializer.parsePdu(jsonSerializer.toJson(request));
    Pdu<SubscribeRequest> decoded = pduRaw.convertBodyTo(SubscribeRequest.class);
    assertThat(decoded.getId(), is(nullValue()));
    assertThat(decoded.getAction(), equalTo("action/subscribe"));
    assertThat(decoded.getBody().getChannel(), equalTo("channel"));
    assertThat(decoded.getBody().getPosition(), equalTo("some_next"));
    assertThat(decoded.getBody().getHistory(), is(nullValue()));
  }
}
