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

    val seriesId: Long,

    val pagesCount: Int,

    val publicationDate: Date
)
