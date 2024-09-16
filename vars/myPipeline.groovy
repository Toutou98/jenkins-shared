def call(Map pipelineParams){
    pipeline {
        agent {
            kubernetes {
                yaml """
                apiVersion: v1
                kind: Pod
                spec:
                containers:
                - name: maven
                  image: maven:3.9.9-ibm-semeru-21-jammy
                  command:
                  - cat
                  tty: true
                - name: docker
                  image: docker:24.0.2-dind
                  command:
                  - dockerd
                  - --host=unix:///var/run/docker.sock
                  - --insecure-registry=nexus-docker.nexus.svc.cluster.local:8083
                  securityContext:
                  privileged: true
                - name: kubehelm
                  image: 10.108.168.228:8083/kubehelm:1.0.0
                  command:
                  - cat
                  tty: true
                """
            }
        }
        stages {
            stage('Build') {
                steps {
                    container('maven') {
                        sh 'mvn package -Dquarkus.package.jar.type=uber-jar'
                    }
                }
            }
            stage('Build Docker Image') {
                steps {
                    container('docker') {
                        script {
                            sh "docker pull nexus-docker.nexus.svc.cluster.local:8083/jdk-base-image:1.0.0"
                            buildDockerImage(pipelineParams.dockerfile, pipelineParams.docker_image, pipelineParams.docker_registry_domain)
                        }
                    }
                }
            }
            stage('Package Helm Chart') {
                steps {
                    container('kubehelm') {
                        script {
                            packageChart(pipelineParams.app_name, pipelineParams.helm_url)
                        }
                    }
                }
            }
            stage('Deploy Helm Chart') {
                steps {
                    container('kubehelm') {
                        script {
                            deployChart(pipelineParams.app_name, pipelineParams.helm_url)
                            sh "kubectl --help"
                            sh "kubectl get pods"
                        }
                    }
                }
            }
        }
        post {
            always {
                cleanWs()
            }
        }
    }
}