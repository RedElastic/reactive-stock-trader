# Deploying Kafka and Cassandra to Kubernetes

Kafka and Cassandra are required by Reactive Stock Trader for proper operations. You'll need to install both to Minikube before RST will pass health checks and work correctly.

## Deploying Kafka

### 1. Create a separate namespace for the Kafka cluster

```
kubectl create namespace kafka
```

### 2. To install the Kafka operator, we will use Helm

Start by adding the Strimzi Helm repository:

```
helm repo add strimzi http://strimzi.io/charts/
helm repo update
```
  
### 3. Deploy the Kafka Operator

```
helm install --namespace kafka --name reactivestock-kafka strimzi/strimzi-kafka-operator
```
    
Check the state of the operator with:
    
```
kubectl get pods --namespace=kafka
```

> It may take a few minutes for the operator to be ready.
  
### 4. Verify that the Kafka Operator has been started

```
helm ls
```
 
### 5. Install Kafka

We'll use the `kafka-persistent-single.yaml` resource in the `deploy/kubernetes` folder:

```
cd deploy/kubernetes
kubectl apply --namespace=kafka  -f kafka-persistent-single.yaml
```
    
> `kafka-persistent-single.yaml` is a special script for creating a single broker Kafka cluster with persistence provided by a [PersistentVolume](https://kubernetes.io/docs/concepts/storage/persistent-volumes/). In production, you would want to run a minimum of 3 Kafka brokers distributed onto designated nodes.

### 6. Validate that the Kafka and Zookeeper pods have started successfully

```
kubectl get pods --namespace=kafka
```
    
Once `Kafka` is running, should see something similar to:

```
NAME                                                     READY   STATUS    RESTARTS   AGE
reactivestock-strimzi-entity-operator-75497487dd-49rcl   3/3     Running   19         2d
reactivestock-strimzi-kafka-0                            2/2     Running   11         2d
reactivestock-strimzi-zookeeper-0                        2/2     Running   6          2d
strimzi-cluster-operator-5658bb5c6-8mmbc                 1/1     Running   5          2d
```

## Deploying Cassandra

### 1. Add the Helm incubator repo

```
helm repo add incubator https://kubernetes-charts-incubator.storage.googleapis.com/
helm repo update
```

### 2. Run the following to install the Cassandra Helm chart

```
cd deploy/kubernetes
helm install -f ./cassandra-values.yaml --version 0.10.2 --namespace "cassandra" -n "cassandra" incubator/cassandra
```

This will create a single `Cassandra` node in the `Cassandra` namespace. This is only appropriate for deployment to Minikube and should not be used for production (you will need to figure out how to install a production-quality Cassandra cluster for prod).

### 3. Verify deployment

```
kubectl get pods --namespace=cassandra
```

You should see output like the following when the Cassandra cluster is ready:

```
NAME          READY   STATUS    RESTARTS   AGE
cassandra-0   1/1     Running   3          2d
```

### 4. Check that internal Cassandra node is running

```
kubectl exec -it --namespace cassandra $(kubectl get pods --namespace cassandra -l app=cassandra,release=cassandra -o jsonpath='{.items[0].metadata.name}') nodetool status
```

This should return a table similar to the following:

```
Datacenter: datacenter1
=======================
Status=Up/Down
|/ State=Normal/Leaving/Joining/Moving
--  Address     Load       Owns (effective)  Host ID                               Token                                    Rack
UN  172.17.0.8  150.4 KiB  100.0%            5ead381a-ed0e-44e4-af72-2f97b968704e  0                                        rack1
```

`UN` (above) indicates that the node status is **U**p and its state is **N**ormal.
