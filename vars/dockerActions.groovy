def buildDockerImage(String imageName, String registryDomain) {
    container('docker') {
        script {
            withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "echo $NEXUS_PASSWORD | docker login ${registryDomain} -u ${NEXUS_USERNAME} --password-stdin"
                sh "docker tag ${imageName} ${registryDomain}/${imageName}"
                sh "docker push ${registryDomain}/${imageName}"
            }
        }
    }
}
