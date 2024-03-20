helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace confluent \
  --set coreDns.enabled=false \
  --set kubeControllerManager.enabled=false \
  --set kubeDns.enabled=false \
  --set kubeEtcd.enabled=false \
  --set kubeProxy.enabled=false \
  --set kubeScheduler.enabled=false \
  --set nodeExporter.enabled=false
