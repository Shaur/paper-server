package org.home.paper.server.model

import jakarta.persistence.*
import java.util.*

@Entity(name = "issue")
data class Issue(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val number: String,

    val summary: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    val series: Series,

    val pagesCount: Int,

    val publicationDate: Date
)
