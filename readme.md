# Containerised Spring Boot microservice 101

This repo demonstrates how to write a simple microservice using Spring Boot and then run it in a containerised environment.
The microservice is a basic REST API for a shopping cart item.
It supports CRUD operations and persists data to MongoDB.
The demo uses Spring Data to simplify the persistence logic.

## Prerequisites
To build and run the demo you will need certain tooling available.

- Git
- A recent JDK
- Maven
- Docker
- A Kubernetes cluster
- Access to a Mongo database

These don't all need to be on your local machine, and if you have access to these as cloud services that may simplify things.
However, for new learners these things may not be readily available, or may come at a cost.

## Understanding the demo
The demo is made of several components.
#### *Spring Boot Application*
This code that provides the REST API is a very simple Spring Boot application.
Take a look at the Maven POM to see how it inherits from `spring-boot-starter-parent`, and also includes dependencies on `spring-boot-starter-web`, `spring-data-mongodb` and `mongodb-driver-sync`.

There is a `MongoConfig` bean that creates a `MongoClient` from the given config (more on this later).
The API component is provided by the `CartItemController` class, the model by `CartItem` and there's a `CartItemRepository` interface to provide the API with a repository to the database.
For this simple demonstration Spring Data provides an adequate default implementation, so no repository implementation is included in our example code. 

#### *Docker*
A simple `Dockerfile` is included that specifies the image build we want to create.
It is based on the Azul Zulu JDK.
The build adds our Spring Boot application jar file and specifies the command to run the application.

#### *Kubernetes*
There are three Kubernetes manifest files.
One of each for the deployment, service and secrets (which is where we put our Mongo config).

## Running the demo
### Step 1 - Build the application
Clone this repository, then build the maven module with;

`$ mvn package`

Don't worry if this build give you an error about not being able to find the MongoDB host.
The Spring application uses parameterised values for the connection to avoid putting usernames and passwords into source control or a docker image repository.
We will handle this later.

So long as your build creates the `microservice-example-0.0.1-SNAPSHOT.jar` file, all is good.

You can test the application running locally with the command

`$ mvn spring-boot:run`

...but it probably won't run unless you first substitute the default values in `application.properties` with your MongoDB config.

### Step 2 - Build the docker image
Next, in order to run our microservice in a container, we need to build a container image.
We're using Docker, which is a good choice for most circumstances.
If you don't have it, you will need to install the Docker runtime first; for my local machine I use [Docker Desktop](https://www.docker.com/products/docker-desktop/).

The Dockerfile is very simple, it only contains three directives.
```Dockerfile
FROM azul/zulu-openjdk:17

COPY target/microservice-example-0.0.1-SNAPSHOT.jar microservice-example.jar

CMD ["java", "-jar", "microservice-example.jar"]
```

The `FROM` directive gives us our base image, which is Ubuntu with the Azul Zulu JDK included on it.
The `COPY` directive puts the Spring Boot jar file in the image and also renames it.
The `CMD` directive tells the container how to run our application, in this case by using the `java` command

Make sure your Docker Desktop is running then go ahead and build the docker image with

`$ docker build --tag=springbootms:latest .`

Once this is complete, you should be able to see the `springbootms` image in your Docker Desktop or by running;

`$ docker images`

![Docker Desktop](/doc/docker_desktop.png)

It can be useful to tag your images to help organise and identify them.
You'll now need to push this to an image repository called a *container registry* so it can be used later by Kubernetes.
Before we push it, we'll give it a unique tag in the form `username/imagename:version`.

`$ docker tag springbootms:latest ntay/springbootms`

..where `ntay` is my hub username, but you should substitute yours.

We'll use Docker Hub because it's easy, but others are available such as AWS ECR.
Within Docker Desktop make sure you sign in, then push to Docker Hub. 

### Step 3 - Create a local Kubernetes cluster

If you don't have access to a Kubernetes cluster, you can install a local dev cluster using Minikube.
If you already have access to a cluster and want to use it, then you can skip this whole step.

First [install kubectl](https://kubernetes.io/docs/tasks/tools/), which is the Kubernetes administration tool.

Next [install minikube](https://minikube.sigs.k8s.io/docs/start/), which is a container based Kubernetes cluster.
That's right, we're running our containers in a container. Once you have installed, start it up with

`$ minikube start`

..the first time you fire it up is slow, maybe go make a cup of tea. Once it's done you can check the status with

`$ kubectl cluster-info`

### Step 4 - Apply the Kubernetes manifests
As mentioned earlier, there are three manifest files representing three parts to our microservice.

The first part is the [Secrets](https://kubernetes.io/docs/concepts/configuration/secret/). Our Spring Boot application contains parameters for the MongoDB config.
We don't want to put these (especially the username and password) into the `application.properties` file and check into source control.
We also want to avoid building them into the docker image, as we made it publically available when we pushed to the container repository.

Create a manifest file called `cartms-mongo-secret.yaml` with the config values base64 encoded as follows;
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: mongo-conf
type: Opaque
data:
  MONGO_CONNECTION: <base64 encoded connection string>
  MONGO_DB: <base64 encoded database name>
```

..and then apply it using;

`$ kubectl apply -f kube/cartms-mongo-secret.yaml`

Using secrets like this allows us to provide the username and password to the container at runtime. The secrets manifest
should be managed in a secure way so as not to leak sensitive information. There are other best practices around data security
that you should learn when using Kubernetes for real - see the linked docs.

Next, look at the [deployment](https://kubernetes.io/docs/concepts/workloads/controllers/deployment/) manifest `cartms-deployment-local.yaml`.
Our deployment defines a single pod running the docker container we pushed earlier `ntay/springbootms:latest`.
**Make sure you change the image name to match the one you pushed earlier**.
Once you've got the correct image name, apply the deployment and service manifests;

```
$ kubectl apply -f kube/cartms-deployment-local.yaml
$ kubectl apply -f kube/cartms-service.yaml
```

The [service](https://kubernetes.io/docs/concepts/services-networking/service/) manifest defines a logical service, exposing the http endpoint running on port 8080 on our container,
and mapping it to port 8081 on the kubernetes cluster.

Wait a few seconds for the cluster to instantiate the objects we have requested. It shouldn't take long and
you can check the status with;

`$ kubectl get pods`

### Step 5 - Port mapping
If you're running a local Kubernetes cluster using minikube, there is one final step. Our service is running in the
cluster listening to port 8081, which we defined in the service, but to access it we need to [forward a port](https://kubernetes.io/docs/tasks/access-application-cluster/port-forward-access-application-cluster/) on our local
computer to Kubernetes. Again, this can be done with kubectl;

`$ kubectl port-forward srv/cartms 8081:8081`

### Step 6 - Testing the microservice
Now the pod has started, and we have the network sorted out, you can access the microservice REST API for testing.

Try posting a new cart item with;

```
$ curl --data '{"name":"Biscuits","qty":5,"unit_cost": 129}' http://localhost:8081/cart/item --header "Content-Type:application/json"
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100   136    0    92  100    44   1957    936 --:--:-- --:--:-- --:--:--  2956{"id":"65ce92b56d240e2c1ae102cf","name":"Biscuits","qty":5,"unit_cost":129,"total_cost":645}
```

Then you can try reading the item back;
```
$ curl http://localhost:8081/cart/item/65ce92b56d240e2c1ae102cf
  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                 Dload  Upload   Total   Spent    Left  Speed
100    92    0    92    0     0   2044      0 --:--:-- --:--:-- --:--:--  2044{"id":"65ce92b56d240e2c1ae102cf","name":"Biscuits","qty":5,"unit_cost":129,"total_cost":645}
```

# Conclusion
We've been able to get a Spring Boot microservice running on Kubernetes locally, and test it out.
There's more to explore in the next stage, which involves [creating a Kubernetes cluster on AWS using Elastic Kubernetes Service (EKS),
and using Terraform to define our deployment infrastructure as code](infrastructure-as-code.md).