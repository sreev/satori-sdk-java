package examples;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.satori.rtm.Ack;
import com.satori.rtm.RtmClient;
import com.satori.rtm.RtmClientBuilder;
import com.satori.rtm.SubscriptionAdapter;
import com.satori.rtm.SubscriptionMode;
import com.satori.rtm.model.SubscribeReply;
import com.satori.rtm.model.SubscribeRequest;
import com.satori.rtm.model.SubscriptionData;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ComplexPublishSubscribe {
  public static void main(String[] args) throws InterruptedException {
    String endpoint = (0 < args.length) ? args[0] : null;
    String appKey = (1 < args.length) ? args[1] : null;

    if (Strings.isNullOrEmpty(endpoint) || Strings.isNullOrEmpty(appKey)) {
      System.out.println("Usage: ./program <endpoint> <appkey>");
      System.exit(1);
    }

    final CountDownLatch signal = new CountDownLatch(1);
    final RtmClient client = new RtmClientBuilder(endpoint, appKey)
        .build();

    client.start();
    final String channel = "channel";
    client.createSubscription(channel, SubscriptionMode.SIMPLE,
        new SubscriptionAdapter() {
          @Override
          public void onEnterSubscribed(SubscribeRequest request, SubscribeReply reply) {
            Person mike = new Person("Mike", 8, null);
            Person alice = new Person("Alice", 3, null);
            Person john = new Person("John", 32, Lists.newArrayList(mike, alice));
            client.publish(channel, john, Ack.NO);
          }

          @Override
          public void onSubscriptionData(SubscriptionData data) {
            for (Person person : data.getMessagesAsType(Person.class)) {
              System.out.println("received: " + person);
            }
            signal.countDown();
          }
        });

    signal.await(15, TimeUnit.SECONDS);
    client.shutdown();
  }

  static class Person {
    String name;
    Integer age;
    List<Person> children;

    public Person(String name, Integer age, List<Person> children) {
      this.name = name;
      this.age = age;
      this.children = children;
    }

    @Override
    public String toString() {
      return String.format("(name: %s, age: %s, children: %s)", name, age, children);
    }
  }
}
