# Installing Minikube on MacOS

We assume that you have Homebrew installed before continuing.

## 1. Install Virtualbox and Minikube:

Minikube requires virtualization, which we will use Virtualbox for. 

`brew cask install virtualbox minikube` 

## 2. Verify installation:

To test out the installation, let's start up Minikube and assign 8 GiB of RAM. Unfortunately, anything less than this will prevent all of the microservices, along Kafka and Cassandra, from launching properly.

Note: allocating specific memory only works the first time you launch Minikube. If you forget to specify extra memory, you will need to delete the Minicube cluster and start again. Without extra memory Kafka and Cassandra will have a hard time starting correctly.

`minikube start --memory 8192` 

This will handle downloading the Minikube ISO and getting everything up and running with Virtualbox.

## 3. Once you see that Minikube is up and running, test out the installation and bootup by launching the dashboard:

`minikube dashboard`

> If you’d like to pause testing at any point, make sure to stop Minikube as it's quite resource intensive: `minikube stop`

For now though, we'll keep Minikube running so we can complete our configuration. 

## 4. Enable ingress addon:

In order to expose ports to the outside world we need the _ingress addon_ for Minikube.

`minikube addons enable ingress`

## 5. Install kubectl:

`brew install kubernetes-cli`

After installing kubectl with brew you should run:

`rm /usr/local/bin/kubectl`

`brew link --overwrite kubernetes-cli`

And then you can verify installation with:

`kubectl config get-contexts`

``` 
CURRENT NAME CLUSTER AUTHINFO NAMESPACE 
* minikube minikube minikube 
```

## 6. Install Helm:

`brew install kubernetes-helm`

And then deploy Helm to your Kubernetes cluster:

`helm init`

and you should see a large amount of logging, including something like this:

```
Tiller (the Helm server-side component) has been installed into your Kubernetes Cluster.
```

To verify that Helm has installed properly:

`helm version`

``` 
Client: &version.Version{SemVer:"v2.14.1", GitCommit:"5270352a09c7e8b6e8c9593002a73535276507c0", GitTreeState:"clean"} 
Server: &version.Version{SemVer:"v2.14.1", GitCommit:"5270352a09c7e8b6e8c9593002a73535276507c0", GitTreeState:"clean"} 
```

Congrats! Minikube should now be up and running properly.

> It can take a minute or two for `Server` to be ready and return a proper value.
