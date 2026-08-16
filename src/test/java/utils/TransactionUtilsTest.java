package utils;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TransactionUtilsTest {
    @Test
    public void rollbackIsAttempted() {
        List<String> calls = new ArrayList<>();
        TransactionUtils.rollbackQuietly(recordingConnection(calls));
        assertEquals(List.of("rollback"), calls);
    }

    @Test
    public void autoCommitIsRestoredBeforeClose() {
        List<String> calls = new ArrayList<>();
        TransactionUtils.restoreAutoCommitAndClose(recordingConnection(calls));
        assertEquals(List.of("setAutoCommit:true", "close"), calls);
    }

    private Connection recordingConnection(List<String> calls) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
            new Class<?>[] { Connection.class }, (proxy, method, args) -> {
                if (method.getName().equals("rollback")) calls.add("rollback");
                if (method.getName().equals("setAutoCommit")) calls.add("setAutoCommit:" + args[0]);
                if (method.getName().equals("close")) calls.add("close");
                return null;
            });
    }
}
