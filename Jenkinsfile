pipeline {
    agent {
        kubernetes {
            cloud 'devops-k8s'
            namespace 'devops-jenkins'
            agentInjection false
            yaml """
                apiVersion: v1
                kind: ConfigMap
                metadata:
                  name: jenkins-maven-settings
                  namespace: devops-jenkins
                data:
                  settings.xml: |
                    <?xml version="1.0" encoding="UTF-8"?>
                    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
                      <mirrors>
                        <mirror>
                          <id>central</id>
                          <name>central</name>
                          <mirrorOf>central</mirrorOf>
                          <url>https://repo1.maven.org/maven2/</url>
                        </mirror>
                        <mirror>
                          <id>nexus-aliyun</id>
                          <mirrorOf>central</mirrorOf>
                          <name>Nexus aliyun</name>
                          <url>https://maven.aliyun.com/repository/public</url>
                        </mirror>
                        <mirror>
                          <id>mirror</id>
                          <mirrorOf>!rdc-releases,!rdc-snapshots</mirrorOf>
                          <name>mirror</name>
                          <url>https://maven.aliyun.com/nexus/content/groups/public</url>
                        </mirror>
                      </mirrors>
                    </settings>
                ---
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: jenkins/inbound-agent:3309.v27b_9314fd1a_4-1
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
                  - name: maven
                    image: maven:3.8.4-jdk-8
                    volumeMounts:
                    - name: maven-settings
                      mountPath: /root/maven/conf/
                  volumes:
                  - name: maven-settings
                    configMap:
                      name: jenkins-maven-settings
            """
            retries 1
        }
    }
    stages {
        stage('Run compile') {
            steps {
                container('maven') {
                    sh 'shoami'
                    sh 'ln -s /root/maven/conf/settings.xml /root/.m2/settings.xml'
                    sh 'ls /root/.m2/*'
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