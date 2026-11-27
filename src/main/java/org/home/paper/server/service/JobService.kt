package org.home.paper.server.service

import org.home.paper.server.dto.JobView
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

interface JobService {

    fun get(jobId: Long): JobView

    fun getAll(): List<JobView>

    fun resolve(jobId: Long): JobView

    fun register(): SseEmitter

}
