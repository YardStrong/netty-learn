pipeline {
    agent {
        kubernetes {
            cloud 'kubernetes'
            namespace 'jenkins'
            agentInjection false
            yaml """
                kind: Pod
                spec:
                  containers:
                  - name: jnlp
                    image: jenkins/inbound-agent:latest
                    args: ['\$(JENKINS_SECRET)', '\$(JENKINS_NAME)']
            """
        }
    }
    stages {
        stage('print') {
            steps {
                echo 'hello'
            }
        }

    }
}