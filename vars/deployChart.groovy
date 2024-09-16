def call(String chartName, String helmRepoUrl) {
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