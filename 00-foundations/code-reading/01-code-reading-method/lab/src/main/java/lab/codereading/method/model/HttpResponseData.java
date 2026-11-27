package lab.codereading.method.model;

import java.math.BigDecimal;

public record HttpResponseData(int status, String traceId, String body, BigDecimal finalAmount) {
}
