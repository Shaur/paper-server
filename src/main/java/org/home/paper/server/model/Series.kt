package org.home.paper.server.model

import jakarta.persistence.*

@Entity(name = "series")
data class Series(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val title: String,

    val publisher: String,

    val isEnded: Boolean = false
)
