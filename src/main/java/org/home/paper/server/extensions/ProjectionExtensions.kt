package org.home.paper.server.extensions

import org.home.paper.server.model.projection.BaseSeriesProjection
import org.home.paper.server.model.projection.FilteredSeriesCatalogItemProjection

fun BaseSeriesProjection.title(): String {
    return with(this) {
        if (getMinYear() == null && getMaxYear() == null) {
            getTitle()
        } else if (getMinYear() == getMaxYear()) {
            "${getTitle()} (${getMinYear()})"
        } else {
            "${getTitle()} (${getMinYear()} - ${getMaxYear()})"
        }
    }
}

fun FilteredSeriesCatalogItemProjection.title(): String {
    return with(this) {
        if (minYear == null && maxYear == null) {
            title
        } else if (minYear == maxYear) {
            "$title (${minYear})"
        } else {
            "$title (${minYear} - ${maxYear})"
        }
    }
}