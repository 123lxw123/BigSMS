package com.lxw.smsreader

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lxw.smsreader.DataStoreUtil.deleteDataStore
import com.lxw.smsreader.DataStoreUtil.readDataStore
import com.lxw.smsreader.ui.theme.SMSReaderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SMSChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val messages = intent?.getSerializableExtra("messages") as List<MessageInfo>
        GlobalScope.launch(Dispatchers.Default) {
            readDataStore.edit { preferences ->
                messages.forEach {
                    if (it.read != 1) {
                        preferences[booleanPreferencesKey(DataStoreUtil.readKey(it.id))] = true
                    }
                }
            }
        }
        setContent {
            SMSReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SMSChatPage(
                        messageInfoList = messages,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SMSChatPage(messageInfoList: List<MessageInfo>, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember {
        messageInfoList.toMutableStateList()
    }
    val firstMessage = remember {
        if (messages.isEmpty()) null else messages[0]
    }
    val dialogInfo = remember { mutableStateOf<MessageInfo?>(null) }
    Column(
        modifier = modifier
            .background(Color.White)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(
                modifier = Modifier
                    .width(16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "返回",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        (context as Activity).finish()
                    }
            )

            Spacer(modifier = Modifier.width(10.dp))

            if (firstMessage != null) {
                Column {
                    if (!TextUtils.isEmpty(firstMessage.contract)) {
                        Text(
                            text = firstMessage.contract!!,
                            style = MaterialTheme.typography.displaySmall
                                .copy(color = Color.Blue),
                        )
                    }
                    BasicTextField(
                        value = firstMessage.address,
                        onValueChange = { /* 禁用编辑 */ },
                        readOnly = true,
                        textStyle = MaterialTheme.typography.displaySmall
                            .copy(color = Color.Blue),
                    )
                }
            }
        }
        HorizontalDivider()
        if (messages.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(Color(0xFFEEEEEE))
                    .padding(16.dp, 80.dp, 16.dp, 10.dp)
            ) {
                Text(
                    text = "没有更多的短信啦",
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.Black)
                )
            }
        } else {
            // 列表布局
            LazyColumn(
                modifier = Modifier
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 16.dp)
                    .weight(1.0f)
            ) {
                items(messages) { messageInfo ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.combinedClickable(
                            onClick = {
                            },
                            onLongClick = {
                                dialogInfo.value = messageInfo
                            }
                        )) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = messageInfo.time,
                            style = MaterialTheme.typography.displaySmall.copy(color = Color.Black)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row {
                            if (messageInfo.type != 1) {
                                Spacer(modifier = Modifier.width(30.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                            ) {
                                Row {
                                    if (messageInfo.type != 1) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White)
                                            .padding(15.dp)
                                    ) {
                                        BasicTextField(
                                            value = messageInfo.body,
                                            onValueChange = { /* 禁用编辑 */ },
                                            readOnly = true,
                                            textStyle = MaterialTheme.typography.displaySmall
                                                .copy(
                                                    color = Color.Black
                                                ),
                                            modifier = Modifier.width(IntrinsicSize.Min)
                                        )
                                    }
                                    if (messageInfo.type == 1) {
                                        Spacer(modifier = Modifier.weight(1.0f))
                                    }
                                }
                            }

                            if (messageInfo.type == 1) {
                                Spacer(modifier = Modifier.width(30.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    if (dialogInfo.value != null) {
        AlertDialog(
            onDismissRequest = {
            },
            title = {
                Text(
                    text = "是否删除该条短信？",
                    style = MaterialTheme.typography.displaySmall.copy(color = Color.Black)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            context.deleteDataStore.edit { preferences ->
                                preferences[booleanPreferencesKey(DataStoreUtil.deleteKey(dialogInfo.value?.id!!))] =
                                    true
                            }
                            messages.remove(dialogInfo.value)
                            dialogInfo.value = null
                        }
                    }
                ) {
                    Text(
                        "确定",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.Blue)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        dialogInfo.value = null
                    }
                ) {
                    Text(
                        "取消",
                        style = MaterialTheme.typography.displaySmall.copy(color = Color.DarkGray)
                    )
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SMSChatPagePreview() {
    SMSReaderTheme {
        SMSChatPage(arrayListOf())
    }
}