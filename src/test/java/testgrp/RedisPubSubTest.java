package testgrp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.FiberUtil;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.redis.Jedis;
import co.paralleluniverse.fibers.redis.JedisPubSub;
import co.paralleluniverse.strands.Strand;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class RedisPubSubTest {
    @Test
    public void testSubscribeFiber() throws InterruptedException, ExecutionException, SuspendExecution {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.auth("foobared");

        final Channel<Object> resCh = Channels.newChannel(1);

        FiberUtil.runInFiber(() -> jedis.subscribe(new JedisPubSub() {
            @Suspendable
            public void onMessage(String channel, String message) {
                assertEquals("foo", channel);
                assertEquals("exit", message);
                unsubscribe();
            }

            @Suspendable
            public void onSubscribe(String channel, int subscribedChannels) {
                assertEquals("foo", channel);
                assertEquals(1, subscribedChannels);

                // now that I'm subscribed... publish
                jedis.publish("foo", "exit");
            }

            @Suspendable
            public void onUnsubscribe(String channel, int subscribedChannels) {
                assertEquals("foo", channel);
                assertEquals(0, subscribedChannels);
                ping(resCh);
            }
        }, "foo"));

        resCh.receive(); // Wait for asserts
    }

    @Test
    public void testSubscribeThread() throws InterruptedException, ExecutionException, SuspendExecution {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.auth("foobared");

        final Channel<Object> resCh = Channels.newChannel(1);

        jedis.subscribe(new JedisPubSub() {
            @Suspendable
            public void onMessage(String channel, String message) {
                assertEquals("foo", channel);
                assertEquals("exit", message);
                unsubscribe();
            }

            @Suspendable
            public void onSubscribe(String channel, int subscribedChannels) {
                assertEquals("foo", channel);
                assertEquals(1, subscribedChannels);

                // now that I'm subscribed... publish
                jedis.publish("foo", "exit");
            }

            @Suspendable
            public void onUnsubscribe(String channel, int subscribedChannels) {
                assertEquals("foo", channel);
                assertEquals(0, subscribedChannels);
                ping(resCh);
            }
        }, "foo");

        resCh.receive(); // Wait for asserts
    }

    @Suspendable
    private static void ping(Channel<Object> resCh) {
        try {
            resCh.send(new Object());
        } catch (final SuspendExecution e) {
            throw new AssertionError(e);
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
