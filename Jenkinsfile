pipeline {
    agent {
        kubernetes {
            cloud 'devops-k8s'
            namespace 'devops-jenkins'
            agentInjection false
            yaml """
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
                  - name: maven
                    image: maven:3.8.4-jdk-8
                    command:
                      - cat
                    volumeMounts:
                    - name: maven-settings
                      mountPath: /home/jenkins/agent/maven/conf/
                  volumes:
                  - name: maven-settings
                    configMap:
                      name: jenkins-maven-settings
            """
            retries 1
        }
    }
    stages {
        stage('Add mirror') {
            steps {
                sh 'shoami'
                sh 'ln -s /home/jenkins/agent/maven/conf/maven/conf/settings.xml /root/.m2/settings.xml'
                sh 'ls /root/.m2/*'
            }
        }
        stage('Run compile') {
            steps {
                container('maven') {
                    sh 'mvn clean compile'
                }
            }
        }

        stage('Run build') {
            steps {
                container('maven') {
                    sh 'mvn package -Dmaven.test.skip=true'
                }
            }
        }

        stage('Archive artifacts') {
            steps {
                archiveArtifacts(artifacts: '*/target/*.jar', followSymlinks: false)
            }
        }

    }
}