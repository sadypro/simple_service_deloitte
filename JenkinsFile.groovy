#!groovy
/* AUTHOR : SAAD UDDIN
 * This is the standard jenkinsfile
 */
import groovy.lang.Binding
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


// pipeline body goes here

pipeline {

//Environment Variable Defnition
environment {
        PROJECT_ID = 'REPLACE_WITH_YOUR_PROJECT_ID'
        APP_NAME = "deloittego"
        CLUSTER_NAME = 'deloitte-dev'
        LOCATION = 'us-east1-d' 
        CREDENTIALS_ID = 'jenkins-gke-deployer'
        CREDS = credentials('saadgen')
        IMAGE_TAG = "saadgen/${APP_NAME}:${env.BRANCH_NAME}.${env.BUILD_NUMBER}"
		}

// Agent defnition  goes here
  
agent {      
    kubernetes {
      label 'jenkins-slave'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
  namespace: jenkins
  labels:
    component: ci-go
spec:
  serviceAccountName: cd-jenkins
  containers:
  - name: golang
    image: golang:1.10
    command:
    - cat
    tty: true
  - name: dind
    image: docker:18.05-dind
    securityContext:
      privileged: true
    resources:
      requests:
        memory: "256Mi"
        cpu: "100m"
      limits:
        memory: "512Mi"
        cpu: "500m"
  - name: kubectl
    image: gcr.io/cloud-builders/kubectl
    resources:
      requests:
        memory: "100Mi"
        cpu: "100m"
      limits:
        memory: "512Mi"
        cpu: "500m"
    command:
    - cat
    tty: true
"""
}
}
  
 
stages {
 
 stage('Test') {
      steps {
        container('golang') {
          sh """
            go test
          """
        }
      }
    }

 stage('Image Build ') 
    { 
    // when { branch 'PLUS' }
     
        steps {
          script {    
        container('dind') {
            def ver = "test-1"
            sh ("docker login -u ${CREDS_USR} -p ${CREDS_PSW} https://hub.docker.com " )
            sh ('echo "Building go API " ')
            sh ('''
            docker build -t ${IMAGE_TAG} .
            docker push ${IMAGE_TAG}
            ''')
            }
       } 
    }
}


stage('Deploy and Setup API ') {
      when { branch 'master' }
      steps {
        container('kubectl') {
        script {
            print "Removing Old API deployment"
            sh("sed -i.bak 's#image_docker#${IMAGE_TAG}#g' .k8s/*.yaml")
            try { 
             sh(' kubectl delete -f /k8s/api.yaml ')
            }
            catch (error)
            {
              println ("Deployment Not Detected ")
            }
            print "Creating new API deployment"
            sh(' kubectl apply -f k8s/api.yaml ; kubectl wait --for=condition=available --timeout=300s  -f k8s/api.yaml ')
            sh(' kubectl apply -f /k8s/hpa.yaml')
                 }
        }
      }
}				

    }
}

