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
