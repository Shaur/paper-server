package org.home.paper.server.extensions

import org.home.paper.server.model.projection.BaseSeriesProjection

fun BaseSeriesProjection.title(): String {
    return with(this) {
        if (getMinYear() == getMaxYear()) {
            "${getTitle()} (${getMinYear()})"
        } else {
            "${getTitle()} (${getMinYear()} - ${getMaxYear()})"
        }
    }
}