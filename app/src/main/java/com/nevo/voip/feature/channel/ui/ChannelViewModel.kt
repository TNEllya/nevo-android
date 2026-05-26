package com.nevo.voip.feature.channel.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nevo.voip.core.model.ChannelInfo
import com.nevo.voip.core.model.UserInfo
import com.nevo.voip.feature.channel.data.ChannelRepository
import com.nevo.voip.feature.connection.data.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FlatChannelNode(
    val channel: ChannelInfo,
    val depth: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean
)

data class ChannelUiState(
    val channels: List<FlatChannelNode> = emptyList(),
    val currentChannelId: Long = 0,
    val currentChannelName: String = "",
    val isInChannel: Boolean = false,
    val usersInChannel: List<UserInfo> = emptyList(),
    val expandedChannelIds: Set<Long> = emptySet(),
    val isMuted: Boolean = false,
    val isDeafened: Boolean = false
)

@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val channelRepository: ChannelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChannelUiState())
    val uiState: StateFlow<ChannelUiState> = _uiState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isDeafened = MutableStateFlow(false)
    val isDeafened: StateFlow<Boolean> = _isDeafened.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepository.channelListUpdates.collect { update ->
                val expanded = _uiState.value.expandedChannelIds
                val flatList = flattenChannels(update.channels, expanded)
                _uiState.update { state ->
                    state.copy(
                        channels = flatList,
                        isInChannel = state.currentChannelId != 0L
                    )
                }
                if (update.channels.isNotEmpty() && _uiState.value.currentChannelId == 0L) {
                    val firstChannel = update.channels.first()
                    _uiState.update { it.copy(
                        currentChannelId = firstChannel.id,
                        currentChannelName = firstChannel.name,
                        usersInChannel = firstChannel.users
                    )}
                }
            }
        }
        viewModelScope.launch {
            connectionRepository.userJoinedChannel.collect { event ->
                if (event.channelId == _uiState.value.currentChannelId && event.user != null) {
                    _uiState.update { state ->
                        val updated = state.usersInChannel.toMutableList()
                        if (updated.none { it.id == event.user!!.id }) {
                            updated.add(event.user!!)
                        }
                        state.copy(usersInChannel = updated)
                    }
                }
            }
        }
        viewModelScope.launch {
            connectionRepository.userLeftChannel.collect { event ->
                if (event.channelId == _uiState.value.currentChannelId) {
                    _uiState.update { state ->
                        state.copy(
                            usersInChannel = state.usersInChannel.filter { it.id != event.userId }
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            connectionRepository.userSpeaking.collect { _ -> }
        }
    }

    fun joinChannel(channelId: Long) {
        viewModelScope.launch {
            channelRepository.joinChannel(channelId).onSuccess {
                val channel = findChannelById(
                    _uiState.value.channels.map { it.channel },
                    channelId
                )
                _uiState.update { state ->
                    state.copy(
                        currentChannelId = channelId,
                        currentChannelName = channel?.name ?: state.currentChannelName,
                        isInChannel = true,
                        usersInChannel = channel?.users ?: state.usersInChannel
                    )
                }
            }
        }
    }

    fun leaveChannel() {
        viewModelScope.launch {
            channelRepository.leaveChannel().onSuccess {
                _uiState.update { state ->
                    state.copy(
                        currentChannelId = 0,
                        currentChannelName = "",
                        isInChannel = false,
                        usersInChannel = emptyList()
                    )
                }
            }
        }
    }

    fun createChannel(name: String, parentId: Long) {
        viewModelScope.launch {
            channelRepository.createChannel(name, parentId)
        }
    }

    fun deleteChannel(channelId: Long) {
        viewModelScope.launch {
            channelRepository.deleteChannel(channelId)
        }
    }

    fun renameChannel(channelId: Long, newName: String) {
        viewModelScope.launch {
            channelRepository.renameChannel(channelId, newName)
        }
    }

    fun toggleExpand(channelId: Long) {
        _uiState.update { state ->
            val updated = state.expandedChannelIds.toMutableSet()
            if (updated.contains(channelId)) {
                updated.remove(channelId)
            } else {
                updated.add(channelId)
            }
            val rootChannels = state.channels.map { it.channel }
            val allChannels = findAllChannels(rootChannels)
            val newFlatList = flattenChannels(allChannels, updated)
            state.copy(
                expandedChannelIds = updated,
                channels = newFlatList
            )
        }
    }

    fun toggleMute() {
        _isMuted.update { !it }
        _uiState.update { it.copy(isMuted = _isMuted.value) }
    }

    fun toggleDeafen() {
        _isDeafened.update { !it }
        _uiState.update { it.copy(isDeafened = _isDeafened.value) }
    }

    private fun flattenChannels(
        channels: List<ChannelInfo>,
        expandedIds: Set<Long>,
        depth: Int = 0
    ): List<FlatChannelNode> {
        val result = mutableListOf<FlatChannelNode>()
        for (channel in channels) {
            val hasChildren = channel.children.isNotEmpty()
            val isExpanded = expandedIds.contains(channel.id)
            result.add(
                FlatChannelNode(
                    channel = channel,
                    depth = depth,
                    hasChildren = hasChildren,
                    isExpanded = isExpanded
                )
            )
            if (hasChildren && isExpanded) {
                result.addAll(flattenChannels(channel.children, expandedIds, depth + 1))
            }
        }
        return result
    }

    private fun findAllChannels(channels: List<ChannelInfo>): List<ChannelInfo> {
        val result = mutableListOf<ChannelInfo>()
        for (channel in channels) {
            result.add(channel)
            result.addAll(findAllChannels(channel.children))
        }
        return result
    }

    private fun findChannelById(
        channels: List<ChannelInfo>,
        channelId: Long
    ): ChannelInfo? {
        for (channel in channels) {
            if (channel.id == channelId) return channel
            val found = findChannelById(channel.children, channelId)
            if (found != null) return found
        }
        return null
    }
}