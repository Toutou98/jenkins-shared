def packageChart(String chartDir, String helmRepoUrl) {
    container('helm') {
        script {
            sh "helm package ${chartDir}"
            withCredentials([usernamePassword(credentialsId: 'nexus', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "curl -u ${NEXUS_USERNAME}:${NEXUS_PASSWORD} --upload-file ${chartDir}-*.tgz ${helmRepoUrl}"
            }
        }
    }
}

def deployChart(String chartName, String helmRepoUrl) {
    container('helm') {
        script {
            sh "helm repo add helm-local ${helmRepoUrl}"
            sh 'helm repo update'
            def releaseExists = sh(script: "helm list -q | grep -w '${chartName}'", returnStatus: true) == 0
            if (releaseExists) {
                echo 'Release already exists. Upgrading...'
                sh "helm upgrade ${chartName} helm-local/${chartName}"
            } else {
                echo 'Release does not exist. Installing...'
                sh "helm install ${chartName} helm-local/${chartName}"
            }
        }
    }
}
