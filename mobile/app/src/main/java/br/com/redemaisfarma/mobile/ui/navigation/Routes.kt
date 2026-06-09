package br.com.redemaisfarma.mobile.ui.navigation

object Routes {
    const val HOME = "home"
    const val CART = "cart"
    const val ORDERS = "orders"
    const val ORDER_DETAIL = "orders/{id}"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val PRODUCT_DETAIL = "product/{id}"

    fun productDetail(id: Long) = "product/$id"
    fun orderDetail(id: Long) = "orders/$id"
}
