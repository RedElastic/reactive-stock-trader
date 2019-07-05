# Build and Deploy the BFF

If you're running on MacOS we assume that you have Docker Desktop running.

## 1. Log out of Docker Hub

We're going to be publishing to your Minikube Docker Registry, so we need to make sure that you're not also logged into Docker Hub:

`docker logout`

If you skip this step, publishing will most likely fail.

## 2. Select Minikube registry

To ensure you're pushing to Minikube's Docker Registry, run the following command:

```
eval $(minikube docker-env)
```

> Hint: if you're using the Fish terminal (as I am) remove the `$`.

## 3. Build a Docker container for the BFF

Although we are developing RST in a monorepo, it's actually a collection of microservices. We will build and package each as its own Docker container. This is possible as we've configured `sbt native packager`.

```
sbt "bff/docker:publishLocal"
```

## 4. Validate the image

Ensure the image has been built and is available in Minikube's Docker registry:

```
docker images
```

You should see the following:

```
REPOSITORY                                                       TAG                  IMAGE ID            CREATED             SIZE
bff                                                              1.0-SNAPSHOT         50d718525c69        9 hours ago         322MB
```

## 5. Deploy the BFF to Minikube

```
kubectl apply -f bff/deploy/kubernetes/bff-service.yaml
```

## 6. Deploy the ingress manifest

The following will enable access to the BFF from outside of the Kubernetes cluster:

```
kubectl apply -f bff/deploy/kubernetes/reactivestock-ingress.yaml
```

## 7. Apply the new deployment

```
kubectl apply -f bff/deploy/kubernetes/bff-deployment.yaml
```
    
At this point it makes sense to get a list of all the pods:

```
kubectl get pods --all-namespaces
```

Then `kubectl describe pod ...` to investigate the state of the pod.

If you need to troubleshoot, you can access Play logs on the pod, e.g:

```
kubectl logs reactivestock-bff-<pod-hash>
```

Where `pod-hash` will be unique to your pod (assigned randomly during deployments).
    
## 8. Production secrets configuration

Create a folder `bff/deploy/secrets`. 

In this folder, create a file `production.conf`. 

This will be our production configuration with secrets that will be applied at deploy time. You would want to ensure that this file is not included in source control by adding it to your `.gitignore` file.

Copy the following content to `bff/deploy/secrets/production.conf`:

```
include "application"
     
play.http.secret.key = "REPLACEME"

# Other secret configuration data here.  
```
    
Use the following command to generate a [Play Secret](https://www.playframework.com/documentation/latest/ApplicationSecret), use the command:

```
sbt playGenerateSecret
```

Look for the line "Generated new secret: ...". Substitute this value with `REPLACEME` in `frontend/deploy/secrets/production.conf`.

## 9. Add this file as a secret to Kubernetes

```
kubectl create secret generic reactivestock-secrets --from-file=bff/deploy/secrets/production.conf
```
   
## 10. Create the new ConfigMap

```
kubectl apply -f bff/deploy/kubernetes/bff-config.yaml
```

## 11. Check the state of our reactivestock-bff

Did it start successfully? To force the pod to restart, delete it using `kubectl delete pod ...`

At this point, it should start successfully.

## 12. Add reactivestocktrader.com to your hosts file

We have it configured in ALLOWED_HOSTS that `reactivestocktrader.com` is whitelisted. In order to make this happen, you'll need to add it to your hosts file.

Get your IP:

`minikube ip`

Edit `/etc/hosts` and add a line similar to the following:

```
# MINIKUBE
<your ip> reactivestocktrader.com
```

Where `<your ip>` is the IP returned by Minikube above.

## 13. Verify health check

Open up [http://reactivestocktrader.com/healthz](http://reactivestocktrader.com/healthz).

You should see an "OK". Congrats! You deployed the BFF. Next we'll do the same for our bounded contexts (portfolio, broker, and wire transfers).
