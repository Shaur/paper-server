package org.home.paper.server.service

import org.home.paper.server.dto.JobView

interface JobService {

    fun get(jobId: Long): JobView

    fun getAll(): List<JobView>

    fun resolve(jobId: Long): JobView

}
