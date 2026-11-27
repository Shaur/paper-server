package org.home.paper.server.service

import org.home.paper.server.dto.JobView
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import java.util.concurrent.CopyOnWriteArrayList

@Component
class JobStreamComponent {

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(): SseEmitter {
        val emitter = SseEmitter(0L)
        emitters += emitter
        emitter.onCompletion { emitters -= emitter }
        emitter.onTimeout { emitters -= emitter }

        return emitter
    }

    fun broadcast(view: List<JobView>) {
        emitters.forEach { em ->
            runCatching {
                val data = event().name("jobs").data(view)
                em.send(data)
            }
                .onFailure { emitters -= em }
        }
    }

    fun ping() {
        emitters.forEach { em ->
            runCatching { em.send(event().comment("ping")) }
                .onFailure { emitters -= em }
        }
    }
}