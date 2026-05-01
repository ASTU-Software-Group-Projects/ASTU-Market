package com.market.astu.ui.screens.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.market.astu.data.model.DeliveryGig
import com.market.astu.data.model.User
import com.market.astu.data.repository.DeliveryRepository
import com.market.astu.util.toReadableMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GigBoardUiState(
    val runner: User? = null,
    val gigs: List<DeliveryGig> = emptyList(),
    val isLoading: Boolean = true,
    val isActivating: Boolean = false,
    val isAcceptingGigId: String? = null,
    val errorMessage: String? = null
)

sealed interface GigBoardEvent {
    data class Message(val value: String) : GigBoardEvent
}

@HiltViewModel
class GigBoardViewModel @Inject constructor(
    private val deliveryRepository: DeliveryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(GigBoardUiState())
    val uiState: StateFlow<GigBoardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GigBoardEvent>()
    val events: SharedFlow<GigBoardEvent> = _events.asSharedFlow()

    init {
        loadBoard()
    }

    fun loadBoard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val runnerResult = deliveryRepository.getRunnerProfile()
            if (runnerResult.isFailure) {
                _uiState.value = GigBoardUiState(
                    isLoading = false,
                    errorMessage = runnerResult.exceptionOrNull()
                        .toReadableMessage("We couldn't load your runner profile.")
                )
                return@launch
            }

            val runner = runnerResult.getOrThrow()
            val gigsResult = deliveryRepository.getAvailableGigs()
            _uiState.value = GigBoardUiState(
                runner = runner,
                gigs = gigsResult.getOrElse { emptyList() },
                isLoading = false,
                errorMessage = gigsResult.exceptionOrNull()
                    ?.toReadableMessage("We couldn't load delivery gigs.")
                    ?.takeIf { gigsResult.isFailure }
            )
        }
    }

    fun activateRunner() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivating = true, errorMessage = null)
            val result = deliveryRepository.activateRunner()
            if (result.isSuccess) {
                _events.emit(GigBoardEvent.Message("Runner protocol unlocked. Your trust score is now live."))
                loadBoard()
            } else {
                _uiState.value = _uiState.value.copy(
                    isActivating = false,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't activate runner mode.")
                )
            }
        }
    }

    fun acceptGig(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAcceptingGigId = orderId, errorMessage = null)
            val result = deliveryRepository.acceptGig(orderId)
            if (result.isSuccess) {
                _events.emit(GigBoardEvent.Message("Gig accepted. Head to pickup and keep the handoff moving."))
                loadBoard()
            } else {
                _uiState.value = _uiState.value.copy(
                    isAcceptingGigId = null,
                    errorMessage = result.exceptionOrNull()
                        .toReadableMessage("We couldn't accept this gig.")
                )
            }
        }
    }
}
