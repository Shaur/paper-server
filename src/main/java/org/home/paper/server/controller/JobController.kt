package org.home.paper.server.controller

import org.home.paper.server.dto.JobView
import org.home.paper.server.service.JobService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/jobs")
class JobController(private val service: JobService) {

    @GetMapping("/{id}")
    fun get(@PathVariable("id") jobId: Long): JobView {
        return service.get(jobId)
    }

    @GetMapping()
    fun get(): List<JobView> {
        return service.getAll()
    }

    @PostMapping("/{id}/resolve")
    fun resolve(@PathVariable("id") jobId: Long): JobView {
        return service.resolve(jobId)
    }

}