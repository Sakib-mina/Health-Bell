package com.ideacraftlab.healthbell.ui.dashboard

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ideacraftlab.healthbell.data.manager.BillingManager
import com.ideacraftlab.healthbell.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val userRepository: UserRepository
) : ViewModel() {

    val isBillingReady: StateFlow<Boolean> = billingManager.isBillingReady
    val products = billingManager.products

    val shopItems: StateFlow<List<ShopItemData>> = products.map { productList ->
        productList.map { product ->
            val id = product.productId
            val coins = when (id) {
                "coins_199" -> 200
                "coins_299" -> 400
                else -> id.substringAfter("coins_").toIntOrNull() ?: 0
            }
            ShopItemData(
                title = "$coins Coins",
                price = product.oneTimePurchaseOfferDetails?.formattedPrice?.replace("$", "") ?: "",
                productId = id
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _userCoins = MutableStateFlow(0)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    init {
        observeUserData()
    }

    private fun observeUserData() {
        userRepository.getUserDataFlow()
            .onEach { data ->
                _userCoins.value = data?.coins ?: 0
            }
            .launchIn(viewModelScope)
    }

    fun buyCoins(activity: Activity, productId: String) {
        billingManager.launchBillingFlow(activity, productId)
    }
}
