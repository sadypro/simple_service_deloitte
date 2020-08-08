# Simple_service_deloitte

### Assignment :
-	Build an API that will use a string as input and does a find and replace for certain words and outputs the result. For example: replace Google for Google©. 
-	Example input: 	“We really like the new security features of Google Cloud”. 
-	Expected output: 	“We really like the new security features of Google Cloud©”.
-	The words that need to be replaced are provided below the description of this assignment.

-	List of word that need to be replaced:
	•	Oracle -> Oracle©
	•	Google -> Google©
	•	Microsoft -> Microsoft©
	•	Amazon -> Amazon©
	•	Deloitte -> Deloitte©



## Methods 

### 1. Using API GateWay + Lambda function ( Serverless Approach ) - Recommended 
### 2. Deploy using Docker-compose 
### 3. Create Kuberntes Cluster and deploy 


### Method 1 : Using API GateWay + Lambda function ( Serverless Approach )

#### This is the preferred approach to deploy simple API 

- 1. Install the serverless CLI:   npm install -g serverless
- 2. cd lamda_function /
- 3. Run : sls deploy --verbose 


### Method 2 : Deploy using Docker-compose

#### This is the full ops approach to deploy simple api in a docker environemnt using docker-compose
- 1. Install docker and docker-compose if not present 
- 2. Run : docker-compose up -d 

### Method 3 : Create Kuberntes Cluster and deploy 

#### This is the mid ops approach to deploy simple api in a kubernetes cluster
- 1. Create GKE cluster 
		gcloud container clusters create jenkins-cd \
        --num-nodes 2 \
        --machine-type n1-standard-2 \
        --scopes "https://www.googleapis.com/auth/source.read_write,cloud-platform"
		
- 2. Populate Credentials --> gcloud container clusters get-credentials jenkins-cd
- 3. cd k8s/ and run kubectl apply -f *.yml

#### Optional if Jenkins CI/CD  dynamic slaves is require 
- 1. Install Helm 
		wget https://storage.googleapis.com/kubernetes-helm/helm-v2.14.1-linux-amd64.tar.gz
		tar zxfv helm-v2.14.1-linux-amd64.tar.gz
		cp linux-amd64/helm .
		
- 2. Add yourself as a cluster administrator in the cluster's RBAC so that you can give Jenkins permissions in the cluster:
		kubectl create clusterrolebinding cluster-admin-binding --clusterrole=cluster-admin --user=$(gcloud config get-value account)
		
- 3. Grant Tiller, the server side of Helm, the cluster-admin role in your cluster:

		kubectl create serviceaccount tiller --namespace kube-system
		kubectl create clusterrolebinding tiller-admin-binding --clusterrole=cluster-admin --serviceaccount=kube-system:tiller
		
- 4. Use the Helm CLI to deploy the Jenkins

		./helm install -n cd stable/jenkins -f jenkins/values.yaml --version 1.2.2 --wait		
		
- 5. Configure the Jenkins service account to be able to deploy to the cluster and connect to jenkins console

		kubectl create clusterrolebinding jenkins-deploy --clusterrole=cluster-admin --serviceaccount=default:cd-jenkins
- 6. Create a build job and use JenkinsFile supplied to start ci/cd 