package com.lxw.smsreader

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.lxw.smsreader.DataStoreUtil.dataStore
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
            dataStore.edit { preferences ->
                messages.forEach {
                    if (it.read != 1) {
                        preferences[booleanPreferencesKey(DataStoreUtil.key(it.id))] = true
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

@Composable
fun SMSChatPage(messageInfoList: List<MessageInfo>, modifier: Modifier = Modifier) {
    val messages = remember {
        messageInfoList.toList()
    }
    val context = LocalContext.current

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

            Column {
                if (!TextUtils.isEmpty(messages[0].contract)) {
                    Text(
                        text = messages[0].contract!!,
                        style = MaterialTheme.typography.displaySmall
                            .copy(color = Color.Blue),
                    )
                }
                BasicTextField(
                    value = messages[0].address,
                    onValueChange = { /* 禁用编辑 */ },
                    readOnly = true,
                    textStyle = MaterialTheme.typography.displaySmall
                        .copy(color = Color.Blue),
                )
            }
        }
        HorizontalDivider()
        // 列表布局
        LazyColumn(
            modifier = Modifier
                .background(Color(0xFFEEEEEE))
                .padding(horizontal = 16.dp)
                .weight(1.0f)
        ) {
            items(messages) { messageInfo ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = messageInfo.time,
                        color = Color.Black,
                        style = MaterialTheme.typography.displaySmall
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

@Preview(showBackground = true)
@Composable
fun SMSChatPagePreview() {
    SMSReaderTheme {
        SMSChatPage(arrayListOf())
    }
}