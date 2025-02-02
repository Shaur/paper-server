package org.home.paper.server.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass

@Entity(name = "series_subscription")
@IdClass(SeriesSubscription::class)
class SeriesSubscription(
    @Id
    val userId: Long,
    @Id
    val seriesId: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeriesSubscription

        if (userId != other.userId) return false
        if (seriesId != other.seriesId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + seriesId.hashCode()
        return result
    }
}