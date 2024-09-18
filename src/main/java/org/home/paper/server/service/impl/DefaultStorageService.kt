package org.home.paper.server.service.impl

import org.home.paper.server.configuration.properties.StorageProperties
import org.home.paper.server.service.StorageService
import org.springframework.stereotype.Service
import java.io.File

@Service
class DefaultStorageService(properties: StorageProperties) : StorageService {

    private val purgatoryDir = File(properties.purgatoryPath)
    private val issuesDir = File(properties.issuesPath)

    init {
        if (!issuesDir.exists()) issuesDir.mkdirs()
        if (!purgatoryDir.exists()) purgatoryDir.mkdirs()
    }

    override fun deletePurgatoryDir(id: Long) {
        purgatoryDir.resolve(id.toString()).deleteRecursively()
    }

    override fun transfer(purgatoryId: Long, issueId: Long) {
        purgatoryDir.resolve(purgatoryId.toString())
            .copyRecursively(issuesDir.resolve(issueId.toString()), true)
    }

}