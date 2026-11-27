package org.home.paper.server.service.impl

import org.home.paper.server.dto.JobView
import org.home.paper.server.exceptions.IllegalStateException
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.model.Job
import org.home.paper.server.model.JobStatus
import org.home.paper.server.repository.JobRepository
import org.home.paper.server.service.JobService
import org.home.paper.server.service.JobStreamComponent
import org.home.paper.server.service.toView
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Service

class DefaultJobService(
    private val repository: JobRepository,
    private val streamer: JobStreamComponent
) : JobService {

    override fun get(jobId: Long): JobView {
        val job = repository.findByIdOrNull(jobId) ?: throw ObjectNotFoundException("Job", jobId)

        return job.toView()
    }

    override fun getAll(): List<JobView> {
        return repository.findAllByOrderByIdDesc().map { it.toView() }
    }

    override fun resolve(jobId: Long): JobView {
        val job = repository.findByIdOrNull(jobId) ?: throw ObjectNotFoundException("Job", jobId)
        if (job.status != JobStatus.FAILED) {
            throw IllegalStateException("Wrong job status")
        }

        val savedJob = repository.save(job.copy(status = JobStatus.DONE))
        return savedJob.toView()
    }

    override fun register(): SseEmitter {
        return streamer.register()
    }

    private var state = EventStreamState()

    @Scheduled(fixedRate = 2000)
    fun broadcastEvents() {
        val views = repository.findAllByOrderByIdDesc().map(Job::toView)
        if (views == state.lastSent) {
            if (state.incrementAndCheck()) {
                streamer.ping()
            }
            return
        }

        state.updateLast(views)
        streamer.broadcast(views)
    }

    private data class EventStreamState(
        var lastSent: List<JobView> = listOf(),
        var quietTicks: Int = 0
    ) {
        fun updateLast(views: List<JobView>) {
            this.lastSent = views
            this.quietTicks = 0
        }

        fun incrementAndCheck(): Boolean {
            if (++quietTicks >= 15) {
                quietTicks = 0
                return true
            }

            return false
        }
    }
}