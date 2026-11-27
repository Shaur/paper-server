package org.home.paper.server.service

import org.home.paper.server.dto.JobView
import org.home.paper.server.model.Job

fun Job.toView(): JobView {
    return JobView(
        id = this.id!!,
        fileName = this.fileName,
        uploadedBy = this.uploadedBy,
        status = this.status,
        updateTime = this.updateTime,
        errorMessage = this.errorMessage,
        attempts = this.attempts
    )
}