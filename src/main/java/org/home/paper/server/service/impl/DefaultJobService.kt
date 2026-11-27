package org.home.paper.server.service.impl

import org.home.paper.server.dto.JobView
import org.home.paper.server.exceptions.IllegalStateException
import org.home.paper.server.exceptions.ObjectNotFoundException
import org.home.paper.server.model.JobStatus
import org.home.paper.server.repository.JobRepository
import org.home.paper.server.service.JobService
import org.home.paper.server.service.toView
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class DefaultJobService(private val repository: JobRepository) : JobService {

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
}