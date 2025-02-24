package org.home.paper.server.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass

@Entity(name = "reading_progress")
@IdClass(ReadingProgressKey::class)
class ReadingProgress(
    @Id
    val userId: Long,

    @Id
    val issueId: Long,

    val currentPage: Int,

    val updateTime: Long
) {

    fun update(currentPage: Int, updateTime: Long): ReadingProgress {
        return ReadingProgress(
            userId = this.userId,
            issueId = this.issueId,
            currentPage = currentPage,
            updateTime = updateTime
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReadingProgress

        if (userId != other.userId) return false
        if (issueId != other.issueId) return false
        if (currentPage != other.currentPage) return false
        if (updateTime != other.updateTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + issueId.hashCode()
        result = 31 * result + currentPage
        result = 31 * result + updateTime.hashCode()
        return result
    }
}

class ReadingProgressKey(val userId: Long = 0, val issueId: Long = 0)