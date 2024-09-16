def call(){
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
        environment {
            HELM_URL = "http://my-nexus-nexus-repository-manager.nexus:8081/repository/helm-local-repo/"
            DOCKER_REGISTRY = "http://nexus-docker.nexus.svc.cluster.local:8083"
            DOCKER_REGISTRY_DOMAIN = "nexus-docker.nexus.svc.cluster.local:8083"
            DOCKER_IMAGE = "getting-started:1.0.0"
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
                            buildDockerImage('src/main/docker/Dockerfile.toutou', env.DOCKER_IMAGE, env.DOCKER_REGISTRY_DOMAIN)
                        }
                    }
                }
            }
            stage('Package Helm Chart') {
                steps {
                    container('kubehelm') {
                        script {
                            packageChart('quarkus-app', env.HELM_URL)
                        }
                    }
                }
            }
            stage('Deploy Helm Chart') {
                steps {
                    container('kubehelm') {
                        script {
                            deployChart('quarkus-app', env.HELM_URL)
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