def packageChart(String chartDir, String helmRepoUrl) {
    script {
        sh "helm package ${chartDir}"
        withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
            sh "curl -u ${NEXUS_USERNAME}:${NEXUS_PASSWORD} --upload-file ${chartDir}-*.tgz ${helmRepoUrl}"
        }
    }
    
}


