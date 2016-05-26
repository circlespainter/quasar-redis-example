package testgrp;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.redis.Jedis;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RedisCommandsTest {
    private final byte[] bfoo = {0x01, 0x02, 0x03, 0x04};
    private final byte[] bbar = {0x05, 0x06, 0x07, 0x08};

    @Test
    public void testGetSetThread() throws ExecutionException, InterruptedException {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.auth("foobared");
        jedis.set("foo", "bar");
        String status = jedis.rename("foo", "bar");
        assertEquals("OK", status);

        String value = jedis.get("foo");
        assertEquals(null, value);

        value = jedis.get("bar");
        assertEquals("bar", value);

        // Binary
        jedis.set(bfoo, bbar);
        String bstatus = jedis.rename(bfoo, bbar);
        assertEquals("OK", bstatus);

        byte[] bvalue = jedis.get(bfoo);
        assertEquals(null, bvalue);

        bvalue = jedis.get(bbar);
        assertArrayEquals(bbar, bvalue);
    }

    @Test
    public void testGetSetFiber() throws ExecutionException, InterruptedException {
        Jedis jedis = new Jedis("localhost", 6379);

        new Fiber(() -> {
            jedis.auth("foobared");
            jedis.set("foo", "bar");
            String status = jedis.rename("foo", "bar");
            assertEquals("OK", status);

            String value = jedis.get("foo");
            assertEquals(null, value);

            value = jedis.get("bar");
            assertEquals("bar", value);

            // Binary
            jedis.set(bfoo, bbar);
            String bstatus = jedis.rename(bfoo, bbar);
            assertEquals("OK", bstatus);

            byte[] bvalue = jedis.get(bfoo);
            assertEquals(null, bvalue);

            bvalue = jedis.get(bbar);
            assertArrayEquals(bbar, bvalue);
        }).start().join();
    }
}
