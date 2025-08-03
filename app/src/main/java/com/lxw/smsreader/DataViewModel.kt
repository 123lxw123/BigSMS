package com.lxw.smsreader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {

    // UI 状态管理
    private val _uiState = MutableStateFlow<MutableList<MessageGroupInfo>>(mutableListOf())
    val uiState: StateFlow<MutableList<MessageGroupInfo>> = _uiState

    // 从子线程加载数据
    fun updateState(messageGroupList: MutableList<MessageGroupInfo>) {
        viewModelScope.launch {
            _uiState.value = messageGroupList
        }
    }
}