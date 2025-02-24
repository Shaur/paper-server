package org.home.paper.server.repository

import org.home.paper.server.model.ReadingProgress
import org.home.paper.server.model.ReadingProgressKey
import org.springframework.data.jpa.repository.JpaRepository

interface ReadingProgressRepository : JpaRepository<ReadingProgress, ReadingProgressKey> {
}