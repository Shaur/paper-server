package org.home.paper.server.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity(name = "job")
data class Job(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val fileName: String,

    val filePath: String,

    val uploadedBy: String,

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    val status: JobStatus = JobStatus.PENDING,

    val updateTime: LocalDateTime = LocalDateTime.now(),

    val errorMessage: String? = null,

    val attempts: Int = 0
)
