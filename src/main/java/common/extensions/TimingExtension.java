package common.extensions;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
//    private Map<String, Long> startTimes = new ConcurrentHashMap<>();
//
//    @Override
//    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
//        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
//        startTimes.put(testName, System.currentTimeMillis());
//        System.out.println("Thread " + Thread.currentThread().getName() + ": Test started " + testName);
//    }
//
//    @Override
//    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
//        String testName = extensionContext.getRequiredTestClass().getPackageName() + "." + extensionContext.getDisplayName();
//        Long testDuration = System.currentTimeMillis() - startTimes.get(testName);
//        System.out.println("Thread " + Thread.currentThread().getName() + ": Test finished " + testName + ", test duration " + testDuration + " ms");
//    }

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TimingExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        context.getStore(NAMESPACE).put(context.getUniqueId(), System.currentTimeMillis());
        System.out.println("[" + Thread.currentThread().getName() + "] Started: " + getTestName(context));
    }

    @Override
    public void afterTestExecution(ExtensionContext context) throws Exception {
        Long startTime = context.getStore(NAMESPACE).remove(context.getUniqueId(), Long.class);
        String testName = getTestName(context);

        if (startTime == null) {
            System.err.println("[" + Thread.currentThread().getName() + "] No start time for: " + testName);
            return;
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("[" + Thread.currentThread().getName() + "] Finished: " + testName + " (" + duration + "ms)");
    }

    private String getTestName(ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName() + "." +
                context.getDisplayName().replace("()", "");
    }
}