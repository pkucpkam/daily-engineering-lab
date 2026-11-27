package lab.ai.playbook;

import java.util.List;

/**
 * Công cụ kiểm chứng API bịa (hallucination) vs lệch phiên bản JDK (version drift) bằng reflection.
 */
public final class ApiVerifier {

    public enum Classification {
        AVAILABLE_SINCE_JAVA_7,
        VERSION_DRIFT_NEWER_JDK,
        HALLUCINATED_NON_EXISTENT
    }

    public record ApiProbe(
            String className,
            String methodName,
            Class<?>[] paramTypes,
            int introducedInJdk,
            String description
    ) {}

    public record VerificationResult(
            ApiProbe probe,
            boolean existsInCurrentJvm,
            Classification classification
    ) {}

    public static List<ApiProbe> defaultProbes() {
        return List.of(
                new ApiProbe("java.lang.String", "trim", new Class<?>[0], 1, "String#trim()"),
                new ApiProbe("java.lang.String", "isBlank", new Class<?>[0], 11, "String#isBlank()"),
                new ApiProbe("java.util.List", "of", new Class<?>[0], 9, "List#of()"),
                new ApiProbe("java.util.Optional", "orElseThrow", new Class<?>[0], 10, "Optional#orElseThrow()"),
                new ApiProbe("java.net.http.HttpClient$Builder", "retryPolicy", new Class<?>[]{Object.class}, -1, "HttpClient.Builder#retryPolicy(...)"),
                new ApiProbe("java.lang.Thread", "ofVirtual", new Class<?>[0], 21, "Thread#ofVirtual()")
        );
    }

    public VerificationResult verify(ApiProbe probe, int targetJdk) {
        boolean exists = methodExists(probe.className(), probe.methodName(), probe.paramTypes());
        if (!exists || probe.introducedInJdk() < 0) {
            return new VerificationResult(probe, exists, Classification.HALLUCINATED_NON_EXISTENT);
        }
        if (probe.introducedInJdk() > targetJdk) {
            return new VerificationResult(probe, exists, Classification.VERSION_DRIFT_NEWER_JDK);
        }
        return new VerificationResult(probe, exists, Classification.AVAILABLE_SINCE_JAVA_7);
    }

    private boolean methodExists(String className, String methodName, Class<?>[] paramTypes) {
        try {
            Class<?> clazz = Class.forName(className);
            clazz.getMethod(methodName, paramTypes);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            return false;
        }
    }
}
