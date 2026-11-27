# Containers & Kubernetes (K8s)

Khu vực thực hành về Containerization và Container Orchestration quy mô lớn.

---

## 🎯 Nội dung trọng tâm

1. **Docker & Container Mechanics**:
   * Bản chất của Container: Linux namespaces, cgroups, UnionFS/Overlay2.
   * Multi-stage Dockerfile optimization (giảm kích thước image, bảo mật non-root user).
   * Docker networking: Bridge, Host, Overlay networks.

2. **Kubernetes Core Concepts**:
   * Master/Control Plane vs Worker Nodes (API Server, etcd, Scheduler, Kubelet).
   * Pod lifecycle, probes (`livenessProbe`, `readinessProbe`, `startupProbe`).
   * Deployments, ReplicaSets, StatefulSets, DaemonSets.
   * Services (ClusterIP, NodePort, LoadBalancer) và Ingress Controllers.
   * ConfigMaps, Secrets, RBAC, ServiceAccounts.

3. **Packaging & GitOps**:
   * Quản lý cấu hình bằng Helm Charts.
   * HPA (Horizontal Pod Autoscaler) dựa trên CPU/Memory và Custom Metrics.

---

<!-- CURRICULUM:START — sinh tự động từ CURRICULUM, cột Status cập nhật tay -->
## 📚 Danh sách bài

Loại: **Bài học** · Template: [lesson-template.md](../../docs/templates/lesson-template.md) · Quy chuẩn: [CONTENT-GUIDE](../../docs/CONTENT-GUIDE.md)

| ID | Bài | Giai đoạn | Status |
| :--- | :--- | :--- | :---: |
| CTR-01 | [Container internals: namespaces & cgroups](01-namespaces-cgroups/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-02 | [Image layers & OverlayFS](02-overlayfs-image-layers/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-03 | [Dockerfile: multi-stage, distroless, jlink, non-root](03-dockerfile-best-practices/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-04 | [JVM trong container](04-jvm-in-containers/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-05 | [Docker networking](05-docker-networking/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-06 | [Docker Compose cho môi trường dev](06-docker-compose-dev/README.md) | Năm 1 · Q3 | 🔲 |
| CTR-07 | [Kiến trúc Kubernetes](07-k8s-architecture/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-08 | [Pod lifecycle & probes](08-pod-lifecycle-probes/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-09 | [Deployment, StatefulSet, DaemonSet, Job](09-workloads/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-10 | [Requests, Limits & QoS](10-requests-limits-qos/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-11 | [Service, Ingress & CNI](11-services-ingress-cni/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-12 | [NetworkPolicy](12-networkpolicy/README.md) | Năm 2 · Q4 | 🔲 |
| CTR-13 | [Storage: PV, PVC, StorageClass](13-storage-pv-pvc/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-14 | [ConfigMap, Secret, RBAC, ServiceAccount](14-config-secrets-rbac/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-15 | [Helm charts](15-helm/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-16 | [Kustomize](16-kustomize/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-17 | [HPA, VPA & Cluster Autoscaler](17-hpa-autoscaling/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-18 | [Debug Kubernetes](18-debugging-k8s/README.md) | Năm 2 · Q3 | 🔲 |
| CTR-19 | [Operators & CRD](19-operators-crd/README.md) | Năm 3 · Q1 | 🔲 |

**Tài liệu gốc của chủ đề:** Docker docs · Kubernetes docs · Kubernetes in Action · Kubernetes the Hard Way · Learnk8s blog

<!-- CURRICULUM:END -->
