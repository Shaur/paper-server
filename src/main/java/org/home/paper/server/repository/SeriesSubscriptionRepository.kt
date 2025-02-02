package org.home.paper.server.repository

import org.home.paper.server.model.SeriesSubscription
import org.springframework.data.jpa.repository.JpaRepository

interface SeriesSubscriptionRepository : JpaRepository<SeriesSubscription, SeriesSubscription> {
    fun getByUserId(userId: Long): List<SeriesSubscription>
}