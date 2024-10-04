package org.home.paper.server.model.projection

interface BaseSeriesProjection {
    fun getId(): Long

    fun getTitle(): String

    fun getMinYear(): Int

    fun getMaxYear(): Int
//    fun title(): String {
//        return if (minYear == maxYear) {
//            "$title (${minYear})"
//        } else {
//            "$title (${minYear} - ${maxYear})"
//        }
//    }
}