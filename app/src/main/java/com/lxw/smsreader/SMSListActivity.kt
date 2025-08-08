package com.lxw.smsreader

import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.database.Cursor
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.Telephony
import android.text.TextUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.lxw.smsreader.DataStoreUtil.dataStore
import com.lxw.smsreader.ui.theme.SMSReaderTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

class SMSListActivity : ComponentActivity() {
    private val SMS_PERMISSION_REQUEST_CODE = 1001
    private val SMS_LAST_MONTH_AMOUNT = 6
    private val SMS_CONTRACT_MAP = mapOf(
        "110" to "报警电话",
        "119" to "火警电话",
        "120" to "急救中心",
        "122" to "交通事故报警",
        "114" to "电话查号台",
        "10086" to "中国移动",
        "10010" to "中国联通",
        "10000" to "中国电信",
        "10011" to "中国联通",
        "10001" to "中国电信",
        "12306" to "中国铁路客服（火车票服务）",
        "12315" to "消费者投诉举报（市场监督管理）",
        "12320" to "卫生热线（公共卫生、健康咨询）",
        "12345" to "政务服务便民热线（市长热线）",
        "12333" to "人力资源和社会保障服务热线",
        "12358" to "价格举报电话",
        "12366" to "税务服务热线",
        "12369" to "环保举报热线",
        "12371" to "党员咨询服务热线",
        "12377" to "网络违法和不良信息举报",
        "12385" to "残疾人服务热线",
        "12388" to "纪检监察举报",
        "12389" to "公安机关举报投诉",
        "12393" to "医保服务热线（部分地区）",
        "12318" to "文化市场举报热线",
        "12308" to "外交部全球领事保护与服务应急热线（海外中国公民求助）",
        "12117" to "报时服务",
        "12110" to "短信报警（部分地区支持）",
        "12121" to "气象服务电话",
        "12122" to "高速公路救援与路况咨询",
        "12123" to "交管12123（交通管理服务）",
        "95588" to "工商银行客服",
        "95599" to "农业银行客服",
        "95566" to "中国银行客服",
        "95533" to "建设银行客服",
        "95555" to "招商银行客服",
        "95518" to "中国人保财险客服",
        "95519" to "中国人寿客服",
        "95500" to "平安保险客服",
        "95589" to "太平洋保险客服",
        "95598" to "国家电网供电服务",
        "96110" to "反电信网络诈骗专用号码",
        "96120" to "心理援助热线（部分地区）",
        "800-810-5118" to "苹果中国客服（固话免费）",
        "400-666-8800" to "京东客服",
        "400-616-1111" to "小米客服",
        "400-810-5555" to "华为消费者服务热线",
        "400-666-6666" to "滴滴出行客服",
        "400-820-0000" to "携程旅行客服",
        "400-822-2000" to "饿了么客服",
        "400-882-0666" to "美团客服"
    )


    private var messageGroupMap: MutableMap<String, MessageGroupInfo> = mutableMapOf()
    private var messageGroupList: MutableList<MessageGroupInfo> = mutableListOf()
    private var smsObserver: ContentObserver? = null
    private val viewModel: DataViewModel = DataViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMSReaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SMSListPage(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            requestPermissions()
        }
    }

    private fun registerSmsObserver() {
        if (smsObserver == null) {
            // 短信URI
            val uri = Telephony.Sms.CONTENT_URI
            val smsObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
                var lastUpdateTime: Long = System.currentTimeMillis()

                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastUpdateTime > 1000) {
                        lastUpdateTime = currentTime
                        requestPermissions()
                    }
                }
            }
            contentResolver.registerContentObserver(uri, true, smsObserver)
        }
    }

    private fun requestPermissions() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 没有权限，请求权限
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_SMS,
                    android.Manifest.permission.RECEIVE_SMS,
                    android.Manifest.permission.READ_PHONE_NUMBERS,
                    android.Manifest.permission.READ_CONTACTS
                ),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            // 已经有权限，读取短信
            GlobalScope.launch(Dispatchers.Default) {
                readSmsList()
                registerSmsObserver()
            }
        }
    }

    private fun readSmsList() {
        messageGroupMap = mutableMapOf()
        val messageGroupTempList: MutableList<MessageGroupInfo> = mutableListOf()

        // 读取收件箱短信
        val uri = Telephony.Sms.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.READ
        )
        // 计算6个月前的起始时间
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -SMS_LAST_MONTH_AMOUNT)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // 查询条件：最近3个月
        val selection = "${Telephony.Sms.DATE} >= ?"
        val selectionArgs = arrayOf(startTime.toString())
        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val indexId = it.getColumnIndex(Telephony.Sms._ID)
            val indexType = it.getColumnIndex(Telephony.Sms.TYPE)
            val indexAddress = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val indexDate = it.getColumnIndex(Telephony.Sms.DATE)
            val indexRead = it.getColumnIndex(Telephony.Sms.READ)
            val indexBody = it.getColumnIndex(Telephony.Sms.BODY)
            while (it.moveToNext()) {
                val id = it.getLong(indexId)
                val type = it.getInt(indexType)
                val address = it.getString(indexAddress)
                val date = it.getLong(indexDate)
                var read = it.getInt(indexRead)
                val body = it.getString(indexBody).trim()
                val time = formatTime(date)
                val contract = getContactName(address)
                if (read != 1) {
                    val localReadStatus = runBlocking {
                        dataStore.data.map { data ->
                            data[booleanPreferencesKey(DataStoreUtil.key(id))] ?: false
                        }.first()
                    }
                    if (localReadStatus) {
                        read = 1
                    }
                }
                val messageInfo = MessageInfo(
                    id,
                    type,
                    address.trim(),
                    date,
                    read,
                    time,
                    contract?.trim(),
                    body.trim()
                )
                var messageGroupInfo = messageGroupMap[address]
                if (messageGroupInfo == null) {
                    messageGroupInfo = MessageGroupInfo(address)
                    messageGroupMap[address] = messageGroupInfo
                    messageGroupTempList.add(messageGroupInfo)
                }
                messageGroupInfo.messages.add(messageInfo)
            }
            messageGroupTempList.map { messageGroupInfo ->
                messageGroupInfo.unReadCount =
                    messageGroupInfo.messages.count { messageInfo -> messageInfo.read != 1 }
            }

            GlobalScope.launch(Dispatchers.Main) {
                messageGroupList = messageGroupTempList
                viewModel.updateState(messageGroupList)
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM.dd HH:mm", Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    // 根据电话号码查询联系人名称
    private fun getContactName(phoneNumber: String): String? {
        // 如果号码为空，返回null
        if (phoneNumber.isBlank()) return null
        var name: String? = null
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        contentResolver.query(uri, projection, null, null, null)?.use { contactCursor ->
            if (contactCursor.moveToFirst()) {
                val index = contactCursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                if (index >= 0 && !TextUtils.isEmpty(contactCursor.getString(index))) {
                    name = contactCursor.getString(index)
                }
            }
        }

        if (TextUtils.isEmpty(name)) {
            name = SMS_CONTRACT_MAP[phoneNumber]
        }
        return name
    }
}

@Composable
fun SMSListPage(viewModel: DataViewModel, modifier: Modifier = Modifier) {
    val messageGroups by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    Column(modifier = modifier) {
        Text(
            text = "短信",
            style = MaterialTheme.typography.displaySmall.copy(color = Color.Black),
            modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 10.dp)
        )
        HorizontalDivider()
        // 列表布局
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            items(messageGroups) { messageGroupInfo ->
                val messageInfo = messageGroupInfo.messages[0]
                Box(modifier = Modifier.clickable {
                    val intent = Intent(context, SMSChatActivity::class.java)
                    intent.putExtra("messages", ArrayList(messageGroupInfo.messages))
                    context.startActivity(intent)
                }) {
                    Column() {
                        Spacer(modifier = Modifier.height(15.dp))
                        if (!TextUtils.isEmpty(messageInfo.contract)) {
                            Text(
                                text = messageInfo.contract!!,
                                style = MaterialTheme.typography.displaySmall.copy(color = Color.Blue),
                            )
                        } else {
                            Text(
                                text = messageInfo.address,
                                style = MaterialTheme.typography.displaySmall.copy(color = Color.Blue)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = messageInfo.time,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    color = Color.DarkGray,
                                    fontSize = 45.sp
                                )
                            )
                            if (messageGroupInfo.unReadCount > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(Color.Red, shape = CircleShape)
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = messageGroupInfo.unReadCount.toString(),
                                        style = MaterialTheme.typography.displaySmall.copy(
                                            color = Color.White,
                                            fontSize = 40.sp
                                        ),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = messageInfo.body,
                            style = MaterialTheme.typography.displaySmall.copy(color = Color.Black),
                            textAlign = TextAlign.Start,
                            maxLines = 3,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SMSListPagePreview() {
    SMSReaderTheme {
        SMSListPage(DataViewModel())
    }
}