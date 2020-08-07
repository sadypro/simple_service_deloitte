#!groovy
/* AUTHOR : SAAD UDDIN(saad.uddin@soprateria.com) SOPRA STERIA CLOUD COE 
 * This is the standard jenkinsfile to build and test monolith application 
 */
import groovy.lang.Binding
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



//Function to collect change logs and send to Jira

@NonCPS
def sendChangeLogs(buildstat) {
   def commitMessages = ""
   def formatter = new SimpleDateFormat('yyyy-MM-dd HH:mm')
   def changeLogSets = currentBuild.changeSets
   for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            commitMessages = commitMessages + "* Last Commited by ${entry.author}  \n \n * Commit Message ${formatter.format(new Date(entry.timestamp))}: *${entry.msg}* \n * Commit id : [${entry.commitId}|${env.commit_id_url}] " 
        }
    }
  jiraAddComment comment: "BUILD is ${buildstat} for  [${env.JOB_BASE_NAME}${env.BUILD_DISPLAY_NAME}|${BUILD_URL}] : \n ${commitMessages}", idOrKey: "${env.BR}", site: 'jira'
    
}
//Function to send notifcation to teams

def notifyteams(status,colour) {
office365ConnectorSend color: "$colour", message: """

<h2 style="text-align: left;"><strong>JIRA ISSUE</strong> : <span style="color: #000000; background-color: #ffffff;">${env.BR}</span></h2>
<h2 style="text-align: left;"><strong> Build Number</strong> : <span style="color: #000000; background-color: #ffffff;">${env.BUILD_NUMBER}</span></h2>
<h2 style="text-align: left;"><strong>Status</strong> : <span style="background-color: #ffffff; color: #$colour;"><strong>$status </strong></span></h2>
<h2 style="text-align: left;"><span style="text-decoration: underline;"><strong>Reports</strong></span> :&nbsp;</h2>
<ul>
<li><a title="Sonar Scan" href="https://www.sterialiquiditysuite.ml/sonarqube/dashboard?id=${env.BR}" target="_blank" rel="noopener noreferrer" data-ignore-semantic-link="">Sonar Scan</a></li>
<li><a title="Junit Test" href="${env.JOB_URL}Junit_20Report" target="_blank" rel="noopener">Junit Test</a></li>
<li><a title="Integration Test" href="${env.JOB_URL}Integration_20Test_20Report">Integration Test</a></li>
</ul> 
""", status: "$status", webhookUrl: 'https://outlook.office.com/webhook/765b63a8-d4e7-4e92-8602-b9af283b76a8@8b87af7d-8647-4dc7-8df4-5f69a2011bb5/JenkinsCI/1c4d77f1bb3d425c876b2ed45474a895/43953621-0da6-4ac5-9ed8-d7ac165440ca'
           }

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
      label 'jenkins-slave-dzb'
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
            ln -s `pwd` /go/src/sample-app
            cd /go/src/sample-app
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

