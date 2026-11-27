package lab.linux.netfilter.solutions;

import java.util.List;

/**
 * Lời giải tham khảo (LNX-15):
 * Tính danh sách xác suất điều kiện "--probability" mà kube-proxy sinh ra cho N Pod endpoints
 * sao cho xác suất thực tế rơi vào mỗi Pod đều bằng đúng 1/N.
 */
public final class MultiBackendKubeProxySolution {

    public static List<Double> computeIptablesProbabilities(int endpointCount) {
        if (endpointCount <= 0) {
            throw new IllegalArgumentException("endpointCount must be positive");
        }
        java.util.ArrayList<Double> probs = new java.util.ArrayList<>(endpointCount);
        for (int i = 0; i < endpointCount; i++) {
            probs.add(1.0 / (endpointCount - i));
        }
        return probs;
    }

    public static void main(String[] args) {
        System.out.println("N=4 endpoints -> kube-proxy probabilities = " + computeIptablesProbabilities(4));
    }
}
