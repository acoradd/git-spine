package fr.accoradd.gitspine.infrastructure.image

import coil3.EventListener
import coil3.decode.DataSource
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult

class LoggingEventListener : EventListener() {

    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
        if (result.dataSource === DataSource.NETWORK) {
            println("GET : ${request.data}")
        }
    }

    override fun onError(request: ImageRequest, result: ErrorResult) {
        println("Erreur GET ${request.data}: ${result.throwable.message}")
    }
}
