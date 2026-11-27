package org.home.paper.server.dto

import org.home.paper.server.model.JobStatus
import java.time.LocalDateTime

data class JobView(
    val id: Long,

    val fileName: String,

    val uploadedBy: String,

    val status: JobStatus = JobStatus.PENDING,

    val updateTime: LocalDateTime = LocalDateTime.now(),

    val errorMessage: String? = null,

    val attempts: Int = 0
)
