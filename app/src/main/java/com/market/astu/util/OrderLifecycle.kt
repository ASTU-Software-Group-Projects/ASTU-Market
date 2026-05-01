package com.market.astu.util

import com.market.astu.data.model.MarketOrder
import com.market.astu.data.model.OrderStatus

enum class OrderActor {
    BUYER,
    SELLER,
    RUNNER,
    OBSERVER
}

fun MarketOrder.actorFor(uid: String?): OrderActor {
    if (uid.isNullOrBlank()) return OrderActor.OBSERVER
    return when (uid) {
        buyerId -> OrderActor.BUYER
        sellerId -> OrderActor.SELLER
        runnerId -> OrderActor.RUNNER
        else -> OrderActor.OBSERVER
    }
}

fun MarketOrder.canBeAcceptedBy(uid: String?): Boolean {
    return !uid.isNullOrBlank() &&
        uid != buyerId &&
        OrderStatus.fromValue(status) == OrderStatus.AWAITING_RUNNER &&
        runnerId.isNullOrBlank()
}

fun MarketOrder.canBeMarkedPickedUpBy(uid: String?): Boolean {
    return uid == runnerId && OrderStatus.fromValue(status) == OrderStatus.RUNNER_ASSIGNED
}

fun MarketOrder.canBeMarkedInTransitBy(uid: String?): Boolean {
    return uid == runnerId && OrderStatus.fromValue(status) == OrderStatus.PICKED_UP
}

fun MarketOrder.canBeDeliveredBy(uid: String?): Boolean {
    return uid == runnerId && OrderStatus.fromValue(status) == OrderStatus.IN_TRANSIT
}

fun MarketOrder.canBeCancelledBy(uid: String?): Boolean {
    val actor = actorFor(uid)
    return actor in setOf(OrderActor.BUYER, OrderActor.SELLER) &&
        OrderStatus.fromValue(status) in setOf(OrderStatus.AWAITING_RUNNER, OrderStatus.RUNNER_ASSIGNED)
}

fun MarketOrder.canBeDisputedBy(uid: String?): Boolean {
    val actor = actorFor(uid)
    return actor != OrderActor.OBSERVER &&
        OrderStatus.fromValue(status) in setOf(
            OrderStatus.RUNNER_ASSIGNED,
            OrderStatus.PICKED_UP,
            OrderStatus.IN_TRANSIT,
            OrderStatus.DELIVERED
        )
}

fun OrderStatus.isLive(): Boolean = this !in setOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED, OrderStatus.DISPUTED)
