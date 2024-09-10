package org.home.paper.server.model

import jakarta.persistence.*

@Entity(name = "series")
data class Series(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val title: String,

    @OneToMany(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        mappedBy = "id"
    )
    val issues: List<Issue> = listOf(),

    val publisher: String,

    val isEnded: Boolean = false
)
