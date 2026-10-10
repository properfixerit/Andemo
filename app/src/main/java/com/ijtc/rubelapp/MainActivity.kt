package com.ijtc.rubelapp

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.DownloadManager
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.util.Base64
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.File
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import java.security.SecureRandom
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.geometry.Offset
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import com.ijtc.rubelapp.ui.theme.ExamTheme
import androidx.activity.SystemBarStyle
import kotlin.math.abs

internal data class ExamQuestion(
    val row: Int,
    val question: String,
    val answer: String
)

private data class SimPhoneNumber(
    val label: String,
    val number: String
)

private data class AppUpdateRelease(
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val apkFileName: String,
    val sha256: String
)

internal fun isAppUpdateAvailable(
    installedVersionCode: Long,
    installedVersionName: String,
    releaseVersionCode: Long,
    releaseVersionName: String
): Boolean =
    releaseVersionCode >= installedVersionCode &&
        (releaseVersionCode > installedVersionCode ||
            releaseVersionName != installedVersionName)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            )
        )
        WindowInsetsControllerCompat(window, window.decorView).apply {
            show(WindowInsetsCompat.Type.statusBars())
            isAppearanceLightStatusBars = true
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            ExamTheme {
                AppScreen()
            }
        }
    }
}

private const val DEFAULT_ENTRY_LIST_ADMIN_EMAIL = "properfixer.it@gmail.com"

private data class RegistrationFieldSpec(
    val id: String,
    val label: String,
    val valueIndex: Int
)

private val registrationFields = listOf(
    RegistrationFieldSpec("rgMail", "rgMail", 4),
    RegistrationFieldSpec("rgDevice", "rgDevice", 1),
    RegistrationFieldSpec("rgMobile", "rgMobile", 3),
    RegistrationFieldSpec("rgName", "rgName", 5),
    RegistrationFieldSpec("rgRole", "rgRole", 8),
    RegistrationFieldSpec("rgSwitch", "rgSwitch", 6)
)

private val titleBlue = Color(0xFF3D56A5)
private val clearGreen = Color(0xFF58783A)
private val submitGreen = Color(0xFF7CC33B)
private const val GITHUB_UPDATE_MANIFEST_URL =
    "https://raw.githubusercontent.com/properfixerit/PFAPK/main/RubelApp/update.json"
private const val GITHUB_UPDATE_APK_URL =
    "https://raw.githubusercontent.com/properfixerit/PFAPK/main/RubelApp/app-debug.apk"
private const val SUPABASE_IJTC_FUNCTION = "ijtc"
private val publicWritableFiles = setOf(
    "QuestionList",
    "BankList",
    "ContactList",
    "EntryList",
    "FileList",
    "ItemList",
    "ProductList",
    "TypeList",
    "UnitList",
    "UserList"
)
private const val MAX_FILE_ATTACHMENT_BYTES = 5 * 1024 * 1024
private const val TELEGRAM_CLAIM_URL = "https://web.telegram.org/a/#-1004398824308"
private const val SHARE_APP_URL = "https://properfixerit.github.io/PFAPK/RubelApp"
private const val PREFERENCES_NAME = "questions_preferences"
private const val SAVED_LOGIN_EMAIL_KEY = "saved_login_email"
private const val ON_TEST_QUESTIONS_CACHE_KEY = "on_test_questionlist_first_100_cache"
private const val LEGACY_ON_TEST_QUESTIONS_CACHE_KEY = "on_test_pharmacy_first_100_cache"
private const val LAST_APP_PAGE_KEY = "last_app_page"
private const val LAST_EXAM_NAME_KEY = "last_exam_name"
internal const val SETUP_LIST_TYPE_LANGUAGE_KEY = "setup_list_type_is_bangla"

internal fun sanitizeText(value: String): String =
    value.replace(Regex("[\\r\\n\\t\\u00A0\\u200B]+"), " ").trim()

internal fun isValidLoginCode(value: String): Boolean {
    val normalized = sanitizeText(value).replace(Regex("[\\s-]+"), "")
    return normalized.matches(Regex("^\\d{15}$"))
}

private fun generateRegistrationCode(): String =
    SecureRandom().nextLong(100_000_000_000_000L, 1_000_000_000_000_000L)
        .toString()

internal fun isValidBangladeshiMobile(value: String): Boolean {
    val normalized = sanitizeText(value).replace(Regex("[\\s()\\-]+"), "")
    return normalized.matches(Regex("^(?:\\+?88)?01[3-9]\\d{8}$"))
}

internal fun isValidEmail(value: String): Boolean = sanitizeText(value).matches(
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
)

private fun hasValidatedInternet(
    connectivityManager: ConnectivityManager,
    network: Network? = connectivityManager.activeNetwork
): Boolean {
    val capabilities = network?.let(connectivityManager::getNetworkCapabilities) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

@Composable
private fun rememberInternetAvailability(): Boolean {
    val context = LocalContext.current
    val connectivityManager = remember(context) {
        context.getSystemService(ConnectivityManager::class.java)
            ?: error("Connectivity service is unavailable.")
    }
    var hasInternet by remember(connectivityManager) {
        mutableStateOf(hasValidatedInternet(connectivityManager))
    }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                hasInternet = hasValidatedInternet(connectivityManager, network)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                ) && networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
            }

            override fun onLost(network: Network) {
                hasInternet = hasValidatedInternet(connectivityManager)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        hasInternet = hasValidatedInternet(connectivityManager)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return hasInternet
}

@Composable
private fun rememberResponsiveScale(): Float {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        minOf(
            configuration.screenWidthDp / 393f,
            configuration.screenHeightDp / 780f
        ).coerceIn(0.78f, 1.3f)
    }
}

@Composable
private fun CenterNoticeHost(hostState: SnackbarHostState) {
    val snackbarData = hostState.currentSnackbarData ?: return
    AlertDialog(
        onDismissRequest = snackbarData::dismiss,
        title = { Text("Notice", textAlign = TextAlign.Center) },
        text = { Text(snackbarData.visuals.message, textAlign = TextAlign.Center) },
        confirmButton = {
            TextButton(onClick = snackbarData::dismiss) {
                Text("OK")
            }
        }
    )
}

private data class SubmissionResult(
    val success: Boolean,
    val message: String,
    val duplicate: Boolean = false,
    val requiresPayment: Boolean = false,
    val paymentUrl: String = "",
    val examName: String = "",
    val whatsappUrl: String = "",
    val networkUnavailable: Boolean = false,
    val recoveryRecipientEmail: String = "",
    val appUpdate: AppUpdateRelease? = null,
    val questions: List<ExamQuestion> = emptyList(),
    val fileLines: List<String> = emptyList(),
    val fileSha: String = "",
    val filePath: String = ""
)

private data class FileAttachment(
    val name: String,
    val contentType: String,
    val bytes: ByteArray
)

internal fun fileUploadErrorMessage(statusCode: Int, serverMessage: String): String =
    if (statusCode == 405) {
        "File upload is not enabled on the server. Deploy the updated RubelApp Edge Function and try again."
    } else {
        serverMessage.ifBlank { "Could not upload the file (HTTP $statusCode)." }
    }

internal fun canDeleteQuestionLine(fileName: String, lineCount: Int): Boolean =
    lineCount > 1 || isEnterpriseDataFile(fileName)

internal data class FileAttachmentPath(
    val directory: String,
    val fileId: String,
    val name: String
)

internal fun parseFileAttachmentPath(path: String): FileAttachmentPath? {
    val parts = path.split("/", limit = 3)
    if (parts.size != 3 || parts[0] !in setOf("FileData", "FileList") ||
        !parts[1].matches(Regex("[A-Za-z0-9_-]{1,64}")) ||
        !parts[2].matches(Regex("[A-Za-z0-9][A-Za-z0-9._ -]{0,119}"))
    ) {
        return null
    }
    return FileAttachmentPath(parts[0], parts[1], parts[2])
}

private fun readFileAttachment(
    context: android.content.Context,
    uri: Uri
): FileAttachment {
    val displayName = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null,
        null,
        null
    )?.use { cursor ->
        val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameColumn >= 0 && cursor.moveToFirst()) cursor.getString(nameColumn) else null
    }.orEmpty()
    val safeName = displayName
        .substringAfterLast('/')
        .replace(Regex("[^A-Za-z0-9._ -]"), "_")
        .trim()
        .take(120)
        .takeUnless { it.isBlank() || it == "." || it == ".." }
        ?: "attachment"
    val input = context.contentResolver.openInputStream(uri)
        ?: throw IOException("Could not open the selected file.")
    val output = ByteArrayOutputStream()
    input.use { stream ->
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            if (output.size() + count > MAX_FILE_ATTACHMENT_BYTES) {
                throw IOException("Files must be 5 MiB or smaller.")
            }
            output.write(buffer, 0, count)
        }
    }
    if (output.size() == 0) {
        throw IOException("The selected file is empty.")
    }
    return FileAttachment(
        safeName,
        context.contentResolver.getType(uri) ?: "application/octet-stream",
        output.toByteArray()
    )
}

private fun decodeFileAttachmentPreview(
    attachment: FileAttachment
): androidx.compose.ui.graphics.ImageBitmap? {
    val isImage = attachment.contentType.startsWith("image/") ||
        attachment.name.substringAfterLast('.', "").lowercase(Locale.ROOT) in
        setOf("avif", "bmp", "gif", "jpeg", "jpg", "png", "webp")
    if (!isImage) return null

    val bounds = android.graphics.BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    android.graphics.BitmapFactory.decodeByteArray(
        attachment.bytes,
        0,
        attachment.bytes.size,
        bounds
    )
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > 1200) {
        sampleSize *= 2
    }
    val bitmap = android.graphics.BitmapFactory.decodeByteArray(
        attachment.bytes,
        0,
        attachment.bytes.size,
        android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
    ) ?: return null
    return bitmap.asImageBitmap()
}

@Composable
private fun FileAttachmentPreviewBox(
    path: String,
    attachment: FileAttachment?,
    isLoading: Boolean,
    scale: Float
) {
    var imageBitmap by remember(attachment) {
        mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
    }
    LaunchedEffect(attachment) {
        imageBitmap = attachment?.let {
            withContext(Dispatchers.Default) { decodeFileAttachmentPreview(it) }
        }
    }

    val shape = RoundedCornerShape((18 * scale).dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((190 * scale).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape)
                .background(Color.White)
                .border((2 * scale).dp, Color.Black, shape),
            contentAlignment = Alignment.Center
        ) {
            when {
                imageBitmap != null -> androidx.compose.foundation.Image(
                    bitmap = imageBitmap!!,
                    contentDescription = attachment?.name ?: "File preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((8 * scale).dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
                isLoading -> Text(
                    "Loading preview...",
                    color = Color.DarkGray,
                    fontSize = (14 * scale).sp
                )
                attachment != null -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy((6 * scale).dp)
                ) {
                    Text(
                        text = attachment.name,
                        color = Color.Black,
                        fontSize = (16 * scale).sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = attachment.contentType,
                        color = Color.DarkGray,
                        fontSize = (12 * scale).sp
                    )
                }
                path.isNotBlank() -> Text(
                    text = path.substringAfterLast('/'),
                    color = Color.DarkGray,
                    fontSize = (14 * scale).sp,
                    textAlign = TextAlign.Center
                )
                else -> Text(
                    "No file selected",
                    color = Color.DarkGray,
                    fontSize = (14 * scale).sp
                )
            }
        }
        Text(
            text = "flData",
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (16 * scale).dp, y = (-9 * scale).dp)
                .background(Color.White)
                .padding(horizontal = (6 * scale).dp),
            color = Color.Black,
            fontSize = (14 * scale).sp
        )
    }
}

internal fun parseQuestionListQuestions(text: String): List<ExamQuestion> =
    text.lineSequence()
        .mapIndexedNotNull { index, line ->
            line.removeSuffix("\r")
                .takeIf(String::isNotBlank)
                ?.let { entry ->
                    val separatorIndex = entry.indexOf('=')
                    val question = if (separatorIndex >= 0) {
                        entry.substring(0, separatorIndex).trim()
                    } else {
                        entry
                    }
                    val answer = if (separatorIndex >= 0) {
                        entry.substring(separatorIndex + 1).trim()
                    } else {
                        ""
                    }
                    ExamQuestion(
                        row = index + 2,
                        question = question,
                        answer = answer
                    )
                }
        }
        .toList()

internal val entryListFieldLabels = listOf(
    "enID",
    "enUser",
    "enTrack",
    "enDate",
    "enType",
    "enSector",
    "enInvoice",
    "enContact",
    "enCash",
    "enBank",
    "enBName",
    "enCB",
    "enDiscount",
    "enLabor",
    "enRent",
    "enCashier",
    "enNote",
    "enITotal"
)
internal const val entryListSchemaHeader =
    "enID,enUser,enTrack,enDate,enType,enSector,enInvoice,enContact,enCash,enBank," +
        "enBName,enCB,enDiscount,enLabor,enRent,enCashier,enNote,enITotal"

internal fun parseEntryListLines(text: String): List<String> {
    val lines = text.lineSequence()
        .map { it.removeSuffix("\r") }
        .filter(String::isNotBlank)
        .toList()
    return lines.filterNot { it.trim() == entryListSchemaHeader }
}

internal fun serializeEntryListLines(lines: List<String>): List<String> =
    listOf(entryListSchemaHeader) +
        lines.filterNot { it.trim() == entryListSchemaHeader }

internal data class SetupTypeRecord(
    val english: String,
    val bangla: String,
    val selectedType: String
)

internal fun parseTypeListRecord(
    line: String,
    isBanglaSelected: Boolean = true
): SetupTypeRecord? {
    val fields = line.split(',', limit = 4)
    if (fields.size !in 2..3) return null
    val english = fields[0].trim()
    val bangla = fields[1].trim()
    if (english.isBlank() || bangla.isBlank()) return null
    val selectedType = fields.getOrNull(2)?.trim().takeUnless { it.isNullOrBlank() }
    return SetupTypeRecord(
        english = english,
        bangla = bangla,
        selectedType = selectedType ?: if (isBanglaSelected) bangla else english
    )
}

internal fun typeListRecordsForLanguage(
    records: List<SetupTypeRecord>,
    isBanglaSelected: Boolean
): List<SetupTypeRecord> = records.map { record ->
    record.copy(selectedType = if (isBanglaSelected) record.bangla else record.english)
}

internal fun reorderTypeRecords(
    records: List<SetupTypeRecord>,
    fromIndex: Int,
    toIndex: Int
): List<SetupTypeRecord> {
    if (fromIndex !in records.indices || toIndex !in records.indices ||
        fromIndex == toIndex
    ) {
        return records
    }
    return records.toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}

internal fun typeListLanguageSelection(records: List<SetupTypeRecord>): Boolean? {
    if (records.isEmpty()) return null
    val allBangla = records.all { it.selectedType.equals(it.bangla, ignoreCase = true) }
    val allEnglish = records.all { it.selectedType.equals(it.english, ignoreCase = true) }
    return when {
        allBangla && !allEnglish -> true
        allEnglish && !allBangla -> false
        else -> null
    }
}

internal fun serializeTypeListRecords(records: List<SetupTypeRecord>): List<String> =
    records.map { record ->
        "${record.english.trim()},${record.bangla.trim()},${record.selectedType.trim()}"
    }

internal fun typeListOptions(records: List<SetupTypeRecord>): List<String> {
    val seen = mutableSetOf<String>()
    return records.map { it.selectedType.trim() }
        .filter { it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT)) }
}

internal fun typeListValidationError(
    englishType: String,
    banglaType: String,
    records: List<SetupTypeRecord>,
    editingIndex: Int? = null
): String? {
    val english = englishType.trim()
    val bangla = banglaType.trim()
    return when {
        english.isBlank() || bangla.isBlank() ->
            "Enter both English and Bangla types."
        listOf(english, bangla).any {
            it.contains(',') || it.contains('\n') || it.contains('\r')
        } -> "Commas and line breaks are not allowed."
        records.withIndex().any { (index, row) ->
            index != editingIndex && row.english.equals(english, ignoreCase = true)
        } -> "This English type already exists."
        records.withIndex().any { (index, row) ->
            index != editingIndex && row.bangla.equals(bangla, ignoreCase = true)
        } -> "This Bangla type already exists."
        else -> null
    }
}

internal val enterpriseDataFiles = listOf(
    "BankList" to "Bank List",
    "ContactList" to "Contact List",
    "EntryList" to "Entry List",
    "FileList" to "File List",
    "ItemList" to "Item List",
    "ProductList" to "Product List",
    "RPList" to "RP List",
    "TypeList" to "Type List",
    "UnitList" to "Unit List",
    "UserList" to "User List"
)

internal fun isEnterpriseDataFile(fileName: String): Boolean =
    enterpriseDataFiles.any { (name, _) -> name == fileName }

internal val contactListFieldLabels = listOf(
    "cnID",
    "cnName",
    "cnType",
    "cnMobile",
    "cnAddress",
    "cnOBalance",
    "cnGroup",
    "cnArea",
    "cnNote",
    "cnLBalance"
)
internal val contactListTypeOptions = listOf("কাস্টমার", "সাপ্লায়ার", "উভয়", "অন্যান্য")
internal val contactListCashierTypeOptions =
    listOf("এডমিন", "ম্যানেজার", "সেলসম্যান", "ক্যাশিয়ার")
internal val contactListAllTypeOptions =
    contactListTypeOptions + contactListCashierTypeOptions
internal val bankListFieldLabels = listOf(
    "bnID",
    "bnName",
    "bnAccount",
    "bnContact",
    "bnBranch",
    "bnOBalance",
    "bnLBalance"
)
internal val productListFieldLabels =
    listOf("pdID", "pdName", "pdBPrice", "pdSPrice", "pdOStock")
internal const val productListSchemaHeader = "pdID,pdName,pdBPrice,pdSPrice,pdOStock"
internal val userListFieldLabels =
    listOf("usEMail", "usDevice", "usMobile", "usName", "usRole", "usSwitch", "usClaim")
internal val userListRoleOptions =
    listOf("Admin", "Manager", "Cashier", "User", "Marketing")
internal const val userListSchemaHeader =
    "usEMail,usDevice,usMobile,usName,usRole,usSwitch,usClaim"
private const val legacyUserListSchemaHeader = "usEMail,usDevice,usMobile,usRole,usSwitch"
private const val previousUserListSchemaHeader =
    "usEMail,usDevice,usMobile,usName,usRole,usSwitch"

private fun encodeUserClaim(claim: String): String =
    claim.trim()
        .replace("\\", "\\\\")
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace("\n", "\\n")
        .replace(",", "\\c")

private fun decodeUserClaim(claim: String): String {
    val decoded = StringBuilder(claim.length)
    var index = 0
    while (index < claim.length) {
        if (claim[index] == '\\' && index + 1 < claim.length) {
            when (claim[index + 1]) {
                '\\' -> {
                    decoded.append('\\')
                    index += 2
                }
                'c' -> {
                    decoded.append(',')
                    index += 2
                }
                'n' -> {
                    decoded.append('\n')
                    index += 2
                }
                else -> decoded.append(claim[index++])
            }
        } else {
            decoded.append(claim[index++])
        }
    }
    return decoded.toString()
}

private fun serializeUserListFields(fields: List<String>): String =
    fields.mapIndexed { index, value ->
        if (index == 6) encodeUserClaim(value) else value.trim()
    }.joinToString(",")

internal fun parseUserListFields(entry: String): List<String> {
    val fields = entry.split(",", limit = userListFieldLabels.size)
    val normalizedFields = if (fields.size <= 5) {
        fields.take(3) + "" + fields.drop(3)
    } else {
        fields.take(userListFieldLabels.size)
    }
    val paddedFields = normalizedFields +
        List((userListFieldLabels.size - normalizedFields.size).coerceAtLeast(0)) { "" }
    return paddedFields.mapIndexed { index, value ->
        if (index == 6) decodeUserClaim(value) else value
    }
}

internal fun normalizeUserListLines(lines: List<String>): List<String> {
    val dataLines = if (lines.firstOrNull()?.trim() in setOf(
            userListSchemaHeader,
            legacyUserListSchemaHeader,
            previousUserListSchemaHeader
        )
    ) {
        lines.drop(1)
    } else {
        lines
    }
    return dataLines.filter(String::isNotBlank).map { line ->
        serializeUserListFields(parseUserListFields(line))
    }
}

internal fun serializeUserListLines(lines: List<String>): List<String> =
    listOf(userListSchemaHeader) + lines

internal fun newUserListFields(): List<String> =
    listOf("", "", "", "", "", "On", "")

internal fun buildUserListLine(fields: List<String>): String? {
    if (fields.size != userListFieldLabels.size ||
        fields.take(4).any(String::isBlank) ||
        !fields[0].trim().matches(
            Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        ) ||
        fields[5].isNotBlank() && fields[5] !in setOf("On", "Off") ||
        fields.take(6).any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return serializeUserListFields(fields)
}

internal fun updateUserListClaim(
    userListLines: List<String>,
    email: String,
    deviceId: String,
    claim: String
): List<String>? {
    if (email.isBlank() || deviceId.isBlank() || claim.isBlank()) return null
    val rows = normalizeUserListLines(userListLines).toMutableList()
    val matchingIndex = rows.indexOfFirst { line ->
        val fields = parseUserListFields(line)
        fields[0].equals(email.trim(), ignoreCase = true) && fields[1] == deviceId
    }
    if (matchingIndex < 0) return null

    val fields = parseUserListFields(rows[matchingIndex]).toMutableList()
    fields[6] = claim.trim()
    rows[matchingIndex] = serializeUserListFields(fields)
    return rows
}

internal fun isUserLoginAllowed(
    userListLines: List<String>,
    email: String,
    deviceId: String
): Boolean {
    if (email.isBlank() || deviceId.isBlank()) return false
    return normalizeUserListLines(userListLines)
        .asSequence()
        .map(::parseUserListFields)
        .any { fields ->
            fields[0].equals(email.trim(), ignoreCase = true) &&
                fields[1] == deviceId &&
                fields[5] == "On"
        }
}

internal val itemListFieldLabels =
    listOf("itID", "pdName", "itUnit", "itRate", "itQty", "itTotal", "itNote")
internal val itemListFormFieldLabels =
    listOf("itID", "itProduct", "itRate", "itQty", "itUnit", "itTotal", "itNote")

internal fun parseItemListFields(entry: String): List<String> =
    entry.split(",", limit = itemListFieldLabels.size + 3)
        .let { fields ->
            val migratedFields = when {
                fields.size >= itemListFieldLabels.size + 2 ->
                    fields.drop(1).take(1) + fields.drop(3)
                fields.size >= itemListFieldLabels.size + 1 ->
                    fields.drop(1)
                else -> fields
            }.take(itemListFieldLabels.size)
            migratedFields +
                List(itemListFieldLabels.size - migratedFields.size) { "" }
        }

internal fun normalizeItemListLines(lines: List<String>): List<String> =
    lines.map { line -> parseItemListFields(line).joinToString(",") }

internal fun removeItemListRowsForEntryId(
    itemEntries: List<String>,
    entryId: String
): Pair<List<String>, Boolean> {
    val normalizedEntries = normalizeItemListLines(itemEntries)
    if (entryId.isBlank()) return normalizedEntries to false
    val remainingEntries = normalizedEntries.filterNot { entry ->
        parseItemListFields(entry)[0].trim() == entryId.trim()
    }
    return remainingEntries to (remainingEntries.size != normalizedEntries.size)
}

internal fun newItemListFields(entryId: String = ""): List<String> =
    listOf(entryId) + List(itemListFieldLabels.size - 1) { "" }

internal fun buildItemListLine(fields: List<String>): String? {
    if (fields.size != itemListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[1].isBlank() ||
        listOf(3, 4, 5).any { fields[it].isBlank() } ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun parseProductListFields(entry: String): List<String> {
    val fields = entry.split(",", limit = productListFieldLabels.size + 1)
    return fields.take(productListFieldLabels.size) +
        List((productListFieldLabels.size - fields.size).coerceAtLeast(0)) { "" }
}

internal fun migrateLegacyProductListLine(entry: String): String {
    val fields = entry.split(",")
    val productFields = when {
        fields.size >= 6 ->
            listOf(fields[0], fields[2], fields[4], fields[5], fields[3])
        fields.size >= 5 ->
            listOf(fields[0], fields[1], fields[3], fields[4], fields[2])
        fields.size == 4 ->
            listOf(formatEntryListId(Date()), fields[0], fields[1], fields[2], fields[3])
        fields.size >= 2 ->
            listOf(formatEntryListId(Date()), fields[0], fields[1], "", "")
        else ->
            listOf(formatEntryListId(Date()), fields.firstOrNull().orEmpty(), "", "", "")
    }
    return productFields.joinToString(",")
}

internal fun normalizeProductListLines(lines: List<String>): List<String> {
    val header = lines.firstOrNull()?.trim()
    return when (header) {
        productListSchemaHeader -> lines.drop(1)
        "pdName,pdBPrice,pdSPrice,pdOStock" ->
            lines.drop(1).mapIndexed { index, line ->
                (listOf(formatEntryListId(Date(System.currentTimeMillis() + index))) +
                    parseProductListFieldsWithoutId(line)).joinToString(",")
            }
        "pdID,pdName,pdUnit,pdBPrice,pdSPrice,pdOStock" ->
            lines.drop(1).map { line ->
                val fields = line.split(",")
                listOf(
                    fields.getOrElse(0) { "" },
                    fields.getOrElse(1) { "" },
                    fields.getOrElse(3) { "" },
                    fields.getOrElse(4) { "" },
                    fields.getOrElse(5) { "" }
                ).joinToString(",")
            }
        "pdID,pdName,pdBPrice,pdSPrice,pdOStock" ->
            lines.drop(1).map { line ->
                val fields = line.split(",")
                listOf(
                    fields.getOrElse(0) { "" },
                    fields.getOrElse(1) { "" },
                    fields.getOrElse(2) { "" },
                    fields.getOrElse(3) { "" },
                    fields.getOrElse(4) { "" }
                ).joinToString(",")
            }
        "pdName,pdUnit,pdBPrice,pdSPrice,pdOStock" ->
            lines.drop(1).mapIndexed { index, line ->
                val fields = line.split(",")
                listOf(
                    formatEntryListId(Date(System.currentTimeMillis() + index)),
                    fields.getOrElse(0) { "" },
                    fields.getOrElse(2) { "" },
                    fields.getOrElse(3) { "" },
                    fields.getOrElse(4) { "" }
                ).joinToString(",")
            }
        else -> lines.map(::migrateLegacyProductListLine)
    }
}

private fun parseProductListFieldsWithoutId(entry: String): List<String> {
    val fields = entry.split(",", limit = productListFieldLabels.size)
    return fields.take(4) + List((4 - fields.size).coerceAtLeast(0)) { "" }
}

internal fun serializeProductListLines(lines: List<String>): List<String> =
    listOf(productListSchemaHeader) + lines

internal fun newProductListFields(id: String = formatEntryListId(Date())): List<String> =
    listOf(id) + List(productListFieldLabels.size - 1) { "" }

internal fun buildProductListLine(fields: List<String>): String? {
    if (fields.size != productListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[1].isBlank() ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun parseProductListNames(productEntries: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return productEntries.mapNotNull { entry ->
        parseProductListFields(entry)[1].trim().takeIf {
            it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal val unitListFieldLabels = listOf("utName", "utValue", "utUV", "utCount")

internal fun unitListDisplayValue(name: String, value: String): String =
    if (name.isBlank() || value.isBlank()) "" else "${name.trim()} (${value.trim()})"

internal fun parseUnitListFields(entry: String): List<String> {
    val fields = entry.split(",")
    val (name, value, count) = when {
        fields.size >= 5 && fields[2] != unitListDisplayValue(fields[0], fields[1]) ->
            Triple(fields.getOrElse(2) { "" }, fields.getOrElse(4) { "" }, "")
        fields.size >= 5 ->
            Triple(fields[0], fields[1], fields[3])
        fields.size == 4 &&
            fields[2] == unitListDisplayValue(fields[0], fields[1]) ->
            Triple(fields[0], fields[1], fields[3])
        fields.size == 4 -> Triple(fields[2], "", "")
        fields.size == 3 && fields[2] == unitListDisplayValue(fields[0], fields[1]) ->
            Triple(fields[0], fields[1], "")
        fields.size == 3 -> Triple(fields[0], fields[2], "")
        fields.size == 2 -> Triple(fields[0], fields[1], "")
        else -> Triple(fields.firstOrNull().orEmpty(), "", "")
    }
    return listOf(name, value, unitListDisplayValue(name, value), count)
}

internal fun shouldShowUnitDeleteAction(entry: String): Boolean =
    (parseUnitListFields(entry)[3].trim().toIntOrNull() ?: 0) <= 0

internal fun normalizeUnitListLines(lines: List<String>): List<String> =
    lines.map { line -> parseUnitListFields(line).joinToString(",") }

internal fun countUnitUsage(itemEntries: List<String>, unitDisplayValue: String): Int {
    if (unitDisplayValue.isBlank()) return 0
    return itemEntries.count { entry ->
        parseItemListFields(entry)[2].trim()
            .equals(unitDisplayValue.trim(), ignoreCase = true)
    }
}

internal fun populateUnitListCounts(
    unitEntries: List<String>,
    itemEntries: List<String>
): List<String> =
    normalizeUnitListLines(unitEntries).map { entry ->
        parseUnitListFields(entry).toMutableList().apply {
            this[3] = countUnitUsage(itemEntries, this[2]).toString()
        }.joinToString(",")
    }

internal fun newUnitListFields(): List<String> =
    listOf("", "", "", "0")

internal fun buildUnitListLine(fields: List<String>): String? {
    if (fields.size != unitListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[1].isBlank() ||
        fields[2] != unitListDisplayValue(fields[0], fields[1]) ||
        fields[3].trim().toBigDecimalOrNull() == null ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun isDuplicateUnitDisplayValue(
    unitEntries: List<String>,
    displayValue: String,
    excludedIndex: Int? = null
): Boolean {
    val normalizedValue = displayValue.trim().lowercase(Locale.ROOT)
    if (normalizedValue.isBlank()) return false
    return unitEntries.withIndex().any { (index, entry) ->
        index != excludedIndex &&
            parseUnitListFields(entry)[2].trim().lowercase(Locale.ROOT) == normalizedValue
    }
}

internal fun parseUnitListNames(unitEntries: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return unitEntries.mapNotNull { entry ->
        parseUnitListFields(entry)[0].trim().takeIf {
            it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun parseUnitListDisplayOptions(unitEntries: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return unitEntries.mapNotNull { entry ->
        parseUnitListFields(entry)[2].trim().takeIf {
            it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun filterItemUnitOptions(options: List<String>, query: String): List<String> =
    if (query.isBlank()) options else options.filter { it.contains(query, ignoreCase = true) }

internal fun calculateItemListTotal(
    rate: String,
    quantity: String,
    unitDisplay: String,
    unitEntries: List<String>
): String? {
    val rateValue = rate.trim().toBigDecimalOrNull() ?: return null
    val quantityValue = quantity.trim().toBigDecimalOrNull() ?: return null
    val unitValue = if (unitDisplay.isBlank()) {
        java.math.BigDecimal.ONE
    } else {
        unitEntries.asSequence()
            .map(::parseUnitListFields)
            .firstOrNull { it[2].equals(unitDisplay.trim(), ignoreCase = true) }
            ?.get(1)
            ?.toBigDecimalOrNull()
            ?: return null
    }
    return rateValue.multiply(quantityValue).multiply(unitValue)
        .stripTrailingZeros()
        .toPlainString()
}

internal val fileListFieldLabels = listOf("flID", "flNote", "flData")
internal const val fileListSchemaHeader = "flID,flNote,flData"

internal fun parseFileListFields(entry: String): List<String> {
    val fields = entry.split(",", limit = fileListFieldLabels.size + 2)
    val currentFields = when {
        fields.size >= fileListFieldLabels.size -> fields.take(fileListFieldLabels.size)
        else -> fields + List(fileListFieldLabels.size - fields.size) { "" }
    }
    return currentFields
}

private fun migrateLegacyFileListFields(entry: String): List<String> {
    val fields = entry.split(",", limit = fileListFieldLabels.size + 2)
    val oldOrderFields = when {
        fields.size >= fileListFieldLabels.size + 1 -> fields.drop(1)
        fields.size == fileListFieldLabels.size &&
            parseFileAttachmentPath(fields[2]) != null ->
            fields.drop(1) + ""
        fields.size == 2 && fields[0].matches(Regex("\\d{16}")) ->
            listOf(fields[0], fields[1], "")
        else -> fields
    }
    val paddedFields = oldOrderFields.take(fileListFieldLabels.size) +
        List((fileListFieldLabels.size - oldOrderFields.size).coerceAtLeast(0)) { "" }
    return listOf(paddedFields[0], paddedFields[2], paddedFields[1])
}

internal fun normalizeFileListLines(lines: List<String>): List<String> {
    val hasCurrentSchemaHeader = lines.firstOrNull()?.trim() == fileListSchemaHeader
    val dataLines = if (hasCurrentSchemaHeader) lines.drop(1) else lines
    return dataLines.map { line ->
        val fields = if (hasCurrentSchemaHeader) {
            parseFileListFields(line)
        } else {
            migrateLegacyFileListFields(line)
        }
        fields.joinToString(",")
    }
}

internal fun serializeFileListLines(lines: List<String>): List<String> =
    listOf(fileListSchemaHeader) + lines

internal fun removeFileListRowsForRecordId(
    fileEntries: List<String>,
    recordId: String
): Pair<List<String>, List<String>> {
    val normalizedEntries = normalizeFileListLines(fileEntries)
    if (recordId.isBlank()) return normalizedEntries to emptyList()
    val matchingEntries = normalizedEntries.filter { entry ->
        parseFileListFields(entry)[0].trim() == recordId.trim()
    }
    val remainingEntries = normalizedEntries.filterNot { entry ->
        parseFileListFields(entry)[0].trim() == recordId.trim()
    }
    val attachmentPaths = matchingEntries.mapNotNull { entry ->
        parseFileListFields(entry)[2].trim().takeIf {
            parseFileAttachmentPath(it) != null
        }
    }.distinct()
    return remainingEntries to attachmentPaths
}

internal fun newFileListFields(id: String = ""): List<String> =
    listOf(id) + List(fileListFieldLabels.size - 1) { "" }

internal fun buildFileListLine(fields: List<String>): String? {
    if (fields.size != fileListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[2].isBlank() ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun parseBankListFields(entry: String): List<String> =
    entry.split(",", limit = bankListFieldLabels.size + 1)
        .take(bankListFieldLabels.size)
        .let { fields ->
            fields + List(bankListFieldLabels.size - fields.size) { "" }
        }

internal fun entryBankNameOptions(
    bankEntries: List<String>,
    contactName: String
): List<String> {
    if (contactName.isBlank()) return emptyList()
    val seen = mutableSetOf<String>()
    return bankEntries.mapNotNull { entry ->
        val fields = parseBankListFields(entry)
        fields[1].trim().takeIf {
            fields[3].trim().equals(contactName.trim(), ignoreCase = true) &&
                it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun isEntryFieldVisible(label: String, fields: List<String>): Boolean =
    when (label) {
        "enBank" -> fields.getOrNull(7).orEmpty().isNotBlank()
        "enBName" -> fields.getOrNull(9).orEmpty().isNotBlank()
        else -> true
    }

internal fun buildBankListLine(fields: List<String>): String? {
    if (fields.size != bankListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[1].isBlank() ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun parseContactListFields(entry: String): List<String> =
    entry.split(",", limit = contactListFieldLabels.size + 1)
        .take(contactListFieldLabels.size)
        .let { fields ->
            fields + List(contactListFieldLabels.size - fields.size) { "" }
        }

internal fun linkedFileSearchId(fileName: String, entry: String): String =
    when (fileName) {
        "ContactList" -> parseContactListFields(entry)[0]
        "EntryList" -> parseEntryListFields(entry)[0]
        "BankList" -> parseBankListFields(entry)[0]
        "ProductList" -> parseProductListFields(entry)[0]
        "UserList" -> parseUserListFields(entry)[0]
        else -> ""
    }.trim()

internal fun newContactListFields(id: String): List<String> =
    listOf(id) + List(contactListFieldLabels.size - 1) { "" }

internal fun parseEntryContactNames(contactEntries: List<String>): List<String> =
    contactEntries
        .map { parseContactListFields(it)[1].trim() }
        .filter(String::isNotEmpty)
        .distinct()

internal fun parseEntryCashierNames(contactEntries: List<String>): List<String> {
    val seen = mutableSetOf<String>()
    return contactEntries.mapNotNull { entry ->
        val fields = parseContactListFields(entry)
        fields[1].trim().takeIf {
            fields[2].trim() in contactListCashierTypeOptions &&
                it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun contactListColumnOptions(
    contactEntries: List<String>,
    columnIndex: Int
): List<String> {
    if (columnIndex !in listOf(6, 7)) return emptyList()
    val seen = mutableSetOf<String>()
    return contactEntries.mapNotNull { entry ->
        parseContactListFields(entry)[columnIndex].trim().takeIf {
            it.isNotEmpty() && seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun buildContactListLine(fields: List<String>): String? {
    if (fields.size != contactListFieldLabels.size ||
        fields[0].isBlank() ||
        fields[1].isBlank() ||
        (fields[2] !in contactListTypeOptions &&
            fields[2] !in contactListCashierTypeOptions) ||
        fields.any { ',' in it || '\n' in it || '\r' in it }
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

internal fun parseEntryListFields(
    entry: String,
    allowedTypes: Collection<String> = entryListTypeOptions
): List<String> {
    val fields = entry.split(",", limit = 19)
    val normalizedFields = when {
        fields.size >= 18 && fields.getOrNull(4) in allowedTypes ->
            fields.take(18)
        fields.getOrNull(4) in allowedTypes ->
            (fields.take(12) + List(4) { "" } + fields.drop(12)).take(18)
        fields.getOrNull(3) in allowedTypes -> {
            val withTrack = fields.take(2) + "" + fields.drop(2)
            (withTrack.take(12) + List(4) { "" } + withTrack.drop(12)).take(18)
        }
        else -> fields.take(18)
    }
    return normalizedFields + List(18 - normalizedFields.size) { "" }
}

internal val entryListItemActionTypes = setOf(
    "Buy",
    "Sell",
    "Adjust",
    "Return",
    "Damage",
    "ক্রয়",
    "বিক্রয়",
    "সমন্বয়",
    "ফেরত",
    "ড্যামেজ"
)

internal fun shouldShowEntryItemAction(entry: String): Boolean =
    parseEntryListFields(entry)[4].trim() in entryListItemActionTypes

internal fun entryListSectorOptions(
    entries: List<String>,
    entryType: String,
    allowedTypes: Collection<String> = entryListTypeOptions
): List<String> {
    if (entryType !in allowedTypes) return emptyList()
    val seen = mutableSetOf<String>()
    return entries.mapNotNull { entry ->
        val fields = parseEntryListFields(entry, allowedTypes)
        fields[5].trim().takeIf {
            fields[4] == entryType && it.isNotEmpty() &&
                seen.add(it.lowercase(Locale.ROOT))
        }
    }
}

internal fun formatEntryListId(date: Date): String =
    SimpleDateFormat("yyMMddHHmmssSSSS", Locale.US).format(date)

internal fun formatEntryListDate(date: Date): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

internal fun calculateEntryCashBankTotal(cash: String, bank: String): String? {
    val cashAmount = cash.trim().takeIf(String::isNotEmpty)?.toBigDecimalOrNull()
        ?: if (cash.isBlank()) java.math.BigDecimal.ZERO else return null
    val bankAmount = bank.trim().takeIf(String::isNotEmpty)?.toBigDecimalOrNull()
        ?: if (bank.isBlank()) java.math.BigDecimal.ZERO else return null
    return cashAmount.add(bankAmount).stripTrailingZeros().toPlainString()
}

internal fun calculateEntryItemTotal(entryId: String, itemEntries: List<String>): String? {
    if (entryId.isBlank()) return null
    var total = java.math.BigDecimal.ZERO
    itemEntries.asSequence()
        .map(::parseItemListFields)
        .filter { it[0].trim() == entryId.trim() }
        .forEach { fields ->
            val itemTotal = fields[5].trim().toBigDecimalOrNull() ?: return null
            total = total.add(itemTotal)
        }
    return total.stripTrailingZeros().toPlainString()
}

internal fun newEntryListFields(
    date: Date,
    userEmail: String = DEFAULT_ENTRY_LIST_ADMIN_EMAIL
): List<String> =
    (
        listOf(
        formatEntryListId(date),
        userEmail.trim(),
        "",
        formatEntryListDate(date),
        ""
        ) + List(13) { "" }
    ).toMutableList().apply {
        this[11] = calculateEntryCashBankTotal(this[8], this[9]).orEmpty()
    }

internal val entryListTypeOptions = listOf("গ্রহণ", "প্রদান", "খরচ")

internal fun formatEntryTrackUrl(latitude: Double, longitude: Double): String? {
    if (!latitude.isFinite() || latitude !in -90.0..90.0 ||
        !longitude.isFinite() || longitude !in -180.0..180.0
    ) {
        return null
    }
    return String.format(
        Locale.US,
        "https://www.google.com/maps/search/?api=1&query=%.7f%%2C%.7f",
        latitude,
        longitude
    )
}

internal fun buildEntryListLine(
    fields: List<String>,
    allowedTypes: Collection<String> = entryListTypeOptions
): String? {
    if (fields.size != 18 ||
        listOf(0, 1, 3, 4).any { fields[it].isBlank() } ||
        fields.any { ',' in it || '\n' in it || '\r' in it } ||
        fields[4] !in allowedTypes
    ) {
        return null
    }
    return fields.joinToString(",") { it.trim() }
}

private fun startCurrentLocationUpdates(
    context: android.content.Context,
    onLocation: (Location) -> Unit,
    onLocationUnavailable: () -> Unit
): () -> Unit {
    val hasFinePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val hasCoarsePermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasFinePermission && !hasCoarsePermission) {
        throw SecurityException("Location permission was not granted.")
    }

    val locationManager = context.getSystemService(LocationManager::class.java)
    val providers = buildList {
        if (hasFinePermission &&
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        ) {
            add(LocationManager.GPS_PROVIDER)
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            add(LocationManager.NETWORK_PROVIDER)
        }
    }
    if (providers.isEmpty()) {
        throw IllegalStateException("No location provider is enabled.")
    }

    val listeners = mutableListOf<LocationListener>()
    val handler = Handler(Looper.getMainLooper())
    var hasReceivedLocation = false
    val firstFixTimeout = Runnable {
        if (!hasReceivedLocation) {
            onLocationUnavailable()
        }
    }
    var lastRequestError: Exception? = null
    providers.forEach { provider ->
        try {
            locationManager.getLastKnownLocation(provider)
                ?.takeIf {
                    val age = System.currentTimeMillis() - it.time
                    age in 0..120_000L
                }
                ?.let { location ->
                    hasReceivedLocation = true
                    onLocation(location)
                }
        } catch (exception: IllegalArgumentException) {
            lastRequestError = exception
        } catch (exception: SecurityException) {
            lastRequestError = exception
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!hasReceivedLocation) {
                    hasReceivedLocation = true
                    handler.removeCallbacks(firstFixTimeout)
                }
                onLocation(location)
            }

            @Deprecated("Deprecated in Android")
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?
            ) = Unit

            override fun onProviderEnabled(provider: String) = Unit

            override fun onProviderDisabled(provider: String) = Unit
        }
        try {
            locationManager.requestLocationUpdates(
                provider,
                1_000L,
                10f,
                listener,
                Looper.getMainLooper()
            )
            listeners.add(listener)
        } catch (exception: IllegalArgumentException) {
            lastRequestError = exception
        } catch (exception: SecurityException) {
            lastRequestError = exception
        }
    }
    if (listeners.isEmpty()) {
        throw lastRequestError ?: IllegalStateException("No location provider is available.")
    }
    if (listeners.isNotEmpty() && !hasReceivedLocation) {
        handler.postDelayed(firstFixTimeout, 30_000L)
    }
    return {
        handler.removeCallbacks(firstFixTimeout)
        listeners.forEach(locationManager::removeUpdates)
    }
}

internal fun entryListDisplayOrder(entries: List<String>): List<Pair<Int, String>> =
    entries.mapIndexed { index, entry -> index to entry }.asReversed()

internal fun containsDuplicateQuestionAnswer(
    entries: List<String>,
    question: String,
    answer: String,
    excludedIndex: Int? = null
): Boolean {
    val normalizedQuestion = question.trim()
    val normalizedAnswer = answer.trim()
    return entries.withIndex().any { (index, entry) ->
        if (index == excludedIndex) {
            false
        } else {
            val separatorIndex = entry.indexOf('=')
            val existingQuestion = if (separatorIndex >= 0) {
                entry.substring(0, separatorIndex).trim()
            } else {
                entry.trim()
            }
            val existingAnswer = if (separatorIndex >= 0) {
                entry.substring(separatorIndex + 1).trim()
            } else {
                ""
            }
            existingQuestion.equals(normalizedQuestion, ignoreCase = true) &&
                existingAnswer.equals(normalizedAnswer, ignoreCase = true)
        }
    }
}

internal fun firstOnTestQuestions(questions: List<ExamQuestion>): List<ExamQuestion> =
    questions.take(100)

private fun loadCachedOnTestQuestions(context: android.content.Context): List<ExamQuestion> {
    val preferences = context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
    val cachedQuestions = preferences.getString(ON_TEST_QUESTIONS_CACHE_KEY, null)
        ?: preferences.getString(LEGACY_ON_TEST_QUESTIONS_CACHE_KEY, null)?.also {
            preferences.edit()
                .putString(ON_TEST_QUESTIONS_CACHE_KEY, it)
                .remove(LEGACY_ON_TEST_QUESTIONS_CACHE_KEY)
                .apply()
        }
        ?: return emptyList()
    return try {
        val json = JSONArray(cachedQuestions)
        val questions = List(json.length()) { index ->
            val item = json.getJSONObject(index)
            ExamQuestion(
                row = item.getInt("row"),
                question = item.getString("question"),
                answer = item.getString("answer")
            )
        }
        if (questions.isEmpty()) {
            preferences.edit().remove(ON_TEST_QUESTIONS_CACHE_KEY).apply()
            emptyList()
        } else {
            questions
        }
    } catch (exception: JSONException) {
        Log.w("OnTest", "Discarding invalid cached OnTest questions.", exception)
        preferences.edit().remove(ON_TEST_QUESTIONS_CACHE_KEY).apply()
        emptyList()
    }
}

private fun saveCachedOnTestQuestions(
    context: android.content.Context,
    questions: List<ExamQuestion>
) {
    val serializedQuestions = JSONArray().apply {
        questions.forEach { question ->
            put(
                JSONObject()
                    .put("row", question.row)
                    .put("question", question.question)
                    .put("answer", question.answer)
            )
        }
    }.toString()
    val saved = context.getSharedPreferences(
        PREFERENCES_NAME,
        android.content.Context.MODE_PRIVATE
    ).edit()
        .putString(ON_TEST_QUESTIONS_CACHE_KEY, serializedQuestions)
        .commit()
    if (!saved) {
        Log.e("OnTest", "Could not save OnTest questions to device storage.")
    }
}

private fun fetchPrivateGitHubQuestionFile(
    projectUrl: String,
    anonKey: String,
    fileName: String,
    examTitle: String,
    accessToken: String = anonKey
): SubmissionResult {
    if (fileName !in setOf("QuestionList", "OnTest") && !isEnterpriseDataFile(fileName)) {
        return SubmissionResult(false, "Unsupported exam question file.")
    }
    val baseUrl = projectUrl.trim().trimEnd('/')
    val parsedBaseUrl = try {
        URL(baseUrl)
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The Supabase project URL is invalid.")
    }
    if (parsedBaseUrl.protocol != "https" || parsedBaseUrl.host.isNullOrBlank() ||
        parsedBaseUrl.query != null || parsedBaseUrl.ref != null
    ) {
        return SubmissionResult(false, "Use a valid HTTPS Supabase project URL.")
    }
    if (anonKey.isBlank() || BuildConfig.RUBELAPP_PROXY_API_KEY.isBlank()) {
        return SubmissionResult(false, "Supabase is not configured for exam questions.")
    }
    val requestUrl = Uri.parse("$baseUrl/functions/v1/$SUPABASE_IJTC_FUNCTION")
        .buildUpon()
        .appendQueryParameter("file", fileName)
        .build()
    val connection = try {
        URL(requestUrl.toString()).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The $examTitle questions URL is invalid.")
    } catch (exception: IOException) {
        return SubmissionResult(false, "ইন্টারনেট সংযোগ নেই, চেক করুন!")
    }

    return try {
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 30_000
            setRequestProperty("apikey", BuildConfig.RUBELAPP_PROXY_API_KEY)
            setRequestProperty("Accept", "text/plain")
        }
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            val response = connection.errorStream
                ?.bufferedReader(StandardCharsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            val detail = response.takeIf(String::isNotBlank)?.let {
                try {
                    JSONObject(it).optString("message").takeIf(String::isNotBlank)
                } catch (_: JSONException) {
                    it.take(240)
                }
            }
            return SubmissionResult(
                false,
                detail ?: "Could not load $examTitle questions (HTTP $statusCode)."
            )
        }
        val content = connection.inputStream
            .bufferedReader(StandardCharsets.UTF_8)
            .use { it.readText() }
        val questions = parseQuestionListQuestions(content)
        if (questions.isEmpty() && !isEnterpriseDataFile(fileName)) {
            SubmissionResult(false, "The $examTitle question file is empty.")
        } else {
            SubmissionResult(
                true,
                "$examTitle questions loaded.",
                questions = questions,
                fileLines = parseEntryListLines(content),
                fileSha = connection.getHeaderField("X-GitHub-Content-SHA").orEmpty()
            )
        }
    } catch (exception: IOException) {
        SubmissionResult(
            false,
            "ইন্টারনেট সংযোগ নেই, চেক করুন!"
        )
    } finally {
        connection.disconnect()
    }
}

private fun savePrivateQuestionFile(
    projectUrl: String,
    anonKey: String,
    accessToken: String,
    fileName: String,
    displayName: String,
    expectedSha: String,
    questions: List<String>
): SubmissionResult {
    val baseUrl = projectUrl.trim().trimEnd('/')
    val requestUrl = Uri.parse("$baseUrl/functions/v1/$SUPABASE_IJTC_FUNCTION")
        .buildUpon()
        .appendQueryParameter("file", fileName)
        .build()
    val connection = try {
        URL(requestUrl.toString()).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The $displayName URL is invalid.")
    } catch (exception: IOException) {
        return SubmissionResult(false, "ইন্টারনেট সংযোগ নেই, চেক করুন!")
    }

    return try {
        connection.apply {
            requestMethod = "PUT"
            connectTimeout = 15_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("apikey", BuildConfig.RUBELAPP_PROXY_API_KEY)
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        val content = if (questions.isEmpty()) {
            ""
        } else {
            questions.joinToString(separator = "\n", postfix = "\n")
        }
        val requestBody = JSONObject()
            .put("content", content)
            .put("sha", expectedSha)
            .toString()
            .toByteArray(StandardCharsets.UTF_8)
        connection.outputStream.use { it.write(requestBody) }
        val statusCode = connection.responseCode
        val responseText = (if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        })?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (statusCode !in 200..299) {
            val message = try {
                JSONObject(responseText).optString("message").takeIf(String::isNotBlank)
            } catch (_: JSONException) {
                null
            }
            return SubmissionResult(
                false,
                message ?: "Could not save $displayName (HTTP $statusCode)."
            )
        }
        val response = JSONObject(responseText)
        SubmissionResult(
            true,
            response.optString("message", "$displayName saved."),
            fileSha = response.optString("sha")
        )
    } catch (exception: IOException) {
        SubmissionResult(false, "ইন্টারনেট সংযোগ নেই, চেক করুন!")
    } catch (exception: JSONException) {
        SubmissionResult(false, "The $displayName update response was invalid.")
    } finally {
        connection.disconnect()
    }
}

private fun uploadPrivateFileAttachment(
    projectUrl: String,
    anonKey: String,
    accessToken: String,
    fileId: String,
    attachment: FileAttachment
): SubmissionResult {
    val requestUrl = Uri.parse(
        "${projectUrl.trim().trimEnd('/')}/functions/v1/$SUPABASE_IJTC_FUNCTION"
    )    .buildUpon()
    .appendQueryParameter("file", "FileList")
    .appendQueryParameter("directory", "FileData")
        .appendQueryParameter("id", fileId)
        .appendQueryParameter("name", attachment.name)
        .build()
    val connection = try {
        URL(requestUrl.toString()).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The file upload URL is invalid.")
    } catch (exception: IOException) {
        return SubmissionResult(false, "ইন্টারনেট সংযোগ নেই, চেক করুন!")
    }

    return try {
        connection.apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 60_000
            doOutput = true
            setFixedLengthStreamingMode(attachment.bytes.size)
            setRequestProperty("apikey", BuildConfig.RUBELAPP_PROXY_API_KEY)
            setRequestProperty("Content-Type", attachment.contentType)
            setRequestProperty("Accept", "application/json")
        }
        connection.outputStream.use { it.write(attachment.bytes) }
        val statusCode = connection.responseCode
        val responseText = (if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        })?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (statusCode !in 200..299) {
            val message = try {
                JSONObject(responseText).optString("message").takeIf(String::isNotBlank)
            } catch (_: JSONException) {
                null
            }
            return SubmissionResult(
                false,
                fileUploadErrorMessage(statusCode, message.orEmpty())
            )
        }
        val response = JSONObject(responseText)
        val path = response.optString("path")
        if (path.isBlank()) {
            return SubmissionResult(false, "GitHub did not confirm the uploaded file path.")
        }
        SubmissionResult(
            true,
            response.optString("message", "File uploaded."),
            filePath = path
        )
    } catch (exception: IOException) {
        SubmissionResult(false, "Could not upload the selected file.")
    } catch (exception: JSONException) {
        SubmissionResult(false, "The file upload response was invalid.")
    } finally {
        connection.disconnect()
    }
}

private fun downloadPrivateFileAttachment(
    projectUrl: String,
    anonKey: String,
    accessToken: String,
    path: String
): Pair<FileAttachment?, String> {
    val attachmentPath = parseFileAttachmentPath(path)
        ?: return null to "The flData attachment path is invalid."
    val requestUrl = Uri.parse(
        "${projectUrl.trim().trimEnd('/')}/functions/v1/$SUPABASE_IJTC_FUNCTION"
    )    .buildUpon()
    .appendQueryParameter("file", "FileAttachment")
    .appendQueryParameter("directory", attachmentPath.directory)
        .appendQueryParameter("id", attachmentPath.fileId)
        .appendQueryParameter("name", attachmentPath.name)
        .build()
    val connection = try {
        URL(requestUrl.toString()).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return null to "The file download URL is invalid."
    } catch (exception: IOException) {
        return null to "ইন্টারনেট সংযোগ নেই, চেক করুন!"
    }

    return try {
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 45_000
            setRequestProperty("apikey", BuildConfig.RUBELAPP_PROXY_API_KEY)
            setRequestProperty("Accept", "application/octet-stream")
        }
        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            val responseText = connection.errorStream
                ?.bufferedReader(StandardCharsets.UTF_8)
                ?.use { it.readText() }
                .orEmpty()
            val message = try {
                JSONObject(responseText).optString("message")
            } catch (_: JSONException) {
                ""
            }
            return null to message.ifBlank {
                "Could not download the file (HTTP $statusCode)."
            }
        }
        val output = ByteArrayOutputStream()
        connection.inputStream.use { stream ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = stream.read(buffer)
                if (count < 0) break
                if (output.size() + count > MAX_FILE_ATTACHMENT_BYTES) {
                    return null to "Files must be 5 MiB or smaller."
                }
                output.write(buffer, 0, count)
            }
        }
        if (output.size() == 0) {
            return null to "The downloaded file is empty."
        }
        FileAttachment(
            attachmentPath.name,
            connection.contentType?.substringBefore(';') ?: "application/octet-stream",
            output.toByteArray()
        ) to ""
    } catch (exception: IOException) {
        null to "Could not download the file."
    } finally {
        connection.disconnect()
    }
}

private fun deletePrivateFileAttachment(
    projectUrl: String,
    anonKey: String,
    accessToken: String,
    path: String
): SubmissionResult {
    val attachmentPath = parseFileAttachmentPath(path)
        ?: return SubmissionResult(false, "The flData attachment path is invalid.")
    val requestUrl = Uri.parse(
        "${projectUrl.trim().trimEnd('/')}/functions/v1/$SUPABASE_IJTC_FUNCTION"
    )    .buildUpon()
    .appendQueryParameter("file", "FileAttachment")
    .appendQueryParameter("directory", attachmentPath.directory)
        .appendQueryParameter("id", attachmentPath.fileId)
        .appendQueryParameter("name", attachmentPath.name)
        .build()
    val connection = try {
        URL(requestUrl.toString()).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The file deletion URL is invalid.")
    } catch (exception: IOException) {
        return SubmissionResult(false, "ইন্টারনেট সংযোগ নেই, চেক করুন!")
    }

    return try {
        connection.apply {
            requestMethod = "DELETE"
            connectTimeout = 15_000
            readTimeout = 45_000
            setRequestProperty("apikey", BuildConfig.RUBELAPP_PROXY_API_KEY)
            setRequestProperty("Accept", "application/json")
        }
        val statusCode = connection.responseCode
        val responseText = (if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        })?.bufferedReader(StandardCharsets.UTF_8)?.use { it.readText() }.orEmpty()
        if (statusCode !in 200..299) {
            val message = try {
                JSONObject(responseText).optString("message").takeIf(String::isNotBlank)
            } catch (_: JSONException) {
                null
            }
            return SubmissionResult(
                false,
                message ?: "Could not delete the attachment from GitHub (HTTP $statusCode)."
            )
        }
        val response = JSONObject(responseText)
        SubmissionResult(
            true,
            response.optString("message", "Attachment deleted from GitHub.")
        )
    } catch (exception: IOException) {
        SubmissionResult(false, "Could not delete the attachment from GitHub.")
    } catch (exception: JSONException) {
        SubmissionResult(false, "The attachment deletion response was invalid.")
    } finally {
        connection.disconnect()
    }
}

private fun fetchQuestionListQuestions(): SubmissionResult =
    fetchPrivateGitHubQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "QuestionList",
        "QuestionList"
    )

private fun fetchOnTestQuestions(): SubmissionResult {
    val result = fetchPrivateGitHubQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "QuestionList",
        "OnTest"
    )
    return if (result.success) {
        result.copy(questions = firstOnTestQuestions(result.questions))
    } else {
        result
    }
}

private fun submitRegistration(registrationFields: List<String>): SubmissionResult {
    val registrationLine = buildUserListLine(registrationFields)
        ?: return SubmissionResult(
            false,
            "Fill in every registration field with valid values and do not use commas."
        )
    val userListResult = fetchPrivateGitHubQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "UserList",
        "UserList"
    )
    if (!userListResult.success) return userListResult
    if (userListResult.fileSha.isBlank()) {
        return SubmissionResult(false, "The UserList revision is missing. Please try again.")
    }

    val updatedLines = normalizeUserListLines(userListResult.fileLines) + registrationLine
    val saveResult = savePrivateQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "UserList",
        "UserList",
        userListResult.fileSha,
        serializeUserListLines(updatedLines)
    )
    return if (saveResult.success && saveResult.fileSha.isNotBlank()) {
        saveResult.copy(message = "Registration submitted and appended to UserList.")
    } else if (saveResult.success) {
        SubmissionResult(false, "The UserList save was not confirmed. Refresh before retrying.")
    } else {
        saveResult
    }
}

private fun submitUserListClaim(
    email: String,
    deviceId: String,
    claim: String
): SubmissionResult {
    val userListResult = fetchPrivateGitHubQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "UserList",
        "UserList"
    )
    if (!userListResult.success) return userListResult
    if (userListResult.fileSha.isBlank()) {
        return SubmissionResult(false, "The UserList revision is missing. Please try again.")
    }

    val updatedLines = updateUserListClaim(
        userListResult.fileLines,
        email,
        deviceId,
        claim
    ) ?: return SubmissionResult(
        false,
        "No UserList row matches this email and device ID."
    )
    val saveResult = savePrivateQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "UserList",
        "UserList",
        userListResult.fileSha,
        serializeUserListLines(updatedLines)
    )
    return if (saveResult.success && saveResult.fileSha.isNotBlank()) {
        saveResult.copy(message = "Claim saved to UserList.")
    } else if (saveResult.success) {
        SubmissionResult(false, "The UserList save was not confirmed. Refresh before retrying.")
    } else {
        saveResult
    }
}

private fun sendRecoveryCode(): SubmissionResult = SubmissionResult(
    false,
    "Login and registration are unavailable in this app."
)

private fun verifyLogin(email: String, deviceId: String): SubmissionResult {
    if (!isValidEmail(email)) {
        return SubmissionResult(false, "Enter a valid email address.")
    }
    if (deviceId.isBlank()) {
        return SubmissionResult(false, "Could not read this device ID.")
    }
    val userListResult = fetchPrivateGitHubQuestionFile(
        BuildConfig.RUBELAPP_PROXY_URL,
        BuildConfig.RUBELAPP_PROXY_API_KEY,
        "UserList",
        "UserList"
    )
    if (!userListResult.success) return userListResult
    return if (isUserLoginAllowed(userListResult.fileLines, email, deviceId)) {
        SubmissionResult(true, "Login successful.")
    } else {
        SubmissionResult(false, "Email or device is not registered or enabled.")
    }
}

private fun fetchWhatsappGroupLink(): SubmissionResult =
    SubmissionResult(false, "Account and WhatsApp data are unavailable in this app.")

private fun fetchGitHubAppUpdateInfo(): SubmissionResult {
    val connection = try {
        URL(GITHUB_UPDATE_MANIFEST_URL).openConnection() as HttpURLConnection
    } catch (exception: IllegalArgumentException) {
        return SubmissionResult(false, "The GitHub update manifest URL is invalid.")
    } catch (exception: IOException) {
        return SubmissionResult(false, exception.localizedMessage ?: "GitHub update check failed.")
    }

    return try {
        connection.apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
        }

        val statusCode = connection.responseCode
        if (statusCode !in 200..299) {
            return SubmissionResult(false, "GitHub update check failed (HTTP $statusCode).")
        }

        val contentLength = connection.getHeaderFieldLong("Content-Length", -1L)
        if (contentLength > 64 * 1024) {
            return SubmissionResult(false, "The GitHub update manifest is too large.")
        }
        val responseBytes = connection.inputStream.use { input ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4_096)
            var totalBytes = 0
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead < 0) break
                totalBytes += bytesRead
                if (totalBytes > 64 * 1024) {
                    throw IOException("The GitHub update manifest is too large.")
                }
                output.write(buffer, 0, bytesRead)
            }
            output.toByteArray()
        }
        val responseText = String(responseBytes, StandardCharsets.UTF_8)
        if (responseText.isBlank()) {
            return SubmissionResult(false, "The GitHub update manifest is empty.")
        }

        val manifest = JSONObject(responseText)
        val versionCode = manifest.optLong("versionCode")
        val versionName = manifest.optString("versionName").trim()
        val apkUrl = manifest.optString("apkUrl").trim()
        val apkFileName = manifest.optString("apkFileName").trim()
        val sha256 = manifest.optString("sha256").trim().lowercase(Locale.ROOT)
        if (versionCode < 1 || versionName.isBlank() ||
            apkUrl != GITHUB_UPDATE_APK_URL || apkFileName != "app-debug.apk" ||
            !sha256.matches(Regex("^[a-f0-9]{64}$"))
        ) {
            return SubmissionResult(false, "The GitHub update manifest is invalid.")
        }

        SubmissionResult(
            success = true,
            message = "GitHub update information loaded.",
            appUpdate = AppUpdateRelease(
                versionCode = versionCode,
                versionName = versionName,
                apkUrl = apkUrl,
                apkFileName = apkFileName,
                sha256 = sha256
            )
        )
    } catch (exception: IOException) {
        SubmissionResult(false, exception.localizedMessage ?: "GitHub update check failed.")
    } catch (exception: JSONException) {
        SubmissionResult(false, "Could not read the GitHub update manifest.")
    } finally {
        connection.disconnect()
    }
}

private suspend fun downloadAndValidateAppUpdate(
    context: android.content.Context,
    release: AppUpdateRelease
): File = withTimeout(15 * 60_000L) {
    val fileName = "exam-update-${release.versionCode}.apk"
    val outputDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        ?: throw IOException("App-specific download storage is unavailable.")
    val apkFile = File(outputDirectory, fileName)
    if (apkFile.exists() && !apkFile.delete()) {
        throw IOException("Could not replace the previously downloaded update.")
    }

    val downloadManager = context.getSystemService(DownloadManager::class.java)
        ?: throw IOException("Android download service is unavailable.")
    val request = DownloadManager.Request(Uri.parse(release.apkUrl))
        .setTitle("Exam update ${release.versionName}")
        .setDescription("Downloading the latest Exam app update")
        .setMimeType("application/vnd.android.package-archive")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(false)
        .setDestinationInExternalFilesDir(
            context,
            Environment.DIRECTORY_DOWNLOADS,
            fileName
        )
    val downloadId = downloadManager.enqueue(request)

    withTimeout(15 * 60_000L) {
        while (true) {
            currentCoroutineContext().ensureActive()
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            val status: Int
            val reason: Int
            cursor.use {
                if (!it.moveToFirst()) {
                    throw IOException("The APK download was removed from Android downloads.")
                }
                status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
            }
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> break
                DownloadManager.STATUS_FAILED -> throw IOException(
                    "APK download failed (error $reason). Check that the GitHub update file is available."
                )
            }
            delay(1_000)
        }
    }

    if (!apkFile.isFile || apkFile.length() == 0L) {
        throw IOException("The downloaded update APK is empty.")
    }
    if (release.sha256.isNotBlank()) {
        if (!release.sha256.matches(Regex("^[a-f0-9]{64}$"))) {
            throw IOException("The update manifest contains an invalid SHA-256 checksum.")
        }
        val actualSha256 = apkFile.inputStream().buffered().use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead < 0) break
                digest.update(buffer, 0, bytesRead)
            }
            digest.digest().joinToString("") { byte -> "%02x".format(byte) }
        }
        if (actualSha256 != release.sha256) {
            throw IOException("The downloaded APK failed its SHA-256 checksum.")
        }
    }

    val archiveInfo = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        ?: throw IOException("The downloaded file is not a valid Android APK.")
    val archiveVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        archiveInfo.longVersionCode
    } else {
        @Suppress("DEPRECATION")
        archiveInfo.versionCode.toLong()
    }
    if (archiveInfo.packageName != context.packageName ||
        archiveVersionCode != release.versionCode ||
        archiveInfo.versionName != release.versionName
    ) {
        throw IOException("The downloaded APK does not match the app update manifest.")
    }
    apkFile
}

private fun installDownloadedUpdate(
    context: android.content.Context,
    apkFile: File
) {
    val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        apkFile
    )
    val installIntent = android.content.Intent(
        android.content.Intent.ACTION_VIEW
    ).apply {
        setDataAndType(contentUri, "application/vnd.android.package-archive")
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(installIntent)
}

@Composable
private fun AppScreen() {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf("login") }
    var selectedEnterpriseFile by remember { mutableStateOf(enterpriseDataFiles.first()) }
    var contactFileReturnSelection by remember { mutableStateOf<Pair<String, String>?>(null) }
    var pendingContactFileId by remember { mutableStateOf<String?>(null) }
    var pendingItemSearchId by remember { mutableStateOf<String?>(null) }
    var contactFileReturnPage by remember { mutableStateOf<String?>(null) }
    var clearContactSearchOnReturn by remember { mutableStateOf(false) }
    val hasInternet = rememberInternetAvailability()
    val latestInternetState = rememberUpdatedState(hasInternet)
    var updateCheckStarted by remember { mutableStateOf(false) }
    var updateReady by remember { mutableStateOf<AppUpdateRelease?>(null) }
    var downloadedUpdate by remember { mutableStateOf<File?>(null) }
    var updateError by remember { mutableStateOf<String?>(null) }
    var showUpdatePrompt by remember { mutableStateOf(false) }
    var paymentUrl by remember { mutableStateOf("") }
    var examName by remember {
        mutableStateOf(
            context.getSharedPreferences(
                PREFERENCES_NAME,
                android.content.Context.MODE_PRIVATE
            ).getString(LAST_EXAM_NAME_KEY, null)
                ?.takeIf(String::isNotBlank)
                ?: if (currentPage == "exam") "QuestionList" else ""
        )
    }
    var examQuestions by remember { mutableStateOf(emptyList<ExamQuestion>()) }
    var isLoadingQuestionListQuestions by remember { mutableStateOf(false) }
    var questionListQuestionsError by remember { mutableStateOf<String?>(null) }
    var onTestQuestions by remember(context) {
        mutableStateOf(loadCachedOnTestQuestions(context))
    }
    var isLoadingOnTestQuestions by remember { mutableStateOf(false) }
    var onTestQuestionsError by remember { mutableStateOf<String?>(null) }
    var openOnTestWhenLoaded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val apkFile = downloadedUpdate
        if (apkFile != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            context.packageManager.canRequestPackageInstalls()
        ) {
            try {
                installDownloadedUpdate(context, apkFile)
            } catch (exception: android.content.ActivityNotFoundException) {
                updateError = "No Android package installer is available."
            } catch (exception: SecurityException) {
                updateError = "Android did not allow the update installer to open."
            }
        } else if (apkFile != null) {
            updateError = "Allow this app to install unknown apps, then check for updates again."
        }
    }

    LaunchedEffect(currentPage) {
        context.getSharedPreferences(
            PREFERENCES_NAME,
            android.content.Context.MODE_PRIVATE
        ).edit()
            .putString(
                LAST_APP_PAGE_KEY,
                currentPage.takeUnless {
                    it == "questionManagement" || it == "entryList" ||
                        it == "enterpriseFiles" || it == "enterpriseFileEditor"
                } ?: "control"
            )
            .apply()
    }

    LaunchedEffect(Unit) {
        if (updateCheckStarted) {
            return@LaunchedEffect
        }
        withFrameNanos { }
        snapshotFlow { latestInternetState.value }.first { it }
        updateCheckStarted = true
        val result = withContext(Dispatchers.IO) {
            fetchGitHubAppUpdateInfo()
        }
        if (!result.success) {
            Log.w("AppUpdate", result.message)
            return@LaunchedEffect
        }

        val release = result.appUpdate ?: return@LaunchedEffect
        val installedPackageInfo = context.packageManager
            .getPackageInfo(context.packageName, 0)
        val installedVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            installedPackageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            installedPackageInfo.versionCode.toLong()
        }
        if (!isAppUpdateAvailable(
                installedVersionCode = installedVersionCode,
                installedVersionName = installedPackageInfo.versionName.orEmpty(),
                releaseVersionCode = release.versionCode,
                releaseVersionName = release.versionName
            )
        ) {
            return@LaunchedEffect
        }

        updateReady = release
        try {
            downloadedUpdate = downloadAndValidateAppUpdate(context, release)
            showUpdatePrompt = true
        } catch (exception: IOException) {
            Log.e("AppUpdate", "Could not download or validate app update.", exception)
            updateError = exception.localizedMessage ?: "Could not download the app update."
        } catch (exception: SecurityException) {
            Log.e("AppUpdate", "Android denied the app update download.", exception)
            updateError = "Android denied the app update download."
        } finally {
            if (downloadedUpdate == null) {
                updateReady = null
            }
        }
    }

    fun loadQuestionListQuestions() {
        if (isLoadingQuestionListQuestions) return
        scope.launch {
            isLoadingQuestionListQuestions = true
            questionListQuestionsError = null
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchQuestionListQuestions()
                }
                if (result.success) {
                    examQuestions = result.questions
                } else {
                    questionListQuestionsError = result.message
                }
            } finally {
                isLoadingQuestionListQuestions = false
            }
        }
    }

    LaunchedEffect(currentPage, examName) {
        if (currentPage == "exam") {
            loadQuestionListQuestions()
        }
    }

    fun loadOnTestQuestions() {
        if (isLoadingOnTestQuestions) return
        scope.launch {
            isLoadingOnTestQuestions = true
            onTestQuestionsError = null
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchOnTestQuestions()
                }
                if (result.success) {
                    onTestQuestions = result.questions
                    saveCachedOnTestQuestions(context, result.questions)
                    if (openOnTestWhenLoaded) {
                        openOnTestWhenLoaded = false
                        currentPage = "onTest"
                    }
                } else {
                    openOnTestWhenLoaded = false
                    onTestQuestionsError = result.message
                }
            } finally {
                isLoadingOnTestQuestions = false
            }
        }
    }

    fun returnFromContactFileList() {
        val returnSelection = contactFileReturnSelection
        val returnPage = contactFileReturnPage
        if (returnSelection == null || returnPage == null) {
            currentPage = "enterpriseFiles"
        } else {
            selectedEnterpriseFile = returnSelection
            currentPage = returnPage
            contactFileReturnSelection = null
            pendingContactFileId = null
            pendingItemSearchId = null
            contactFileReturnPage = null
            clearContactSearchOnReturn =
                returnSelection.first == "ContactList" ||
                    returnSelection.first == "ProductList"
        }
    }

    BackHandler(enabled = currentPage != "payment" && currentPage != "login") {
        when (currentPage) {
            "registration" -> currentPage = "login"
            "dashboard" -> currentPage = "login"
            "control" -> currentPage = "dashboard"
            "questionManagement" -> currentPage = "control"
            "entryList" -> currentPage = "enterpriseFiles"
            "enterpriseFiles" -> currentPage = "control"
            "enterpriseFileEditor" -> returnFromContactFileList()
            "exam" -> {
                currentPage = "dashboard"
            }
            "onTest" -> currentPage = "login"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentPage) {
            "login" -> LoginScreen(
                deviceId = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ).orEmpty(),
                hasInternet = hasInternet,
                onLoginSuccess = {
                    currentPage = "dashboard"
                },
                onPaymentRequired = { url ->
                    paymentUrl = url
                    currentPage = "payment"
                },
                onOpenOnTest = {
                    currentPage = "onTest"
                    if (hasInternet) loadOnTestQuestions()
                },
                onRegister = {
                    currentPage = "registration"
                }
            )
            "registration" -> RegistrationScreen(
                onBackToLogin = { currentPage = "login" }
            )
            "dashboard" -> DashboardScreen(
                examName = examName,
                onOpenExam = { currentPage = "exam" },
                onOpenUser = { currentPage = "control" },
                onBackToLogin = { currentPage = "login" },
                onLogout = { currentPage = "login" }
            )
            "control" -> ControlScreen(
                onBackToDashboard = { currentPage = "dashboard" },
                onOpenWorkList = { currentPage = "enterpriseFiles" },
                onOpenUserList = {
                    selectedEnterpriseFile =
                        enterpriseDataFiles.first { it.first == "UserList" }
                    currentPage = "enterpriseFileEditor"
                },
                onOpenQuestions = { currentPage = "questionManagement" }
            )
            "questionManagement" -> QuestionListQuestionManagementScreen(
                onBack = { currentPage = "control" }
            )
            "entryList" -> QuestionListQuestionManagementScreen(
                onBack = ::returnFromContactFileList,
                fileName = "EntryList",
                displayName = "Entry List",
                isEntryList = true,
                onOpenFileListForContact = { entryId ->
                    contactFileReturnSelection = "EntryList" to "Entry List"
                    contactFileReturnPage = "entryList"
                    pendingContactFileId = entryId
                    selectedEnterpriseFile =
                        enterpriseDataFiles.first { it.first == "FileList" }
                    currentPage = "enterpriseFileEditor"
                },
                onOpenItemListForEntry = { entryId ->
                    contactFileReturnSelection = "EntryList" to "Entry List"
                    contactFileReturnPage = "entryList"
                    pendingItemSearchId = entryId
                    selectedEnterpriseFile =
                        enterpriseDataFiles.first { it.first == "ItemList" }
                    currentPage = "enterpriseFileEditor"
                }
            )
            "enterpriseFiles" -> EnterpriseFilesScreen(
                onBack = { currentPage = "control" },
                onOpenFile = { file ->
                    selectedEnterpriseFile = file
                    currentPage = if (file.first == "EntryList") {
                        "entryList"
                    } else {
                        "enterpriseFileEditor"
                    }
                }
            )
            "enterpriseFileEditor" -> QuestionListQuestionManagementScreen(
                onBack = ::returnFromContactFileList,
                fileName = selectedEnterpriseFile.first,
                displayName = selectedEnterpriseFile.second,
                isRawLineList = true,
                contactFileId = pendingContactFileId,
                itemSearchId = pendingItemSearchId,
                clearContactSearchOnReturn = clearContactSearchOnReturn,
                onOpenFileListForContact = { contactId ->
                    contactFileReturnSelection = selectedEnterpriseFile
                    contactFileReturnPage = "enterpriseFileEditor"
                    pendingContactFileId = contactId
                    selectedEnterpriseFile =
                        enterpriseDataFiles.first { it.first == "FileList" }
                },
                onContactSearchReset = {
                    clearContactSearchOnReturn = false
                }
            )
            "exam" -> ExamScreen(
                examName = examName,
                questions = examQuestions,
                onBackToDashboard = { currentPage = "dashboard" },
                onReloadQuestions = ::loadQuestionListQuestions
            )
            "onTest" -> ExamScreen(
                examName = examName,
                questions = onTestQuestions,
                onBackToDashboard = { currentPage = "login" },
                onReloadQuestions = ::loadOnTestQuestions,
                pageTitle = "OnTest Form",
                stateKey = "on_test",
                backContentDescription = "Back to login"
            )
            "payment" -> PaymentScreen(
                paymentUrl = paymentUrl,
                onBackToLogin = { currentPage = "login" }
            )
            else -> DashboardScreen(
                examName = examName,
                onOpenExam = { currentPage = "exam" },
                onOpenUser = { currentPage = "control" },
                onBackToLogin = { currentPage = "login" },
                onLogout = { currentPage = "login" }
            )
        }
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.ic_launcher_logo_foreground),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(1f)
                .align(Alignment.Center)
                .alpha(0.05f)
        )
        if (currentPage == "exam" && isLoadingQuestionListQuestions) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Loading...") },
                text = { Text("Please wait.") },
                confirmButton = {}
            )
        }
        if (isLoadingOnTestQuestions) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("") },
                text = { Text("Loading data, Please wait.") },
                confirmButton = {}
            )
        }
        onTestQuestionsError?.let { message ->
            AlertDialog(
                onDismissRequest = {
                    onTestQuestionsError = null
                },
                text = { Text(message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onTestQuestionsError = null
                            if (hasInternet) {
                                openOnTestWhenLoaded = true
                                loadOnTestQuestions()
                            } else {
                                onTestQuestionsError = "ইন্টারনেট সংযোগ নেই, চেক করুন!"
                            }
                        }
                    ) {
                        Text("Retry")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onTestQuestionsError = null
                            openOnTestWhenLoaded = false
                        }
                    ) {
                        Text("OK")
                    }
                }
            )
        }
        if (currentPage == "exam") {
            questionListQuestionsError?.let { message ->
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Could not load QuestionList questions") },
                    text = { Text(message) },
                    confirmButton = {
                        TextButton(onClick = ::loadQuestionListQuestions) {
                            Text("Retry")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                questionListQuestionsError = null
                                currentPage = "dashboard"
                            }
                        ) {
                            Text("Back to Dashboard")
                        }
                    }
                )
            }
        }
        if (showUpdatePrompt && updateReady != null && downloadedUpdate != null) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Update ready") },
                text = {
                    Text(
                        "Exam ${updateReady?.versionName} has been downloaded. Install the update to continue."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val apkFile = downloadedUpdate ?: return@TextButton
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                !context.packageManager.canRequestPackageInstalls()
                            ) {
                                val settingsIntent = android.content.Intent(
                                    android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:${context.packageName}")
                                )
                                installPermissionLauncher.launch(settingsIntent)
                            } else {
                                try {
                                    installDownloadedUpdate(context, apkFile)
                                } catch (exception: android.content.ActivityNotFoundException) {
                                    updateError = "No Android package installer is available."
                                } catch (exception: SecurityException) {
                                    updateError = "Android did not allow the update installer to open."
                                }
                            }
                        }
                    ) {
                        Text("Install")
                    }
                }
            )
        }
        updateError?.let { message ->
            AlertDialog(
                onDismissRequest = { updateError = null },
                title = { Text("Update problem") },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { updateError = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun LoginScreen(
    deviceId: String,
    hasInternet: Boolean,
    onLoginSuccess: (SubmissionResult) -> Unit,
    onPaymentRequired: (String) -> Unit,
    onOpenOnTest: () -> Unit,
    onRegister: () -> Unit
) {
    val context = LocalContext.current
    var email by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
                .getString(SAVED_LOGIN_EMAIL_KEY, "")
                .orEmpty()
        )
    }
    var isSubmitting by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showForgotLoginDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf(email) }
    var forgotClaim by remember { mutableStateOf("") }
    var isRequestingCode by remember { mutableStateOf(false) }
    var isOpeningWhatsapp by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scale = rememberResponsiveScale()
    val emailAccountPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (selectedEmail.isNullOrBlank()) {
                scope.launch {
                    snackbarHostState.showSnackbar("Could not read the selected email account.")
                }
            } else {
                email = selectedEmail.trim()
                context.getSharedPreferences(
                    PREFERENCES_NAME,
                    android.content.Context.MODE_PRIVATE
                ).edit().putString(SAVED_LOGIN_EMAIL_KEY, email).apply()
            }
        }
    }
    val forgotEmailAccountPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (selectedEmail.isNullOrBlank()) {
                scope.launch {
                    snackbarHostState.showSnackbar("Could not read the selected email account.")
                }
            } else {
                forgotEmail = selectedEmail.trim()
            }
        }
    }

    fun chooseEmailAccount() {
        try {
            emailAccountPicker.launch(
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    false,
                    "Choose an email account",
                    null,
                    null,
                    null
                )
            )
        } catch (exception: android.content.ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "No email account picker is available. Enter the email manually."
                )
            }
        }
    }

    fun chooseForgotEmailAccount() {
        try {
            forgotEmailAccountPicker.launch(
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    false,
                    "Choose an email account",
                    null,
                    null,
                    null
                )
            )
        } catch (exception: android.content.ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "No email account picker is available. Enter the email manually."
                )
            }
        }
    }

    fun handleLoginResult(result: SubmissionResult) {
        when {
            result.success -> onLoginSuccess(result)
            result.requiresPayment -> onPaymentRequired(result.paymentUrl)
            result.networkUnavailable -> showNoInternetDialog = true
            else -> scope.launch { snackbarHostState.showSnackbar(result.message) }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            Text(
                text = "Login Form",
                color = titleBlue,
                fontSize = (32 * scale).sp,
                lineHeight = (38 * scale).sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (28 * scale).dp, vertical = (16 * scale).dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = (28 * scale).dp, vertical = (20 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height((48 * scale).dp))
                LoginField(
                    value = email,
                    placeholder = "EMail",
                    onValueChange = { value ->
                        email = value
                        context.getSharedPreferences(
                            PREFERENCES_NAME,
                            android.content.Context.MODE_PRIVATE
                        ).edit().putString(SAVED_LOGIN_EMAIL_KEY, value).apply()
                    },
                    readOnly = isSubmitting,
                    keyboardType = KeyboardType.Email,
                    autofillEmail = true,
                    trailingPicker = true,
                    onPickTrailing = ::chooseEmailAccount,
                    modifier = Modifier.fillMaxWidth(0.76f)
                )
                Spacer(Modifier.height((20 * scale).dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ActionButton(
                        text = if (isSubmitting) "Logging..." else "Login",
                        color = submitGreen,
                        modifier = Modifier.fillMaxWidth(0.92f),
                        enabled = !isSubmitting,
                        compact = true,
                    ) {
                        if (!hasInternet) {
                            showNoInternetDialog = true
                        } else if (email.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Enter your EMail to continue.")
                            }
                        } else {
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        verifyLogin(email, deviceId)
                                    }
                                    handleLoginResult(result)
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height((10 * scale).dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
                ) {
                    ActionButton(
                        text = "Register",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        enabled = hasInternet,
                        compact = true,
                        onClick = onRegister
                    )
                    ActionButton(
                        text = "Forgot",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        compact = true
                    ) {
                        forgotEmail = email
                        forgotClaim = ""
                        showForgotLoginDialog = true
                    }
                }
                Spacer(Modifier.height((12 * scale).dp))
                ActionButton(
                    text = "100 Questions",
                    color = submitGreen,
                    modifier = Modifier.fillMaxWidth(0.92f),
                    compact = true,
                    onClick = onOpenOnTest
                )
                Spacer(Modifier.height((12 * scale).dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
                ) {
                    ActionButton(
                        text = "Claim",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        enabled = hasInternet,
                        compact = true
                    ) {
                        try {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(TELEGRAM_CLAIM_URL)
                                )
                            )
                        } catch (exception: android.content.ActivityNotFoundException) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No app or browser is available to open the Telegram link."
                                )
                            }
                        }
                    }
                    ActionButton(
                        text = if (isOpeningWhatsapp) "Opening..." else "Group",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        enabled = hasInternet && !isOpeningWhatsapp,
                        compact = true,
                        leadingIcon = painterResource(R.drawable.ic_whatsapp)
                    ) {
                        if (deviceId.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Could not read this device ID.")
                            }
                        } else {
                            scope.launch {
                                isOpeningWhatsapp = true
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        fetchWhatsappGroupLink()
                                    }
                                    if (!result.success) {
                                        snackbarHostState.showSnackbar(result.message)
                                    } else {
                                        val groupUri = Uri.parse(result.whatsappUrl)
                                        if (groupUri.scheme !in setOf("http", "https") ||
                                            groupUri.host.isNullOrBlank()
                                        ) {
                                            snackbarHostState.showSnackbar(
                                                "The WhatsApp group link is invalid."
                                            )
                                        } else {
                                            try {
                                                context.startActivity(
                                                    android.content.Intent(
                                                        android.content.Intent.ACTION_VIEW,
                                                        groupUri
                                                    )
                                                )
                                            } catch (exception: android.content.ActivityNotFoundException) {
                                                snackbarHostState.showSnackbar(
                                                    "No app is available to open this WhatsApp link."
                                                )
                                            }
                                        }
                                    }
                                } finally {
                                    isOpeningWhatsapp = false
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height((12 * scale).dp))
                Row(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
                ) {
                    ActionButton(
                        text = "Share",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        compact = true
                    ) {
                        val shareIntent = android.content.Intent(
                            android.content.Intent.ACTION_SEND
                        ).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, SHARE_APP_URL)
                        }
                        try {
                            context.startActivity(
                                android.content.Intent.createChooser(shareIntent, "Share")
                            )
                        } catch (exception: android.content.ActivityNotFoundException) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No app is available to share this link."
                                )
                            }
                        }
                    }
                    ActionButton(
                        text = "Website",
                        color = submitGreen,
                        modifier = Modifier.weight(1f),
                        compact = true
                    ) {
                        try {
                            context.startActivity(
                                android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    Uri.parse(SHARE_APP_URL)
                                )
                            )
                        } catch (exception: android.content.ActivityNotFoundException) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "No external browser is available to open this website."
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (28 * scale).dp, vertical = (10 * scale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME} (VC ${BuildConfig.VERSION_CODE})",
                    color = Color.Gray,
                    fontSize = (12 * scale).sp,
                    maxLines = 1
                )
            }
        }
    }
    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            text = {
                Text(
                    text = "ইন্টারনেট সংযোগ নেই, চেক করুন!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { showNoInternetDialog = false }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
    if (showForgotLoginDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isRequestingCode) showForgotLoginDialog = false
            },
            title = { Text("Forgot Login", color = Color.Black) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginField(
                        value = forgotEmail,
                        placeholder = "EMail",
                        onValueChange = { forgotEmail = it },
                        readOnly = isRequestingCode,
                        keyboardType = KeyboardType.Email,
                        autofillEmail = true,
                        trailingPicker = true,
                        onPickTrailing = ::chooseForgotEmailAccount,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height((8 * scale).dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        BasicTextField(
                            value = forgotClaim,
                            onValueChange = { forgotClaim = it },
                            readOnly = isRequestingCode,
                            singleLine = false,
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = (15 * scale).sp
                            ),
                            modifier = Modifier
                                .padding(top = (8 * scale).dp)
                                .fillMaxWidth()
                                .heightIn(min = (96 * scale).dp)
                                .background(Color.White, RoundedCornerShape((8 * scale).dp))
                                .border(
                                    BorderStroke((1.5 * scale).dp, Color.Black),
                                    RoundedCornerShape((8 * scale).dp)
                                )
                                .padding((12 * scale).dp)
                                .semantics { contentDescription = "usClaim" }
                        )
                        Text(
                            text = "usClaim",
                            color = Color.Black,
                            fontSize = (15 * scale).sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .background(Color.White)
                                .padding(horizontal = (4 * scale).dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isRequestingCode && hasInternet,
                    onClick = {
                        when {
                            !isValidEmail(forgotEmail) -> scope.launch {
                                snackbarHostState.showSnackbar("Enter a valid email address.")
                            }
                            deviceId.isBlank() -> scope.launch {
                                snackbarHostState.showSnackbar("Could not read this device ID.")
                            }
                            forgotClaim.isBlank() -> scope.launch {
                                snackbarHostState.showSnackbar("Enter usClaim.")
                            }
                            else -> scope.launch {
                                isRequestingCode = true
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        submitUserListClaim(
                                            forgotEmail,
                                            deviceId,
                                            forgotClaim
                                        )
                                    }
                                    if (result.success) {
                                        showForgotLoginDialog = false
                                    }
                                    snackbarHostState.showSnackbar(result.message)
                                } finally {
                                    isRequestingCode = false
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        if (isRequestingCode) "Submitting..." else "Submit",
                        color = titleBlue
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isRequestingCode,
                    onClick = { showForgotLoginDialog = false }
                ) {
                    Text("Cancel", color = titleBlue)
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black
        )
    }
    CenterNoticeHost(snackbarHostState)
}

@Composable
private fun PaymentScreen(paymentUrl: String, onBackToLogin: () -> Unit) {
    val scale = rememberResponsiveScale()
    val webViewState = remember { mutableStateOf<WebView?>(null) }
    var loadError by remember(paymentUrl) { mutableStateOf<String?>(null) }
    val parsedUri = Uri.parse(paymentUrl)
    val validPaymentUrl = parsedUri.scheme in setOf("http", "https") &&
        !parsedUri.host.isNullOrBlank()

    BackHandler {
        val webView = webViewState.value
        if (webView?.canGoBack() == true) {
            webView.goBack()
        } else {
            onBackToLogin()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewState.value?.apply {
                stopLoading()
                destroy()
            }
            webViewState.value = null
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = (16 * scale).dp, vertical = (8 * scale).dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Payment",
                    color = titleBlue,
                    fontSize = (22 * scale).sp
                )
                TextButton(onClick = onBackToLogin) {
                    Text("Back to Login")
                }
            }
            when {
                paymentUrl.isBlank() -> Text(
                    text = "Payment link is not configured for this account.",
                    modifier = Modifier.padding((20 * scale).dp)
                )
                !validPaymentUrl -> Text(
                    text = "The payment link in User column K is not a valid HTTP or HTTPS URL.",
                    modifier = Modifier.padding((20 * scale).dp)
                )
                loadError != null -> Text(
                    text = loadError.orEmpty(),
                    modifier = Modifier.padding((20 * scale).dp)
                )
                else -> AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(
                                    view: WebView,
                                    request: android.webkit.WebResourceRequest,
                                    error: android.webkit.WebResourceError
                                ) {
                                    if (request.isForMainFrame) {
                                        loadError = "Could not load the payment page. Check your internet connection and try again."
                                    }
                                }
                            }
                            webViewState.value = this
                            loadUrl(paymentUrl)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun LoginField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    autofillEmail: Boolean = false,
    trailingPicker: Boolean = false,
    onPickTrailing: () -> Unit = {},
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scale = rememberResponsiveScale()
    val heightScale = if (compact) 0.84f else 1f
    val fontScale = if (compact) scale.coerceAtLeast(0.9f) else scale

    Box(
        modifier = modifier
            .height((58 * scale * heightScale).dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height((50 * scale * heightScale).dp)
                .border(BorderStroke((1.5 * scale).dp, Color.Black), RoundedCornerShape(50))
                .padding(
                    start = ((if (compact) 10 else 18) * scale).dp,
                    end = ((if (compact) 10 else 18) * scale +
                        (if (trailingPicker) 42f * scale else 0f)).dp
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword) {
                    PasswordVisualTransformation()
                } else {
                    androidx.compose.ui.text.input.VisualTransformation.None
                },
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = ((if (compact) 12 else 14) * fontScale).sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = placeholder
                        if (autofillEmail) {
                            contentType = ContentType.EmailAddress
                        }
                    }
            )
        }
        if (trailingPicker) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((48 * scale).dp)
                    .clickable(onClick = onPickTrailing)
                    .semantics { contentDescription = "Choose email account" },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.Black, fontSize = (26 * scale).sp)
            }
        }
        Text(
            text = placeholder,
            color = Color.Black,
            fontSize = ((if (compact) 12 else 18) * fontScale).sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.White)
                .padding(horizontal = ((if (compact) 4 else 5) * scale).dp)
        )
    }
}

@Composable
private fun DashboardScreen(
    examName: String,
    onOpenExam: () -> Unit,
    onOpenUser: () -> Unit,
    onBackToLogin: () -> Unit,
    onLogout: () -> Unit
) {
    val scale = rememberResponsiveScale()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = (28 * scale).dp, vertical = (24 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((48 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .semantics { contentDescription = "Back to login" }
                ) {
                    Text("←", fontSize = (24 * scale).sp, color = titleBlue)
                }
                Text(
                    text = "Dashboard Form",
                    color = titleBlue,
                    fontSize = (30 * scale).sp,
                    lineHeight = (36 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height((28 * scale).dp))
            Text(
                text = examName.ifBlank { "Your exam" },
                color = titleBlue,
                fontSize = (20 * scale).sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height((24 * scale).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
            ) {
                ActionButton(
                    text = "Exam",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenExam
                )
                ActionButton(
                    text = "Admin",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onOpenUser
                )
            }
            Spacer(Modifier.height((12 * scale).dp))
            ActionButton(
                text = "Logout",
                color = submitGreen,
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogout
            )
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun ControlScreen(
    onBackToDashboard: () -> Unit,
    onOpenWorkList: () -> Unit,
    onOpenUserList: () -> Unit,
    onOpenQuestions: () -> Unit
) {
    val scale = rememberResponsiveScale()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var wirelessDebugSettingsError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = (28 * scale).dp, vertical = (24 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((48 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onBackToDashboard,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .semantics { contentDescription = "Back to dashboard" }
                ) {
                    Text("←", fontSize = (24 * scale).sp, color = titleBlue)
                }
                Text(
                    text = "Control Form",
                    color = titleBlue,
                    fontSize = (30 * scale).sp,
                    lineHeight = (36 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height((24 * scale).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
            ) {
                ActionButton(
                    text = "WDebug",
                    color = submitGreen,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = "Open wireless debugging settings" },
                    compact = true
                ) {
                    try {
                        context.startActivity(
                            android.content.Intent("android.settings.WIRELESS_DEBUGGING_SETTINGS")
                        )
                    } catch (_: android.content.ActivityNotFoundException) {
                        try {
                            context.startActivity(
                                android.content.Intent(
                                    Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS
                                ).putExtra(
                                    ":settings:show_fragment_args",
                                    Bundle().apply {
                                        putString(
                                            ":settings:fragment_args_key",
                                            "toggle_adb_wireless"
                                        )
                                    }
                                )
                            )
                        } catch (_: android.content.ActivityNotFoundException) {
                            wirelessDebugSettingsError =
                                "Wireless debugging settings are not available on this device."
                        } catch (_: SecurityException) {
                            wirelessDebugSettingsError =
                                "Android did not allow Developer options to open."
                        }
                    } catch (_: SecurityException) {
                        wirelessDebugSettingsError =
                            "Android did not allow Wireless debugging settings to open."
                    }
                }
                ActionButton(
                    text = "User",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    compact = true
                ) {
                    onOpenUserList()
                }
            }
            Spacer(Modifier.height((18 * scale).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((12 * scale).dp)
            ) {
                ActionButton(
                    text = "WorkList Form",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    compact = true
                ) {
                    onOpenWorkList()
                }
                ActionButton(
                    text = "Question",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    compact = true
                ) {
                    onOpenQuestions()
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
    wirelessDebugSettingsError?.let { message ->
        AlertDialog(
            onDismissRequest = { wirelessDebugSettingsError = null },
            title = { Text("Wireless debugging") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { wirelessDebugSettingsError = null }) {
                    Text("ঠিক আছে")
                }
            }
        )
    }
}

@Composable
private fun EnterpriseFilesScreen(
    onBack: () -> Unit,
    onOpenFile: (Pair<String, String>) -> Unit
) {
    val scale = rememberResponsiveScale()
    var showTypeListForm by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        if (showTypeListForm) {
            TypeListForm(
                onBack = { showTypeListForm = false },
                contentPadding = innerPadding
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = (20 * scale).dp, vertical = (16 * scale).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("←", color = titleBlue, fontSize = (24 * scale).sp)
                }
                Text(
                    text = "WorkList Form",
                    modifier = Modifier.weight(1f),
                    color = titleBlue,
                    fontSize = (22 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.width((48 * scale).dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy((8 * scale).dp)
            ) {
                items(
                    enterpriseDataFiles.filterNot {
                        it.first == "UserList" || it.first == "TypeList"
                    }.chunked(2)
                ) { files ->
                    Column(verticalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                        ) {
                            files.forEach { file ->
                                TextButton(
                                    onClick = { onOpenFile(file) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            BorderStroke((1 * scale).dp, Color.LightGray),
                                            RoundedCornerShape((8 * scale).dp)
                                        )
                                ) {
                                    Text(
                                        text = file.second,
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        fontSize = (16 * scale).sp
                                    )
                                }
                            }
                            if (files.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        if (files.any { it.first == "UnitList" }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        onOpenFile("UserList" to "User List")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            BorderStroke((1 * scale).dp, Color.LightGray),
                                            RoundedCornerShape((8 * scale).dp)
                                        )
                                ) {
                                    Text(
                                        text = "User List",
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        fontSize = (16 * scale).sp
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                            ) {
                                TextButton(
                                    onClick = { showTypeListForm = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            BorderStroke((1 * scale).dp, Color.LightGray),
                                            RoundedCornerShape((8 * scale).dp)
                                        )
                                ) {
                                    Text(
                                        text = "Type List",
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        fontSize = (16 * scale).sp
                                    )
                                }
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeListForm(
    onBack: () -> Unit,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scale = rememberResponsiveScale()
    val typeRecords = remember { mutableStateListOf<SetupTypeRecord>() }
    var fileSha by remember { mutableStateOf("") }
    var englishType by remember { mutableStateOf("") }
    var banglaType by remember { mutableStateOf("") }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var deletingIndex by remember { mutableStateOf<Int?>(null) }
    var draggingIndex by remember { mutableStateOf<Int?>(null) }
    val typeListState = rememberLazyListState()
    var isBanglaSelected by remember {
        mutableStateOf(
            context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
                .getBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, true)
        )
    }
    var isLoading by remember { mutableStateOf(true) }
    var isWorking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val isReadOnly = false
    suspend fun publicApiKey(): String? = BuildConfig.RUBELAPP_PROXY_API_KEY
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            fetchPrivateGitHubQuestionFile(
                BuildConfig.RUBELAPP_PROXY_URL,
                BuildConfig.RUBELAPP_PROXY_API_KEY,
                "TypeList",
                "Type List"
            )
        }
        if (result.success) {
            val parsed = result.fileLines.mapNotNull {
                parseTypeListRecord(it, isBanglaSelected)
            }
            if (parsed.size != result.fileLines.size) {
                errorMessage = "TypeList contains an invalid row."
            } else {
                val storedLanguage = typeListLanguageSelection(parsed)
                if (storedLanguage != null) {
                    isBanglaSelected = storedLanguage
                    context.getSharedPreferences(
                        PREFERENCES_NAME,
                        android.content.Context.MODE_PRIVATE
                    ).edit().putBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, storedLanguage).apply()
                }
                typeRecords.clear()
                typeRecords.addAll(typeListRecordsForLanguage(parsed, isBanglaSelected))
                fileSha = result.fileSha
            }
        } else {
            errorMessage = result.message
        }
        isLoading = false
    }

    fun clearEditor() {
        englishType = ""
        banglaType = ""
        editingIndex = null
        errorMessage = ""
    }

    fun persistRecords(
        updatedRecords: List<SetupTypeRecord>,
        successMessage: String,
        clearEditorOnSuccess: Boolean = true,
        onFailure: (() -> Unit)? = null
    ) {
        scope.launch {
            isWorking = true
            errorMessage = ""
            try {
                val writeToken = publicApiKey() ?: run {
                    onFailure?.invoke()
                    isWorking = false
                    return@launch
                }
                val result = withContext(Dispatchers.IO) {
                    savePrivateQuestionFile(
                        BuildConfig.RUBELAPP_PROXY_URL,
                        BuildConfig.RUBELAPP_PROXY_API_KEY,
                        writeToken,
                        "TypeList",
                        "Type List",
                        fileSha,
                        serializeTypeListRecords(updatedRecords)
                    )
                }
                if (result.success && result.fileSha.isNotBlank()) {
                    typeRecords.clear()
                    typeRecords.addAll(updatedRecords)
                    fileSha = result.fileSha
                    if (clearEditorOnSuccess) clearEditor()
                    errorMessage = successMessage
                } else {
                    errorMessage = result.message
                        .takeIf(String::isNotBlank)
                        ?: "Could not verify the saved TypeList. Refresh before editing."
                    onFailure?.invoke()
                }
            } finally {
                isWorking = false
            }
        }
    }

    fun selectTypeLanguage(selectBangla: Boolean) {
        if (selectBangla == isBanglaSelected || isWorking || isLoading) return
        val previousLanguage = isBanglaSelected
        isBanglaSelected = selectBangla
        context.getSharedPreferences(
            PREFERENCES_NAME,
            android.content.Context.MODE_PRIVATE
        ).edit().putBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, selectBangla).apply()
        persistRecords(
            typeListRecordsForLanguage(typeRecords, selectBangla),
            "stCType values updated.",
            clearEditorOnSuccess = false,
            onFailure = {
                isBanglaSelected = previousLanguage
                context.getSharedPreferences(
                    PREFERENCES_NAME,
                    android.content.Context.MODE_PRIVATE
                ).edit().putBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, previousLanguage).apply()
            }
        )
    }

    if (!isReadOnly) Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = (10 * scale).dp, vertical = (8 * scale).dp)
            .border(
                BorderStroke((1 * scale).dp, Color.Black),
                RoundedCornerShape((22 * scale).dp)
            )
            .padding(horizontal = (5 * scale).dp, vertical = (4 * scale).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (4 * scale).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("←", color = titleBlue, fontSize = (24 * scale).sp)
            }
            Text(
                text = "TypeList Form",
                modifier = Modifier.weight(1f),
                color = Color.Black,
                fontSize = (30 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width((48 * scale).dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke((1 * scale).dp, Color.Black),
                    RoundedCornerShape((20 * scale).dp)
                )
                .padding(horizontal = (14 * scale).dp, vertical = (8 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "stCType",
                color = Color.Black,
                fontSize = (23 * scale).sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height((8 * scale).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("English", color = Color.Black, fontSize = (17 * scale).sp)
                Spacer(Modifier.width((12 * scale).dp))
                Box(
                    modifier = Modifier
                        .size(width = (172 * scale).dp, height = (36 * scale).dp)
                        .border(BorderStroke((2 * scale).dp, Color.Black), CircleShape)
                        .toggleable(
                            value = isBanglaSelected,
                            role = Role.Switch,
                            enabled = !isWorking && !isLoading
                        ) { selectTypeLanguage(it) }
                        .padding((2 * scale).dp),
                    contentAlignment = if (isBanglaSelected) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size((30 * scale).dp)
                            .background(Color(0xFF68BE45), CircleShape)
                    )
                }
                Spacer(Modifier.width((12 * scale).dp))
                Text("বাংলা", color = Color.Black, fontSize = (17 * scale).sp)
            }
            Spacer(Modifier.height((18 * scale).dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)
            ) {
                SetupTypeField(
                    label = "stEType",
                    value = englishType,
                    onValueChange = { englishType = it },
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
                SetupTypeField(
                    label = "stBType",
                    value = banglaType,
                    onValueChange = { banglaType = it },
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
            }
            SetupTypeField(
                label = "stCType",
                value = when {
                    englishType.isBlank() || banglaType.isBlank() -> ""
                    isBanglaSelected -> banglaType
                    else -> englishType
                },
                onValueChange = {},
                scale = scale,
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = ::clearEditor,
                    enabled = !isWorking
                ) {
                    Text("Clear", color = Color.Black, fontSize = (16 * scale).sp)
                }
                TextButton(
                    onClick = {
                        val cleanEnglish = englishType.trim()
                        val cleanBangla = banglaType.trim()
                        val validationError = typeListValidationError(
                            cleanEnglish,
                            cleanBangla,
                            typeRecords,
                            editingIndex
                        )
                        if (validationError != null) {
                            errorMessage = validationError
                        } else {
                            val record = SetupTypeRecord(
                                english = cleanEnglish,
                                bangla = cleanBangla,
                                selectedType = if (isBanglaSelected) {
                                    cleanBangla
                                } else {
                                    cleanEnglish
                                }
                            )
                            val updated = typeRecords.toMutableList()
                            val editIndex = editingIndex
                            if (editIndex == null) {
                                updated.add(record)
                            } else {
                                updated[editIndex] = record
                            }
                            persistRecords(updated, "Setup List saved.")
                        }
                    },
                    enabled = !isWorking && !isLoading
                ) {
                    Text(
                        if (isWorking) "Saving..." else "Save",
                        color = Color.Black,
                        fontSize = (16 * scale).sp
                    )
                }
            }
        }
        if (errorMessage.isNotBlank()) {
            Text(
                text = errorMessage,
                color = if (errorMessage.endsWith("saved.") ||
                    errorMessage.endsWith("updated.")
                ) {
                    Color(0xFF2E7D32)
                } else {
                    Color.Red
                },
                fontSize = (13 * scale).sp,
                modifier = Modifier.padding(horizontal = (10 * scale).dp, vertical = (4 * scale).dp)
            )
        }
        if (isLoading) {
            Text(
                text = "Loading Setup List...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding((12 * scale).dp),
                textAlign = TextAlign.Center,
                fontSize = (14 * scale).sp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = (8 * scale).dp)
                .clip(RoundedCornerShape((18 * scale).dp))
                .border(
                    BorderStroke((2 * scale).dp, Color.Black),
                    RoundedCornerShape((18 * scale).dp)
                )
                .padding((10 * scale).dp)
        ) {
            Text(
                text = "Type List",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (6 * scale).dp),
                color = Color.Black,
                fontSize = (20 * scale).sp,
                textAlign = TextAlign.Center
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SetupTypeTableCell("SL", 0.45f, scale, header = true)
                SetupTypeTableCell("stEType", 1f, scale, header = true)
                SetupTypeTableCell("stBType", 1f, scale, header = true)
                SetupTypeTableCell("stCType", 1f, scale, header = true)
                if (!isReadOnly) {
                    SetupTypeTableCell("Actions", 1.15f, scale, header = true)
                }
            }
            LazyColumn(
                state = typeListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(items = typeRecords.toList(), key = { it.english }) { record ->
                    val index = typeRecords.indexOfFirst { it.english == record.english }
                    val currentRowIndex by rememberUpdatedState(index)
                    var originalRecords: List<SetupTypeRecord>? = null
                    var originalEditingType: String? = null
                    var draggedCenterY = 0f
                    val rowModifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (!isReadOnly && !isWorking && !isLoading) {
                                Modifier.pointerInput(record.english, isWorking, isLoading) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = {
                                            originalRecords = typeRecords.toList()
                                            originalEditingType = editingIndex
                                                ?.let { typeRecords.getOrNull(it)?.english }
                                            val item = typeListState.layoutInfo.visibleItemsInfo
                                                .firstOrNull { it.index == currentRowIndex }
                                            draggedCenterY = item?.let {
                                                it.offset + it.size / 2f
                                            } ?: 0f
                                            draggingIndex = currentRowIndex
                                        },
                                        onDragEnd = {
                                            val before = originalRecords
                                            val after = typeRecords.toList()
                                            draggingIndex = null
                                            originalRecords = null
                                            if (before != null && before != after) {
                                                editingIndex = originalEditingType?.let { type ->
                                                    after.indexOfFirst { it.english == type }
                                                        .takeIf { it >= 0 }
                                                }
                                                persistRecords(
                                                    after,
                                                    "Type order saved.",
                                                    clearEditorOnSuccess = false,
                                                    onFailure = {
                                                        typeRecords.clear()
                                                        typeRecords.addAll(before)
                                                        editingIndex = originalEditingType?.let { type ->
                                                            before.indexOfFirst {
                                                                it.english == type
                                                            }.takeIf { it >= 0 }
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        onDragCancel = {
                                            originalRecords?.let { before ->
                                                typeRecords.clear()
                                                typeRecords.addAll(before)
                                            }
                                            draggingIndex = null
                                            originalRecords = null
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            draggedCenterY += dragAmount.y
                                            val targetIndex = typeListState.layoutInfo
                                                .visibleItemsInfo
                                                .minByOrNull {
                                                    abs(
                                                        it.offset + it.size / 2f -
                                                            draggedCenterY
                                                    )
                                                }
                                                ?.index
                                                ?: return@detectDragGesturesAfterLongPress
                                            val fromIndex = draggingIndex
                                                ?: return@detectDragGesturesAfterLongPress
                                            val reordered = reorderTypeRecords(
                                                typeRecords,
                                                fromIndex,
                                                targetIndex
                                            )
                                            if (reordered != typeRecords) {
                                                typeRecords.clear()
                                                typeRecords.addAll(reordered)
                                                draggingIndex = targetIndex
                                            }
                                        }
                                    )
                                }
                            } else {
                                Modifier
                            }
                        )
                        .background(
                            if (draggingIndex == index) Color(0xFFE8F0FE) else Color.White
                        )
                    Row(
                        modifier = rowModifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SetupTypeTableCell(
                            (index + 1).toString(),
                            0.45f,
                            scale,
                            modifier = Modifier
                                .semantics { contentDescription = "Type row ${index + 1}" }
                        )
                        SetupTypeTableCell(record.english, 1f, scale)
                        SetupTypeTableCell(record.bangla, 1f, scale)
                        SetupTypeTableCell(record.selectedType, 1f, scale)
                        if (!isReadOnly) Column(
                            modifier = Modifier
                                .weight(1.15f)
                                .height((46 * scale).dp)
                                .border(BorderStroke((0.5f * scale).dp, Color.Black)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    deletingIndex = index
                                },
                                enabled = !isWorking,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Delete", color = Color.Black, fontSize = (11 * scale).sp)
                            }
                            TextButton(
                                onClick = {
                                    englishType = record.english
                                    banglaType = record.bangla
                                    editingIndex = index
                                    errorMessage = ""
                                },
                                enabled = !isWorking,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Edit", color = Color.Black, fontSize = (11 * scale).sp)
                            }
                        }
                    }
                }
            }
        }
    }

    deletingIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { if (!isWorking) deletingIndex = null },
            title = { Text("Delete type?") },
            text = { Text("Delete this type from Setup List?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = typeRecords.toMutableList().apply { removeAt(index) }
                        deletingIndex = null
                        editingIndex?.let { currentEditIndex ->
                            when {
                                currentEditIndex == index -> clearEditor()
                                currentEditIndex > index ->
                                    editingIndex = currentEditIndex - 1
                            }
                        }
                        persistRecords(updated, "Setup List updated.")
                    },
                    enabled = !isWorking
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RowScope.SetupTypeTableCell(
    text: String,
    weight: Float,
    scale: Float,
    header: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .weight(weight)
            .height((if (header) 38 * scale else 46 * scale).dp)
            .border(BorderStroke((0.5f * scale).dp, Color.Black))
            .padding(horizontal = (4 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.Black,
            fontSize = (if (header) 13 * scale else 14 * scale).sp,
            fontWeight = if (header) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SetupTypeField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    scale: Float,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height((62 * scale).dp)
            .padding(top = (8 * scale).dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .border(
                    BorderStroke((2 * scale).dp, Color.Black),
                    CircleShape
                )
                .padding(horizontal = (12 * scale).dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                readOnly = readOnly,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = (14 * scale).sp,
                    textAlign = TextAlign.Center
                ),
                singleLine = true
            )
        }
        Text(
            text = label,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-7 * scale).dp)
                .background(Color.White)
                .padding(horizontal = (4 * scale).dp),
            color = Color.Black,
            fontSize = (12 * scale).sp
        )
    }
}

@Composable
private fun QuestionListQuestionManagementScreen(
    onBack: () -> Unit,
    fileName: String = "QuestionList",
    displayName: String = "QuestionList",
    isEntryList: Boolean = false,
    isRawLineList: Boolean = false,
    contactFileId: String? = null,
    itemSearchId: String? = null,
    clearContactSearchOnReturn: Boolean = false,
    onOpenFileListForContact: (String) -> Unit = {},
    onOpenItemListForEntry: (String) -> Unit = {},
    onContactSearchReset: () -> Unit = {}
) {
    val isLineList = isEntryList || isRawLineList
    val isContactList = isRawLineList && fileName == "ContactList"
    val isBankList = isRawLineList && fileName == "BankList"
    val isFileList = isRawLineList && fileName == "FileList"
    val isProductList = isRawLineList && fileName == "ProductList"
    val isItemList = isRawLineList && fileName == "ItemList"
    val isUnitList = isRawLineList && fileName == "UnitList"
    val isUserList = isRawLineList && fileName == "UserList"
    val isReadOnly = fileName !in publicWritableFiles
    val scope = rememberCoroutineScope()
    val scale = rememberResponsiveScale()
    val context = LocalContext.current
    var isBanglaTypeSelected by remember(context) {
        mutableStateOf(
            context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
                .getBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, true)
        )
    }
    var questions by remember { mutableStateOf(emptyList<String>()) }
    var fileSha by remember { mutableStateOf("") }
    var isWorking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }
    var showEditor by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }
    var entryFields by remember { mutableStateOf(List(18) { "" }) }
    var entryTypeRecords by remember { mutableStateOf(emptyList<SetupTypeRecord>()) }
    var entryTypesLoaded by remember { mutableStateOf(false) }
    var entryTypeLoadError by remember { mutableStateOf("") }
    var entryContactNames by remember { mutableStateOf(emptyList<String>()) }
    var entryContactsLoaded by remember { mutableStateOf(false) }
    var entryContactLoadError by remember { mutableStateOf("") }
    var contactListLines by remember { mutableStateOf(emptyList<String>()) }
    var contactListSha by remember { mutableStateOf("") }
    var entryItemLines by remember { mutableStateOf(emptyList<String>()) }
    var entryItemSha by remember { mutableStateOf("") }
    var entryItemLoadError by remember { mutableStateOf("") }
    var entryTrackError by remember { mutableStateOf("") }
    var isLocatingEntryTrack by remember { mutableStateOf(false) }
    var hasEntryLocationPermission by remember { mutableStateOf(false) }
    var contactFields by remember {
        mutableStateOf(List(contactListFieldLabels.size) { "" })
    }
    var bankFields by remember { mutableStateOf(List(bankListFieldLabels.size) { "" }) }
    var productFields by remember {
        mutableStateOf(List(productListFieldLabels.size) { "" })
    }
    var userFields by remember {
        mutableStateOf(newUserListFields())
    }
    var itemProductLines by remember { mutableStateOf(emptyList<String>()) }
    var itemProductSha by remember { mutableStateOf("") }
    var itemProductLoadError by remember { mutableStateOf("") }
    var itemUnitLines by remember { mutableStateOf(emptyList<String>()) }
    var itemUnitSha by remember { mutableStateOf("") }
    var itemUnitLoadError by remember { mutableStateOf("") }
    var unitItemLines by remember { mutableStateOf(emptyList<String>()) }
    var unitItemLoadError by remember { mutableStateOf("") }
    var showItemProductEditor by remember { mutableStateOf(false) }
    var isSavingItemProduct by remember { mutableStateOf(false) }
    var itemProductError by remember { mutableStateOf("") }
    var showItemUnitEditor by remember { mutableStateOf(false) }
    var isSavingItemUnit by remember { mutableStateOf(false) }
    var itemUnitError by remember { mutableStateOf("") }
    var itemFields by remember {
        mutableStateOf(List(itemListFieldLabels.size) { "" })
    }
    var unitFields by remember {
        mutableStateOf(List(unitListFieldLabels.size) { "" })
    }
    var fileFields by remember {
        mutableStateOf(List(fileListFieldLabels.size) { "" })
    }
    var pendingFileDownload by remember { mutableStateOf<FileAttachment?>(null) }
    var fileAttachmentPreview by remember { mutableStateOf<FileAttachment?>(null) }
    var fileAttachmentPreviewPath by remember { mutableStateOf("") }
    var isLoadingFilePreview by remember { mutableStateOf(false) }
    var entryBankLines by remember { mutableStateOf(emptyList<String>()) }
    var entryBankSha by remember { mutableStateOf("") }
    var entryBankLoadError by remember { mutableStateOf("") }
    var bankContactFields by remember {
        mutableStateOf(List(contactListFieldLabels.size) { "" })
    }
    var showBankContactEditor by remember { mutableStateOf(false) }
    var showEntryBankEditor by remember { mutableStateOf(false) }
    var contactCreationEntryFieldIndex by remember { mutableStateOf<Int?>(null) }
    var isSavingBankContact by remember { mutableStateOf(false) }
    var isSavingEntryBank by remember { mutableStateOf(false) }
    var bankContactError by remember { mutableStateOf("") }
    var entryFormError by remember { mutableStateOf("") }
    val entryTypeOptions = remember(
        entryTypeRecords,
        isBanglaTypeSelected
    ) {
        typeListOptions(entryTypeRecords)
    }
    var saveSucceeded by remember { mutableStateOf(false) }
    var showDuplicateQuestionAlert by remember { mutableStateOf(false) }
    var questionSearch by remember { mutableStateOf("") }
    var userSimNumbers by remember { mutableStateOf(emptyList<SimPhoneNumber>()) }
    var showUserSimPicker by remember { mutableStateOf(false) }
    var showUserMobileSourcePicker by remember { mutableStateOf(false) }
    val userEmailFocusRequester = remember { FocusRequester() }
    val userPhonePermissions = remember {
        buildList {
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(Manifest.permission.READ_PHONE_NUMBERS)
            }
        }
    }
    fun loadUserSimNumbers() {
        try {
            val subscriptions = context
                .getSystemService(SubscriptionManager::class.java)
                ?.activeSubscriptionInfoList
                .orEmpty()
            userSimNumbers = subscriptions.map { subscription ->
                SimPhoneNumber(
                    label = subscription.carrierName?.toString()
                        ?.takeIf(String::isNotBlank)
                        ?: subscription.displayName?.toString()
                        ?: "SIM ${subscription.simSlotIndex + 1}",
                    number = subscription.number.orEmpty()
                )
            }.filter { it.number.isNotBlank() }
            if (userSimNumbers.isEmpty()) {
                errorMessage = "No SIM phone number is available to select."
            } else {
                showUserSimPicker = true
                errorMessage = ""
            }
        } catch (exception: SecurityException) {
            Log.e("UserList", "Android denied access to SIM phone numbers.", exception)
            errorMessage = "Android did not allow access to SIM numbers."
        } catch (exception: IllegalStateException) {
            Log.e("UserList", "Could not read SIM phone numbers.", exception)
            errorMessage = "Could not read SIM phone numbers."
        }
    }
    val userSimPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (userPhonePermissions.all { permissions[it] == true }) {
            loadUserSimNumbers()
        } else {
            errorMessage = "Phone permission was denied. SIM numbers cannot be selected."
        }
    }
    val userEmailAccountPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (email.isNullOrBlank()) {
                errorMessage = "Could not read the selected email account."
            } else {
                userFields = userFields.toMutableList().apply {
                    this[0] = email.trim()
                }
                errorMessage = ""
                saveSucceeded = false
                statusMessage = ""
            }
        }
    }
    fun chooseUserEmail() {
        try {
            userEmailAccountPicker.launch(
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    false,
                    "Choose an email account",
                    null,
                    null,
                    null
                )
            )
        } catch (exception: android.content.ActivityNotFoundException) {
            Log.e("UserList", "No Android email account picker is available.", exception)
            errorMessage = "No email account picker is available. Enter the email manually."
        }
    }
    fun chooseUserSimNumber() {
        val missingPermissions = userPhonePermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            loadUserSimNumbers()
        } else {
            userSimPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
    val contactPhonePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedContact = result.data?.data
            if (selectedContact == null) {
                errorMessage = "The selected contact did not include a phone number."
            } else {
                try {
                    val phoneNumber = context.contentResolver.query(
                        selectedContact,
                        arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        val numberColumn = cursor.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        )
                        if (numberColumn >= 0 && cursor.moveToFirst()) {
                            cursor.getString(numberColumn)
                        } else {
                            null
                        }
                    }
                    if (phoneNumber.isNullOrBlank()) {
                        errorMessage = "The selected contact has no phone number."
                    } else {
                        if (isUserList) {
                            userFields = userFields.toMutableList().apply {
                                this[2] = phoneNumber.trim()
                            }
                        } else if (showBankContactEditor) {
                            bankContactFields = bankContactFields.toMutableList().apply {
                                this[3] = phoneNumber.trim()
                            }
                            bankContactError = ""
                        } else {
                            contactFields = contactFields.toMutableList().apply {
                                this[3] = phoneNumber.trim()
                            }
                        }
                        errorMessage = ""
                        saveSucceeded = false
                        statusMessage = ""
                    }
                } catch (exception: SecurityException) {
                    Log.e("ContactPicker", "Android denied access to the selected phone number.", exception)
                    errorMessage = "Android did not allow the selected phone number to be read."
                } catch (exception: IllegalArgumentException) {
                    Log.e("ContactPicker", "Could not read the selected contact phone number.", exception)
                    errorMessage = "Could not read the selected contact phone number."
                }
            }
        }
    }
    val entryLocationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            hasEntryLocationPermission = true
            isLocatingEntryTrack = true
            entryTrackError = ""
        } else {
            entryTrackError =
                "Location permission is required to get the current location."
            isLocatingEntryTrack = false
        }
    }
    DisposableEffect(
        isEntryList,
        showEditor,
        editingIndex,
        saveSucceeded,
        hasEntryLocationPermission
    ) {
        if (!isEntryList || !showEditor || editingIndex != null ||
            saveSucceeded || !hasEntryLocationPermission
        ) {
            onDispose {}
        } else {
            isLocatingEntryTrack = true
            entryTrackError = ""
            val stopLocationUpdates = try {
                startCurrentLocationUpdates(
                    context,
                    onLocation = { location ->
                        val trackUrl = formatEntryTrackUrl(
                            location.latitude,
                            location.longitude
                        )
                        if (trackUrl != null) {
                            entryFields = entryFields.toMutableList().apply {
                                this[2] = trackUrl
                            }
                            entryTrackError = ""
                        }
                        isLocatingEntryTrack = false
                    },
                    onLocationUnavailable = {
                        entryTrackError =
                            "Could not get the current location. Check location services and try again."
                        isLocatingEntryTrack = false
                    }
                )
            } catch (exception: SecurityException) {
                Log.e("EntryLocation", "Location permission was denied.", exception)
                entryTrackError =
                    "Location permission is required to get the current location."
                isLocatingEntryTrack = false
                null
            } catch (exception: IllegalStateException) {
                Log.e("EntryLocation", "Could not start current location updates.", exception)
                entryTrackError =
                    "Could not get the current location. Check location services and try again."
                isLocatingEntryTrack = false
                null
            }
            onDispose {
                stopLocationUpdates?.invoke()
                isLocatingEntryTrack = false
            }
        }
    }
    val indexedQuestions = if (isLineList) {
        entryListDisplayOrder(questions)
    } else {
        questions.mapIndexed { index, question -> index to question }
    }
    val filteredQuestions = indexedQuestions.filter { (_, question) ->
        questionSearch.isBlank() ||
            question.contains(questionSearch.trim(), ignoreCase = true)
    }

    suspend fun publicApiKey(): String? =
        BuildConfig.RUBELAPP_PROXY_API_KEY.takeIf {
            fileName in publicWritableFiles
        }

    LaunchedEffect(isFileList, showEditor, fileFields.getOrNull(2)) {
        val path = fileFields.getOrNull(2).orEmpty()
        if (!isFileList || !showEditor || parseFileAttachmentPath(path) == null ||
            path == fileAttachmentPreviewPath
        ) {
            return@LaunchedEffect
        }
        isLoadingFilePreview = true
        try {
            val (attachment, message) = withContext(Dispatchers.IO) {
                downloadPrivateFileAttachment(
                    BuildConfig.RUBELAPP_PROXY_URL,
                    BuildConfig.RUBELAPP_PROXY_API_KEY,
                    BuildConfig.RUBELAPP_PROXY_API_KEY,
                    path
                )
            }
            if (attachment == null) {
                errorMessage = message
            } else {
                fileAttachmentPreview = attachment
                fileAttachmentPreviewPath = path
            }
        } finally {
            isLoadingFilePreview = false
        }
    }

    val fileAttachmentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null && isFileList && showEditor) {
            val fileId = java.util.UUID.randomUUID().toString()
            scope.launch {
                isWorking = true
                errorMessage = ""
                statusMessage = ""
                try {
                    val attachment = withContext(Dispatchers.IO) {
                        readFileAttachment(context, uri)
                    }
                    val writeToken = publicApiKey() ?: return@launch
                    val result = withContext(Dispatchers.IO) {
                        uploadPrivateFileAttachment(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            writeToken,
                            fileId,
                            attachment
                        )
                    }
                    if (result.success && result.filePath.isNotBlank()) {
                        fileAttachmentPreview = attachment
                        fileAttachmentPreviewPath = result.filePath
                        fileFields = fileFields.toMutableList().apply {
                            this[2] = result.filePath
                        }
                        saveSucceeded = false
                        statusMessage =
                            "File uploaded. Save FileList to link it to this record."
                    } else {
                        errorMessage = result.message
                    }
                } catch (exception: IOException) {
                    Log.e("FileListUpload", "Could not read the selected FileList attachment.", exception)
                    errorMessage = exception.message ?: "Could not read the selected file."
                } catch (exception: SecurityException) {
                    Log.e("FileListUpload", "Android denied access to the selected file.", exception)
                    errorMessage = "Android did not allow access to the selected file."
                } catch (exception: IllegalArgumentException) {
                    Log.e("FileListUpload", "The selected FileList attachment is invalid.", exception)
                    errorMessage = "The selected file could not be processed."
                } finally {
                    isWorking = false
                }
            }
        }
    }
    val saveFileAttachmentPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val attachment = pendingFileDownload
        pendingFileDownload = null
        if (uri != null && attachment != null) {
            scope.launch {
                isWorking = true
                errorMessage = ""
                statusMessage = ""
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: throw IOException("Could not create the selected download file.")
                        output.use { it.write(attachment.bytes) }
                    }
                    statusMessage = "File downloaded successfully."
                } catch (exception: IOException) {
                    Log.e("FileListDownload", "Could not save the downloaded attachment.", exception)
                    errorMessage = exception.message ?: "Could not save the downloaded file."
                } catch (exception: SecurityException) {
                    Log.e("FileListDownload", "Android denied saving the attachment.", exception)
                    errorMessage = "Android did not allow the downloaded file to be saved."
                } finally {
                    isWorking = false
                }
            }
        }
    }

    fun refreshQuestions() {
        if (isWorking) return
        scope.launch {
            isWorking = true
            errorMessage = ""
            statusMessage = ""
            if (isEntryList || isBankList) {
                entryContactsLoaded = false
                entryContactNames = emptyList()
                entryContactLoadError = ""
                contactListLines = emptyList()
                contactListSha = ""
            }
            if (isItemList) {
                itemProductLines = emptyList()
                itemProductSha = ""
                itemProductLoadError = ""
                itemUnitLines = emptyList()
                itemUnitSha = ""
                itemUnitLoadError = ""
            }
            if (isUnitList) {
                unitItemLines = emptyList()
                unitItemLoadError = ""
            }
            if (isEntryList) {
                entryTypeRecords = emptyList()
                entryTypesLoaded = false
                entryTypeLoadError = ""
                entryBankLines = emptyList()
                entryBankSha = ""
                entryBankLoadError = ""
                entryItemLines = emptyList()
                entryItemSha = ""
                entryItemLoadError = ""
            }
            try {
                val result = withContext(Dispatchers.IO) {
                    fetchPrivateGitHubQuestionFile(
                        BuildConfig.RUBELAPP_PROXY_URL,
                        BuildConfig.RUBELAPP_PROXY_API_KEY,
                        fileName,
                        displayName
                    )
                }
                val contactResult = if (isEntryList || isBankList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "ContactList",
                            "Contact List"
                        )
                    }
                } else {
                    null
                }
                val bankResult = if (isEntryList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "BankList",
                            "Bank List"
                        )
                    }
                } else {
                    null
                }
                val entryItemResult = if (isEntryList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "ItemList",
                            "Item List"
                        )
                    }
                } else {
                    null
                }
                val setupTypeResult = if (isEntryList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "TypeList",
                            "Type List"
                        )
                    }
                } else {
                    null
                }
                val productResult = if (isItemList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "ProductList",
                            "Product List"
                        )
                    }
                } else {
                    null
                }
                val unitResult = if (isItemList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "UnitList",
                            "Unit List"
                        )
                    }
                } else {
                    null
                }
                val unitItemResult = if (isUnitList) {
                    withContext(Dispatchers.IO) {
                        fetchPrivateGitHubQuestionFile(
                            BuildConfig.RUBELAPP_PROXY_URL,
                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                            "ItemList",
                            "Item List"
                        )
                    }
                } else {
                    null
                }
                if (contactResult != null) {
                    entryContactsLoaded = true
                    if (contactResult.success) {
                        contactListLines = contactResult.fileLines
                        entryContactNames = parseEntryContactNames(contactResult.fileLines)
                        contactListSha = contactResult.fileSha
                        if (contactListSha.isBlank()) {
                            entryContactLoadError =
                                "The ContactList file revision is missing. Please refresh."
                        }
                    } else {
                        entryContactLoadError = contactResult.message
                        errorMessage = "Could not load contacts from ContactList: " +
                            contactResult.message
                    }
                }
                if (bankResult != null) {
                    if (bankResult.success && bankResult.fileSha.isNotBlank()) {
                        entryBankLines = bankResult.fileLines
                        entryBankSha = bankResult.fileSha
                    } else {
                        entryBankLoadError = bankResult.message
                            .takeIf(String::isNotBlank)
                            ?: "The BankList file revision is missing. Please refresh."
                        errorMessage = "Could not load banks from BankList: $entryBankLoadError"
                    }
                }
                if (entryItemResult != null) {
                    if (entryItemResult.success && entryItemResult.fileSha.isNotBlank()) {
                        entryItemLines = normalizeItemListLines(entryItemResult.fileLines)
                        entryItemSha = entryItemResult.fileSha
                    } else {
                        entryItemLoadError = entryItemResult.message
                            .takeIf(String::isNotBlank)
                            ?: "The ItemList file revision is missing. Please refresh."
                    }
                }
                if (setupTypeResult != null) {
                    entryTypesLoaded = true
                    if (setupTypeResult.success && setupTypeResult.fileSha.isNotBlank()) {
                        val records = setupTypeResult.fileLines.mapNotNull {
                            parseTypeListRecord(it, isBanglaTypeSelected)
                        }
                        if (records.size == setupTypeResult.fileLines.size) {
                            val storedLanguage = typeListLanguageSelection(records)
                            if (storedLanguage != null) {
                                isBanglaTypeSelected = storedLanguage
                                context.getSharedPreferences(
                                    PREFERENCES_NAME,
                                    android.content.Context.MODE_PRIVATE
                                ).edit()
                                    .putBoolean(SETUP_LIST_TYPE_LANGUAGE_KEY, storedLanguage)
                                    .apply()
                            }
                            entryTypeRecords = records
                        } else {
                            entryTypeLoadError =
                                "TypeList contains an invalid row. Correct it before selecting enType."
                        }
                    } else if (setupTypeResult.success) {
                        entryTypeLoadError =
                            "The TypeList file revision is missing. Please refresh."
                    } else {
                        entryTypeLoadError = setupTypeResult.message
                            .takeIf(String::isNotBlank)
                            ?: "Could not load the TypeList types."
                    }
                }
                if (productResult != null) {
                    if (productResult.success && productResult.fileSha.isNotBlank()) {
                        itemProductLines = normalizeProductListLines(productResult.fileLines)
                        itemProductSha = productResult.fileSha
                    } else {
                        itemProductLoadError = productResult.message
                            .takeIf(String::isNotBlank)
                            ?: "The ProductList file revision is missing. Please refresh."
                    }
                }
                if (unitResult != null) {
                    if (unitResult.success && unitResult.fileSha.isNotBlank()) {
                        itemUnitLines = normalizeUnitListLines(unitResult.fileLines)
                        itemUnitSha = unitResult.fileSha
                    } else {
                        itemUnitLoadError = unitResult.message
                            .takeIf(String::isNotBlank)
                            ?: "The UnitList file revision is missing. Please refresh."
                    }
                }
                if (unitItemResult != null) {
                    if (unitItemResult.success && unitItemResult.fileSha.isNotBlank()) {
                        unitItemLines = normalizeItemListLines(unitItemResult.fileLines)
                    } else {
                        unitItemLoadError = unitItemResult.message
                            .takeIf(String::isNotBlank)
                            ?: "Could not load ItemList to calculate utCount."
                    }
                }
                if (result.success) {
                    questions = if (isLineList) {
                        when {
                            isProductList -> normalizeProductListLines(result.fileLines)
                            isUserList -> normalizeUserListLines(result.fileLines)
                            isItemList -> normalizeItemListLines(result.fileLines)
                            isUnitList -> if (unitItemLoadError.isBlank()) {
                                populateUnitListCounts(result.fileLines, unitItemLines)
                            } else {
                                normalizeUnitListLines(result.fileLines)
                            }
                            isFileList -> normalizeFileListLines(result.fileLines)
                            else -> result.fileLines
                        }
                    } else {
                        result.questions.map { question ->
                            if (question.answer.isBlank()) {
                                question.question
                            } else {
                                "${question.question}=${question.answer}"
                            }
                        }
                    }
                    fileSha = result.fileSha
                    if (fileSha.isBlank()) {
                        errorMessage = "The question file revision is missing. Please refresh."
                    }
                } else {
                    errorMessage = result.message
                }
            } finally {
                isWorking = false
            }
        }
    }

    LaunchedEffect(fileName) {
        refreshQuestions()
    }

    LaunchedEffect(isEntryList, showEditor, entryItemSha) {
        if (isEntryList && showEditor && entryItemSha.isNotBlank()) {
            entryFields = entryFields.toMutableList().apply {
                this[17] = calculateEntryItemTotal(this[0], entryItemLines).orEmpty()
            }
        }
    }

    LaunchedEffect(isFileList, contactFileId) {
        val contactId = contactFileId
        if (!isFileList || contactId.isNullOrBlank()) return@LaunchedEffect
        questionSearch = contactId
    }

    LaunchedEffect(isItemList, itemSearchId) {
        val entryId = itemSearchId
        if (!isItemList || entryId.isNullOrBlank()) return@LaunchedEffect
        questionSearch = entryId
    }

    LaunchedEffect(isContactList, isProductList, clearContactSearchOnReturn) {
        if ((isContactList || isProductList) && clearContactSearchOnReturn) {
            questionSearch = ""
            onContactSearchReset()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = (20 * scale).dp, vertical = (16 * scale).dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) {
                    Text("←", color = titleBlue, fontSize = (24 * scale).sp)
                }
                Text(
                    text = when {
                        isEntryList -> "EntryList Form"
                        isContactList -> "ContactList Form"
                        isFileList -> "FileList Form"
                        isProductList -> "ProductList Form"
                        isItemList -> "ItemList Form"
                        isUnitList -> "UnitList Form"
                        isUserList -> "UserList Form"
                        isBankList -> "BankList Form"
                        isRawLineList -> "$displayName Form"
                        else -> "Manage Question"
                    },
                    modifier = Modifier.weight(1f),
                    color = titleBlue,
                    fontSize = (22 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                TextButton(
                    enabled = !isWorking,
                    onClick = ::refreshQuestions
                ) {
                    Text(if (isWorking) "…" else "↻", color = titleBlue, fontSize = (22 * scale).sp)
                }
            }
            BasicTextField(
                value = questionSearch,
                onValueChange = { questionSearch = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (8 * scale).dp)
                    .border(
                        BorderStroke((1 * scale).dp, Color.LightGray),
                        RoundedCornerShape((8 * scale).dp)
                    )
                    .padding(horizontal = (12 * scale).dp, vertical = (11 * scale).dp)
                    .semantics {
                        contentDescription = when {
                            isFileList -> "flSearch"
                            isContactList -> "cnSearch"
                            isEntryList -> "enSearch"
                            isProductList -> "pdSearch"
                            isItemList -> "itSearch"
                            isUnitList -> "utSearch"
                            isUserList -> "usSearch"
                            else -> "qnSearch"
                        }
                    },
                singleLine = true,
                textStyle = TextStyle(color = Color.Black, fontSize = (15 * scale).sp),
                decorationBox = { innerTextField ->
                    Box {
                        if (questionSearch.isEmpty()) {
                            Text(
                                when {
                                    isEntryList -> "enSearch"
                                    isContactList -> "cnSearch"
                                    isFileList -> "flSearch"
                                    isProductList -> "pdSearch"
                                    isItemList -> "itSearch"
                                    isUnitList -> "utSearch"
                                    isUserList -> "usSearch"
                                    isRawLineList -> "Search $displayName"
                                    else -> "qnSearch"
                                },
                                color = Color.Gray,
                                fontSize = (15 * scale).sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = (8 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isLineList) {
                        "${questions.size} ${
                            when {
                                isEntryList -> "entries"
                                isContactList -> "contacts"
                                isUserList -> "users"
                                else -> "records"
                            }
                        }"
                    } else {
                        "${questions.size} questions"
                    },
                    modifier = Modifier.weight(1f),
                    color = Color.DarkGray,
                    fontSize = (14 * scale).sp
                )
                if (!isReadOnly) ActionButton(
                    text = when {
                        isContactList -> "Add Contact"
                        isBankList -> "Add Bank"
                        isFileList -> "Add File"
                        isProductList -> "Add Product"
                        isItemList -> "Add Item"
                        isUnitList -> "Add Unit"
                        isUserList -> "Add User"
                        isLineList -> "Add Entry"
                        else -> "Add Question"
                    },
                    color = submitGreen,
                    compact = true,
                    enabled = !isWorking
                ) {
                    editingIndex = null
                    questionText = ""
                    answerText = ""
                    val now = Date()
                    contactFields = if (isContactList) {
                        newContactListFields(formatEntryListId(now))
                    } else {
                        List(contactListFieldLabels.size) { "" }
                    }
                    bankFields = if (isBankList) {
                        listOf(formatEntryListId(now), "") + List(5) { "" }
                    } else {
                        List(bankListFieldLabels.size) { "" }
                    }
                    productFields = if (isProductList) {
                        newProductListFields()
                    } else {
                        List(productListFieldLabels.size) { "" }
                    }
                    userFields = if (isUserList) {
                        newUserListFields()
                    } else {
                        List(userListFieldLabels.size) { "" }
                    }
                    itemFields = if (isItemList) {
                        newItemListFields(questionSearch)
                    } else {
                        List(itemListFieldLabels.size) { "" }
                    }
                    unitFields = if (isUnitList) {
                        newUnitListFields()
                    } else {
                        List(unitListFieldLabels.size) { "" }
                    }
                    fileFields = if (isFileList) {
                        newFileListFields(questionSearch.trim())
                    } else {
                        List(fileListFieldLabels.size) { "" }
                    }
                    fileAttachmentPreview = null
                    fileAttachmentPreviewPath = ""
                    entryFields = if (isEntryList) {
                        val loginEmail = context.getSharedPreferences(
                            PREFERENCES_NAME,
                            android.content.Context.MODE_PRIVATE
                        ).getString(SAVED_LOGIN_EMAIL_KEY, "").orEmpty()
                        newEntryListFields(now, loginEmail).toMutableList().apply {
                            this[17] = calculateEntryItemTotal(this[0], entryItemLines).orEmpty()
                        }
                    } else {
                        List(18) { "" }
                    }
                    entryFormError = ""
                    entryTrackError = ""
                    saveSucceeded = false
                    showDuplicateQuestionAlert = false
                    showEditor = true
                    if (isEntryList) {
                        val hasLocationPermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED ||
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                        if (hasLocationPermission) {
                            hasEntryLocationPermission = true
                            isLocatingEntryTrack = true
                        } else {
                            hasEntryLocationPermission = false
                            isLocatingEntryTrack = true
                            entryLocationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                }
            }
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFB42318),
                    fontSize = (14 * scale).sp,
                    modifier = Modifier.padding(bottom = (8 * scale).dp)
                )
            } else if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    color = submitGreen,
                    fontSize = (14 * scale).sp,
                    modifier = Modifier.padding(bottom = (8 * scale).dp)
                )
            }
            if (questions.isEmpty()) {
                Text(
                    text = if (isWorking) "Loading data..." else "No data loaded.",
                    modifier = Modifier.padding((20 * scale).dp),
                    color = Color.DarkGray
                )
            } else if (filteredQuestions.isEmpty()) {
                Text(
                    text = "No matching questions.",
                    modifier = Modifier.padding((20 * scale).dp),
                    color = Color.DarkGray
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy((8 * scale).dp)
                ) {
                    items(filteredQuestions, key = { it.first }) { (index, question) ->
                        val userFieldsForRow = if (isUserList) {
                            parseUserListFields(question)
                        } else {
                            emptyList()
                        }
                        val userRowColor = when {
                            !isUserList -> Color.Transparent
                            userFieldsForRow.getOrNull(6).orEmpty().isNotBlank() ->
                                Color(0xFFFFF4CC)
                            userFieldsForRow.getOrNull(5) == "On" ->
                                Color(0xFFE8F5E9)
                            userFieldsForRow.getOrNull(5) == "Off" ->
                                Color(0xFFFFE8E6)
                            else -> Color.Transparent
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    userRowColor,
                                    RoundedCornerShape((10 * scale).dp)
                                )
                                .border(
                                    BorderStroke((1 * scale).dp, Color.LightGray),
                                    RoundedCornerShape((10 * scale).dp)
                                )
                                .padding((10 * scale).dp),
                        ) {
                            Text(
                                text = question,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color.Black,
                                fontSize = (15 * scale).sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompositionLocalProvider(
                                    LocalLayoutDirection provides LayoutDirection.Rtl
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    if (isEntryList && shouldShowEntryItemAction(question)) {
                                    TextButton(
                                        modifier = Modifier.height((28 * scale).dp),
                                        contentPadding = PaddingValues(
                                            horizontal = (4 * scale).dp,
                                            vertical = 0.dp
                                        ),
                                        enabled = !isWorking,
                                        onClick = {
                                            val entryId = linkedFileSearchId(
                                                fileName,
                                                question
                                            )
                                            if (entryId.isNotBlank()) {
                                                onOpenItemListForEntry(entryId)
                                            }
                                        }
                                    ) {
                                        Text("Item", fontSize = (12 * scale).sp)
                                    }
                                }
                                if (isEnterpriseDataFile(fileName)) {
                                    TextButton(
                                        modifier = Modifier.height((28 * scale).dp),
                                        contentPadding = PaddingValues(
                                            horizontal = (4 * scale).dp,
                                            vertical = 0.dp
                                        ),
                                        enabled = !isWorking,
                                        onClick = {
                                            val recordId = linkedFileSearchId(
                                                fileName,
                                                question
                                            )
                                            if (recordId.isNotBlank()) {
                                                onOpenFileListForContact(recordId)
                                            }
                                        }
                                    ) {
                                        Text("File", fontSize = (12 * scale).sp)
                                    }
                                }
                                if (!isReadOnly)
                                TextButton(
                                    modifier = if (isEnterpriseDataFile(fileName)) {
                                        Modifier.height((28 * scale).dp)
                                    } else {
                                        Modifier
                                    },
                                    contentPadding = if (isEnterpriseDataFile(fileName)) {
                                        PaddingValues(
                                            horizontal = (4 * scale).dp,
                                            vertical = 0.dp
                                        )
                                    } else {
                                        PaddingValues(horizontal = (12 * scale).dp)
                                    },
                                    enabled = !isWorking,
                                    onClick = {
                                        editingIndex = index
                                        val entry = questions[index]
                                        val separatorIndex = entry.indexOf('=')
                                        questionText = if (isLineList) {
                                            entry
                                        } else if (separatorIndex >= 0) {
                                            entry.substring(0, separatorIndex).trim()
                                        } else {
                                            entry
                                        }
                                        answerText = if (!isLineList && separatorIndex >= 0) {
                                            entry.substring(separatorIndex + 1).trim()
                                        } else {
                                            ""
                                        }
                                        if (isEntryList) {
                                            entryFields = parseEntryListFields(
                                                entry,
                                                entryTypeOptions
                                            )
                                                .toMutableList()
                                                .apply {
                                                    this[11] =
                                                        calculateEntryCashBankTotal(this[8], this[9])
                                                            .orEmpty()
                                                    this[17] =
                                                        calculateEntryItemTotal(this[0], entryItemLines)
                                                            .orEmpty()
                                                }
                                            entryFormError = ""
                                            entryTrackError = ""
                                        } else if (isBankList) {
                                            bankFields = parseBankListFields(entry)
                                        } else if (isContactList) {
                                            contactFields = parseContactListFields(entry)
                                        } else if (isProductList) {
                                            productFields = parseProductListFields(entry)
                                        } else if (isUserList) {
                                            userFields = parseUserListFields(entry)
                                        } else if (isItemList) {
                                            itemFields = parseItemListFields(entry)
                                                .toMutableList()
                                                .apply {
                                                    this[5] = calculateItemListTotal(
                                                        this[3],
                                                        this[4],
                                                        this[2],
                                                        itemUnitLines
                                                    ).orEmpty()
                                                }
                                        } else if (isUnitList) {
                                            unitFields = parseUnitListFields(entry)
                                        } else if (isFileList) {
                                            fileAttachmentPreview = null
                                            fileAttachmentPreviewPath = ""
                                            fileFields = parseFileListFields(entry)
                                        }
                                        saveSucceeded = false
                                        showDuplicateQuestionAlert = false
                                        showEditor = true
                                    }
                                ) {
                                    Text(
                                        "Edit",
                                        fontSize = if (isEnterpriseDataFile(fileName)) {
                                            (12 * scale).sp
                                        } else {
                                            (14 * scale).sp
                                        }
                                    )
                                }
                                if (!isReadOnly &&
                                    (!isUnitList || shouldShowUnitDeleteAction(question))
                                ) TextButton(
                                    modifier = if (isEnterpriseDataFile(fileName)) {
                                        Modifier.height((28 * scale).dp)
                                    } else {
                                        Modifier
                                    },
                                    contentPadding = if (isEnterpriseDataFile(fileName)) {
                                        PaddingValues(
                                            horizontal = (4 * scale).dp,
                                            vertical = 0.dp
                                        )
                                    } else {
                                        PaddingValues(horizontal = (12 * scale).dp)
                                    },
                                    enabled = !isWorking &&
                                        canDeleteQuestionLine(fileName, questions.size),
                                    onClick = {
                                        val updatedQuestions = questions.toMutableList().apply {
                                            removeAt(index)
                                        }
                                        val deletedFileAttachmentPath =
                                            if (isFileList) {
                                                parseFileListFields(questions[index])[2]
                                                    .takeIf {
                                                        parseFileAttachmentPath(it) != null
                                                    }
                                            } else {
                                                null
                                            }
                                        val deletedLinkedRecordId = if (
                                            isContactList || isEntryList || isProductList ||
                                            isUserList
                                        ) {
                                            linkedFileSearchId(fileName, questions[index])
                                        } else {
                                            ""
                                        }
                                        scope.launch {
                                            isWorking = true
                                            errorMessage = ""
                                            statusMessage = ""
                                            var linkedFileRecordsRemoved = false
                                            var linkedItemRecordsRemoved = false
                                            try {
                                                val writeToken = publicApiKey()
                                                    ?: return@launch
                                                if (isEntryList &&
                                                    deletedLinkedRecordId.isNotBlank()
                                                ) {
                                                    val itemListResult = withContext(
                                                        Dispatchers.IO
                                                    ) {
                                                        fetchPrivateGitHubQuestionFile(
                                                            BuildConfig.RUBELAPP_PROXY_URL,
                                                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                            "ItemList",
                                                            "Item List"
                                                        )
                                                    }
                                                    if (!itemListResult.success ||
                                                        itemListResult.fileSha.isBlank()
                                                    ) {
                                                        errorMessage = itemListResult.message
                                                            .takeIf(String::isNotBlank)
                                                            ?: "Could not load ItemList before deleting this entry."
                                                        return@launch
                                                    }
                                                    val (remainingItems, hasMatchingItems) =
                                                        removeItemListRowsForEntryId(
                                                            itemListResult.fileLines,
                                                            deletedLinkedRecordId
                                                        )
                                                    if (hasMatchingItems) {
                                                        val itemSaveResult = withContext(
                                                            Dispatchers.IO
                                                        ) {
                                                            savePrivateQuestionFile(
                                                                BuildConfig.RUBELAPP_PROXY_URL,
                                                                BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                writeToken,
                                                                "ItemList",
                                                                "Item List",
                                                                itemListResult.fileSha,
                                                                remainingItems
                                                            )
                                                        }
                                                        if (!itemSaveResult.success ||
                                                            itemSaveResult.fileSha.isBlank()
                                                        ) {
                                                            errorMessage =
                                                                "Could not delete matching ItemList records; this entry was not deleted. " +
                                                                    itemSaveResult.message
                                                            return@launch
                                                        }
                                                        linkedItemRecordsRemoved = true
                                                    }
                                                }
                                                val linkedFileAttachments =
                                                    if (deletedLinkedRecordId.isNotBlank() &&
                                                        (isContactList || isEntryList ||
                                                            isProductList || isUserList)
                                                    ) {
                                                        val fileListResult = withContext(
                                                            Dispatchers.IO
                                                        ) {
                                                            fetchPrivateGitHubQuestionFile(
                                                                BuildConfig.RUBELAPP_PROXY_URL,
                                                                BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                "FileList",
                                                                "File List"
                                                            )
                                                        }
                                                        if (!fileListResult.success ||
                                                            fileListResult.fileSha.isBlank()
                                                        ) {
                                                            val failure = fileListResult.message
                                                                .takeIf(String::isNotBlank)
                                                                ?: "Could not load FileList before deleting this record."
                                                            errorMessage = if (
                                                                linkedItemRecordsRemoved
                                                            ) {
                                                                "Matching ItemList records were deleted, but FileList could not be loaded and this entry was not deleted: $failure"
                                                            } else {
                                                                failure
                                                            }
                                                            return@launch
                                                        }
                                                        val (remainingFiles, attachmentPaths) =
                                                            removeFileListRowsForRecordId(
                                                                fileListResult.fileLines,
                                                                deletedLinkedRecordId
                                                            )
                                                        if (remainingFiles.size !=
                                                            normalizeFileListLines(
                                                                fileListResult.fileLines
                                                            ).size
                                                        ) {
                                                            val fileSaveResult = withContext(
                                                                Dispatchers.IO
                                                            ) {
                                                                savePrivateQuestionFile(
                                                                    BuildConfig.RUBELAPP_PROXY_URL,
                                                                    BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                    writeToken,
                                                                    "FileList",
                                                                    "File List",
                                                                    fileListResult.fileSha,
                                                                    serializeFileListLines(
                                                                        remainingFiles
                                                                    )
                                                                )
                                                            }
                                                            if (!fileSaveResult.success ||
                                                                fileSaveResult.fileSha.isBlank()
                                                            ) {
                                                                val failure = fileSaveResult.message
                                                                    .takeIf(String::isNotBlank)
                                                                    ?: "Could not verify the saved FileList."
                                                                errorMessage = if (
                                                                    linkedItemRecordsRemoved
                                                                ) {
                                                                    "Matching ItemList records were deleted, but matching FileList records could not be deleted and this entry was not deleted: $failure"
                                                                } else {
                                                                    "Could not delete matching FileList records; this record was not deleted. $failure"
                                                                }
                                                                return@launch
                                                            }
                                                            linkedFileRecordsRemoved = true
                                                        }
                                                        attachmentPaths
                                                    } else {
                                                        emptyList()
                                                    }
                                                val result = withContext(Dispatchers.IO) {
                                                    savePrivateQuestionFile(
                                                        BuildConfig.RUBELAPP_PROXY_URL,
                                                        BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                        writeToken,
                                                        fileName,
                                                        displayName,
                                                        fileSha,
                                                        when {
                                                            isEntryList ->
                                                                serializeEntryListLines(updatedQuestions)
                                                            isProductList ->
                                                                serializeProductListLines(updatedQuestions)
                                                            isUserList ->
                                                                serializeUserListLines(updatedQuestions)
                                                            isFileList ->
                                                                serializeFileListLines(updatedQuestions)
                                                            else -> updatedQuestions
                                                        }
                                                    )
                                                }
                                                if (result.success && result.fileSha.isNotBlank()) {
                                                    questions = updatedQuestions
                                                    fileSha = result.fileSha
                                                    val attachmentPathsToDelete =
                                                        linkedFileAttachments +
                                                            listOfNotNull(
                                                                deletedFileAttachmentPath
                                                            )
                                                    val attachmentDeleteErrors =
                                                        attachmentPathsToDelete.mapNotNull { path ->
                                                            val attachmentResult = withContext(
                                                                Dispatchers.IO
                                                            ) {
                                                                deletePrivateFileAttachment(
                                                                    BuildConfig.RUBELAPP_PROXY_URL,
                                                                    BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                    writeToken,
                                                                    path
                                                                )
                                                            }
                                                            if (attachmentResult.success) {
                                                                null
                                                            } else {
                                                                attachmentResult.message
                                                            }
                                                        }
                                                    if (attachmentDeleteErrors.isNotEmpty()) {
                                                        errorMessage = if (isContactList ||
                                                            isEntryList || isProductList ||
                                                            isUserList
                                                        ) {
                                                            "$displayName and matching linked records deleted, but some attachments could not be deleted: " +
                                                                attachmentDeleteErrors.joinToString("; ")
                                                        } else {
                                                            "File record deleted, but the GitHub attachment could not be deleted: " +
                                                                attachmentDeleteErrors.joinToString("; ")
                                                        }
                                                    } else if (attachmentPathsToDelete.isNotEmpty()) {
                                                        statusMessage = if (isContactList ||
                                                            isEntryList || isProductList ||
                                                            isUserList
                                                        ) {
                                                            if (isEntryList &&
                                                                linkedItemRecordsRemoved
                                                            ) {
                                                                "Entry, matching ItemList and FileList records, and attachments deleted."
                                                            } else {
                                                                "$displayName and matching FileList records and attachments deleted."
                                                            }
                                                        } else {
                                                            "File and GitHub attachment deleted."
                                                        }
                                                    } else {
                                                        statusMessage = when {
                                                            isContactList -> "Contact deleted."
                                                            isUserList -> "User deleted."
                                                            isEntryList && linkedItemRecordsRemoved ->
                                                                "Entry and matching ItemList records deleted."
                                                            isFileList -> "File deleted."
                                                            isItemList -> "Item deleted."
                                                            isLineList -> "Entry deleted."
                                                            else -> "Question deleted."
                                                        }
                                                    }
                                                } else {
                                                    val failure = result.message
                                                        .takeIf(String::isNotBlank)
                                                        ?: "Could not verify the saved question file. Refresh before editing."
                                                    errorMessage = if (
                                                        (isContactList || isEntryList ||
                                                            isProductList || isUserList) &&
                                                        linkedFileRecordsRemoved
                                                    ) {
                                                        "Matching linked records were deleted, but $displayName was not deleted: $failure"
                                                    } else if (linkedItemRecordsRemoved) {
                                                        "Matching ItemList records were deleted, but this entry was not deleted: $failure"
                                                    } else {
                                                        failure
                                                    }
                                                }
                                            } finally {
                                                isWorking = false
                                            }
                                        }
                                    }
                                ) {
                                    Text(
                                        "Delete",
                                        color = Color(0xFFB42318),
                                        fontSize = if (isEnterpriseDataFile(fileName)) {
                                            (12 * scale).sp
                                        } else {
                                            (14 * scale).sp
                                        }
                                    )
                                }
                                    }
                                }
                                Text(
                                    text = "${index + 1}.",
                                    color = Color.DarkGray,
                                    fontSize = (12 * scale).sp,
                                    modifier = Modifier.padding(start = (4 * scale).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showEditor) {
        val wideForm =
            isEntryList || isContactList || isBankList || isProductList || isFileList ||
            isItemList || isUnitList || isUserList
        AlertDialog(
            modifier = if (wideForm) {
                Modifier.padding(horizontal = 2.dp).fillMaxWidth()
            } else {
                Modifier
            },
            properties = if (wideForm) {
                DialogProperties(usePlatformDefaultWidth = false)
            } else {
                DialogProperties()
            },
            onDismissRequest = { if (!isWorking) showEditor = false },
            title = {
                val title = when {
                    isContactList && editingIndex == null -> "Add Contact"
                    isContactList -> "Update Contact"
                    isBankList && editingIndex == null -> "Add Bank"
                    isFileList && editingIndex == null -> "Add File"
                    isFileList -> "Update File"
                    isProductList && editingIndex == null -> "Add Product"
                    isProductList -> "Update Product"
                    isUserList && editingIndex == null -> "Add User"
                    isUserList -> "Update User"
                    isItemList && editingIndex == null -> "Add Item"
                    isItemList -> "Update Item"
                    isUnitList && editingIndex == null -> "Add Unit"
                    isUnitList -> "Update Unit"
                    isLineList && editingIndex == null -> "Add Entry"
                    isLineList -> "Update Entry"
                    editingIndex == null -> "Add Question"
                    else -> "Update Question"
                }
                Text(title, color = Color.Black)
            },
            text = {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .then(
                            if (isEntryList) {
                                Modifier
                                    .heightIn(
                                        max = (LocalConfiguration.current.screenHeightDp - 180)
                                            .coerceAtLeast(320).dp
                                    )
                                    .verticalScroll(rememberScrollState())
                            } else {
                                Modifier
                            }
                        )
                ) {
                    if (isEntryList) {
                        val entryFieldLabels = listOf(
                            "enID",
                            "enUser",
                            "enTrack",
                            "enDate",
                            "enType",
                            "enSector",
                            "enInvoice",
                            "enContact",
                            "enCash",
                            "enBank",
                            "enBName",
                            "enCB",
                            "enDiscount",
                            "enLabor",
                            "enRent",
                            "enCashier",
                            "enNote",
                            "enITotal"
                        )
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                entryFieldLabels.withIndex().chunked(2).forEach { indexedLabels ->
                                    val visibleLabels = indexedLabels.filter { (_, label) ->
                                        isEntryFieldVisible(label, entryFields)
                                    }
                                    if (visibleLabels.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                        ) {
                                            visibleLabels.forEach { (fieldIndex, label) ->
                                            EntryListOutlinedField(
                                                value = entryFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                typeOptions = if (label == "enType") {
                                                    entryTypeOptions
                                                } else if (label in setOf("enContact", "enCashier")) {
                                                    val options = if (label == "enCashier") {
                                                        parseEntryCashierNames(contactListLines)
                                                    } else {
                                                        entryContactNames +
                                                            listOfNotNull(
                                                                entryFields[fieldIndex]
                                                                    .takeIf(String::isNotBlank)
                                                            )
                                                    }
                                                    options.distinct()
                                                } else if (label == "enBName") {
                                                    (
                                                        entryBankNameOptions(
                                                            entryBankLines,
                                                            entryFields[7]
                                                        ) +
                                                            listOfNotNull(
                                                                entryFields[fieldIndex]
                                                                    .takeIf(String::isNotBlank)
                                                            )
                                                        ).distinct()
                                                } else {
                                                    null
                                                },
                                                suggestionOptions = if (label == "enSector") {
                                                    entryListSectorOptions(
                                                        questions,
                                                        entryFields[4],
                                                        entryTypeOptions
                                                    )
                                                } else {
                                                    null
                                                },
                                                pickerEmptyLabel = when (label) {
                                                    "enType" -> when {
                                                        entryTypeLoadError.isNotBlank() ->
                                                            "Setup types unavailable: $entryTypeLoadError"
                                                        !entryTypesLoaded -> "Loading TypeList types..."
                                                        entryTypeOptions.isEmpty() ->
                                                            "No types available in TypeList"
                                                        else -> null
                                                    }
                                                    "enContact", "enCashier" -> when {
                                                        !entryContactsLoaded -> "Loading contacts..."
                                                        entryContactLoadError.isNotBlank() ->
                                                            "Contacts unavailable"
                                                        (if (label == "enCashier") {
                                                            parseEntryCashierNames(contactListLines)
                                                        } else {
                                                            entryContactNames
                                                        }).isEmpty() ->
                                                            "No contacts available"
                                                        else -> null
                                                    }
                                                    "enBName" -> when {
                                                        entryBankLoadError.isNotBlank() ->
                                                            "Banks unavailable"
                                                        entryBankNameOptions(
                                                            entryBankLines,
                                                            entryFields[7]
                                                        ).isEmpty() -> "No banks for this contact"
                                                        else -> null
                                                    }
                                                    else -> null
                                                },
                                                readOnly = label in setOf(
                                                    "enID",
                                                    "enUser",
                                                    "enTrack",
                                                    "enCB",
                                                    "enITotal"
                                                ),
                                                datePicker = label == "enDate",
                                                onLongClick = when (label) {
                                                    "enContact", "enCashier" -> {
                                                        {
                                                            contactCreationEntryFieldIndex =
                                                                fieldIndex
                                                            bankContactFields =
                                                                newContactListFields(
                                                                    formatEntryListId(Date())
                                                                )
                                                            bankContactError = ""
                                                            showBankContactEditor = true
                                                        }
                                                    }
                                                    "enBName" -> {
                                                        {
                                                            contactCreationEntryFieldIndex = null
                                                            bankFields = listOf(
                                                                formatEntryListId(Date()),
                                                                "",
                                                                "",
                                                                entryFields[7],
                                                                "",
                                                                "",
                                                                ""
                                                            )
                                                            bankContactError = ""
                                                            showEntryBankEditor = true
                                                        }
                                                    }
                                                    else -> null
                                                },
                                                onValueChange = { value ->
                                                    val updatedFields =
                                                        entryFields.toMutableList().apply {
                                                            this[fieldIndex] = value
                                                            if (fieldIndex == 8 || fieldIndex == 9) {
                                                                this[11] =
                                                                    calculateEntryCashBankTotal(
                                                                        this[8],
                                                                        this[9]
                                                                    ).orEmpty()
                                                            }
                                                            this[17] =
                                                                calculateEntryItemTotal(
                                                                    this[0],
                                                                    entryItemLines
                                                                ).orEmpty()
                                                        }
                                                    entryFields = updatedFields
                                                    entryFormError = if (
                                                        (fieldIndex == 8 || fieldIndex == 9) &&
                                                        calculateEntryCashBankTotal(
                                                            updatedFields[8],
                                                            updatedFields[9]
                                                        ) == null
                                                    ) {
                                                        "enCash and enBank must contain valid numbers."
                                                    } else {
                                                        ""
                                                    }
                                                    if (fieldIndex == 2) {
                                                        entryTrackError = ""
                                                    }
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                        }
                                    }
                                }
                                if (entryItemLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not calculate enITotal: $entryItemLoadError",
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                } else if (entryItemSha.isBlank()) {
                                    Text(
                                        text = "Loading ItemList to calculate enITotal...",
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                } else if (
                                    calculateEntryItemTotal(entryFields[0], entryItemLines) == null
                                ) {
                                    Text(
                                        text = "A matching ItemList row has an invalid itTotal.",
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                }
                                if (entryFormError.isNotBlank()) {
                                    Text(
                                        text = entryFormError,
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                }
                                if (entryContactLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not load contacts from ContactList: " +
                                            entryContactLoadError,
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                }
                                if (entryBankLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not load banks from BankList: " +
                                            entryBankLoadError,
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                }
                                if (isLocatingEntryTrack) {
                                    Text(
                                        text = "Getting current location...",
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                } else if (entryTrackError.isNotBlank()) {
                                    Text(
                                        text = entryTrackError,
                                        color = Color(0xFFB42318),
                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                    )
                                }
                            }
                        }
                    } else if (isBankList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                bankListFieldLabels.chunked(2).forEach { labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEach { label ->
                                            val fieldIndex = bankListFieldLabels.indexOf(label)
                                            EntryListOutlinedField(
                                                value = bankFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "bnID",
                                                typeOptions = if (label == "bnContact") {
                                                    (
                                                        entryContactNames +
                                                            listOfNotNull(
                                                                bankFields[fieldIndex]
                                                                    .takeIf(String::isNotBlank)
                                                            )
                                                        ).distinct()
                                                } else {
                                                    null
                                                },
                                                pickerEmptyLabel = if (label == "bnContact") {
                                                    when {
                                                        !entryContactsLoaded ->
                                                            "Loading contacts..."
                                                        entryContactLoadError.isNotBlank() ->
                                                            "Contacts unavailable"
                                                        entryContactNames.isEmpty() ->
                                                            "No contacts available"
                                                        else -> null
                                                    }
                                                } else {
                                                    null
                                                },
                                                onLongClick = if (label == "bnContact") {
                                                    {
                                                        contactCreationEntryFieldIndex = null
                                                        bankContactFields =
                                                            newContactListFields(
                                                                formatEntryListId(Date())
                                                            )
                                                        bankContactError = ""
                                                        showBankContactEditor = true
                                                    }
                                                } else {
                                                    null
                                                },
                                                onValueChange = { value ->
                                                    bankFields = bankFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                    }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                    }
                                    if (entryContactLoadError.isNotBlank()) {
                                        Text(
                                            text = "Could not load contacts from ContactList: " +
                                                entryContactLoadError,
                                            color = Color(0xFFB42318),
                                            modifier = Modifier.padding(top = (8 * scale).dp)
                                        )
                                    }
                                    Text(
                                        text = "Long-press bnContact to add a new contact.",
                                        color = Color.DarkGray,
                                        fontSize = (12 * scale).sp
                                    )
                                }
                            }
                        }
                    } else if (isItemList) {
                        val fieldGap = (4 * scale).dp
                        val visibleItemListFieldLabels = itemListFormFieldLabels
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                visibleItemListFieldLabels.chunked(2).forEach { labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEach { label ->
                                            val fieldLabel =
                                                if (label == "itProduct") "pdName" else label
                                            val fieldIndex = itemListFieldLabels.indexOf(fieldLabel)
                                            EntryListOutlinedField(
                                                value = itemFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "itID" || label == "itTotal",
                                                selectionOnly = label == "itProduct",
                                                clearable = label == "itUnit",
                                                suggestionOptions = if (label == "itProduct") {
                                                    (
                                                        parseProductListNames(itemProductLines) +
                                                            listOfNotNull(
                                                                itemFields[fieldIndex]
                                                                    .takeIf(String::isNotBlank)
                                                            )
                                                    ).distinctBy { it.lowercase(Locale.ROOT) }
                                                } else if (label == "itUnit") {
                                                    filterItemUnitOptions(
                                                        parseUnitListDisplayOptions(itemUnitLines),
                                                        itemFields[fieldIndex]
                                                    )
                                                } else {
                                                    null
                                                },
                                                pickerEmptyLabel = when (label) {
                                                    "itProduct" -> {
                                                    when {
                                                        itemProductLoadError.isNotBlank() ->
                                                            "Products unavailable"
                                                        itemProductSha.isBlank() ->
                                                            "Loading products..."
                                                        parseProductListNames(itemProductLines)
                                                            .isEmpty() -> "No products available"
                                                        else -> null
                                                    }
                                                    }
                                                    "itUnit" -> {
                                                        when {
                                                            itemUnitLoadError.isNotBlank() ->
                                                                "Units unavailable"
                                                            itemUnitSha.isBlank() ->
                                                                "Loading units..."
                                                            parseUnitListDisplayOptions(itemUnitLines)
                                                                .isEmpty() -> "No units available"
                                                            else -> null
                                                        }
                                                    }
                                                    else -> null
                                                },
                                                onLongClick = when (label) {
                                                    "itProduct" -> {
                                                        {
                                                        productFields = newProductListFields()
                                                        itemProductError = ""
                                                        showItemProductEditor = true
                                                        }
                                                    }
                                                    "itUnit" -> {
                                                        {
                                                            unitItemLines = questions
                                                            unitItemLoadError = ""
                                                            unitFields = newUnitListFields()
                                                            itemUnitError = ""
                                                            showItemUnitEditor = true
                                                        }
                                                    }
                                                    else -> null
                                                },
                                                onValueChange = { value ->
                                                    itemFields = itemFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                        this[5] = calculateItemListTotal(
                                                            this[3],
                                                            this[4],
                                                            this[2],
                                                            itemUnitLines
                                                        ).orEmpty()
                                                    }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                    }
                                }
                                if (itemFields[3].isNotBlank() || itemFields[4].isNotBlank()) {
                                    if (itemFields[5].isBlank()) {
                                        Text(
                                            text = "Enter a valid rate and quantity; the selected unit must have a numeric utValue.",
                                            color = Color(0xFFB42318),
                                            fontSize = (12 * scale).sp
                                        )
                                    }
                                }
                                if (itemProductLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not load ProductList: $itemProductLoadError",
                                        color = Color(0xFFB42318),
                                        fontSize = (12 * scale).sp
                                    )
                                }
                                if (itemUnitLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not load UnitList: $itemUnitLoadError",
                                        color = Color(0xFFB42318),
                                        fontSize = (12 * scale).sp
                                    )
                                }
                            }
                        }
                    } else if (isUnitList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                unitListFieldLabels.chunked(2).forEach { labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEach { label ->
                                            val fieldIndex = unitListFieldLabels.indexOf(label)
                                            EntryListOutlinedField(
                                                value = unitFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "utUV" || label == "utCount",
                                                suggestionOptions = if (label == "utName") {
                                                    (
                                                        parseUnitListNames(questions) +
                                                            listOfNotNull(
                                                                unitFields[fieldIndex]
                                                                    .takeIf(String::isNotBlank)
                                                            )
                                                    ).distinctBy { it.lowercase(Locale.ROOT) }
                                                } else {
                                                    null
                                                },
                                                onValueChange = { value ->
                                                    unitFields = unitFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                        this[2] = unitListDisplayValue(
                                                            this[0],
                                                            this[1]
                                                        )
                                                        this[3] = countUnitUsage(
                                                            unitItemLines,
                                                            this[2]
                                                        ).toString()
                                                    }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                        if (labels.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                                Text(
                                    text = "Enter utName manually or long-press its dropdown to choose an existing unit name. utUV is generated; utCount counts ItemList rows using that utUV.",
                                    color = Color.DarkGray,
                                    fontSize = (12 * scale).sp
                                )
                                if (unitItemLoadError.isNotBlank()) {
                                    Text(
                                        text = "Could not calculate utCount: $unitItemLoadError",
                                        color = Color(0xFFB42318),
                                        fontSize = (12 * scale).sp
                                    )
                                }
                                if (isDuplicateUnitDisplayValue(
                                        questions,
                                        unitFields[2],
                                        editingIndex
                                    )
                                ) {
                                    Text(
                                        text = "This utUV already exists. Each unit display value must be unique.",
                                        color = Color(0xFFB42318),
                                        fontSize = (12 * scale).sp
                                    )
                                }
                            }
                        }
                    } else if (isProductList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                productListFieldLabels.chunked(2).forEach { labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEach { label ->
                                            val fieldIndex =
                                                productListFieldLabels.indexOf(label)
                                            EntryListOutlinedField(
                                                value = productFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "pdID",
                                                onValueChange = { value ->
                                                    productFields =
                                                        productFields.toMutableList().apply {
                                                            this[fieldIndex] = value
                                                        }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                        if (labels.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isUserList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (2.5f * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                userListFieldLabels.chunked(2).forEach { labels ->
                                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                        val userFieldWidth =
                                            ((maxWidth - fieldGap) / 2).coerceAtMost(
                                                (200 * scale).dp
                                            )
                                        Row(
                                            modifier = Modifier
                                                .width(userFieldWidth * 2 + fieldGap)
                                                .align(Alignment.Center),
                                            horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                        ) {
                                            labels.forEach { label ->
                                                val fieldIndex =
                                                    userListFieldLabels.indexOf(label)
                                                if (fieldIndex == 5) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(userFieldWidth)
                                                            .height((62 * scale).dp)
                                                            .semantics {
                                                                contentDescription = label
                                                            }
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .border(
                                                                    BorderStroke(
                                                                        (2 * scale).dp,
                                                                        Color.Black
                                                                    ),
                                                                    RoundedCornerShape(50)
                                                                )
                                                                .padding(
                                                                    horizontal = (10 * scale).dp
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Switch(
                                                                checked = userFields[5] == "On",
                                                                enabled = !isWorking,
                                                                onCheckedChange = { enabled ->
                                                                    userFields =
                                                                        userFields.toMutableList()
                                                                            .apply {
                                                                                this[5] =
                                                                                    if (enabled) {
                                                                                        "On"
                                                                                    } else {
                                                                                        "Off"
                                                                                    }
                                                                            }
                                                                    errorMessage = ""
                                                                    saveSucceeded = false
                                                                    statusMessage = ""
                                                                },
                                                                modifier = Modifier.graphicsLayer {
                                                                    scaleX = 0.8f
                                                                    scaleY = 0.8f
                                                                }
                                                            )
                                                        }
                                                        Text(
                                                            text = label,
                                                            color = Color.Black,
                                                            fontSize = (12 * scale).sp,
                                                            lineHeight = (14 * scale).sp,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            modifier = Modifier
                                                                .align(Alignment.TopCenter)
                                                                .offset(y = (-15 * scale).dp)
                                                                .background(Color.White)
                                                                .padding(horizontal = (4 * scale).dp)
                                                        )
                                                    }
                                                } else {
                                                    EntryListOutlinedField(
                                                        value = userFields[fieldIndex],
                                                        label = label,
                                                        scale = scale,
                                                        modifier = Modifier.width(userFieldWidth),
                                                        topPadding = false,
                                                        readOnly = fieldIndex == 1 &&
                                                            editingIndex != null,
                                                        trailingPicker = fieldIndex == 0 ||
                                                            fieldIndex == 2,
                                                        trailingPickerEnabled = !isWorking,
                                                        keyboardType = if (fieldIndex == 2) {
                                                            KeyboardType.Number
                                                        } else if (fieldIndex == 0) {
                                                            KeyboardType.Email
                                                        } else {
                                                            KeyboardType.Text
                                                        },
                                                        autofillEmail = fieldIndex == 0,
                                                        focusRequester = if (fieldIndex == 0) {
                                                            userEmailFocusRequester
                                                        } else {
                                                            null
                                                        },
                                                        typeOptions = if (fieldIndex == 4) {
                                                            userListRoleOptions
                                                        } else {
                                                            null
                                                        },
                                                        onPickTrailing = {
                                                            if (fieldIndex == 0) {
                                                                chooseUserEmail()
                                                            } else {
                                                                showUserMobileSourcePicker = true
                                                            }
                                                        },
                                                        onValueChange = { value ->
                                                            userFields =
                                                                userFields.toMutableList().apply {
                                                                    this[fieldIndex] = value
                                                                }
                                                            errorMessage = ""
                                                            saveSucceeded = false
                                                            statusMessage = ""
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isFileList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                fileListFieldLabels.take(2).chunked(2).forEach { labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEach { label ->
                                            val fieldIndex =
                                                fileListFieldLabels.indexOf(label)
                                            EntryListOutlinedField(
                                                value = fileFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "flID",
                                                onValueChange = { value ->
                                                    fileFields =
                                                        fileFields.toMutableList().apply {
                                                            this[fieldIndex] = value
                                                        }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                        if (labels.size == 1) {
                                            Spacer(Modifier.weight(1f))
                                        }
                                    }
                                }
                                FileAttachmentPreviewBox(
                                    path = fileFields[2],
                                    attachment = fileAttachmentPreview,
                                    isLoading = isLoadingFilePreview,
                                    scale = scale
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        enabled = !isWorking &&
                                            parseFileAttachmentPath(fileFields[2]) != null,
                                        onClick = {
                                            scope.launch {
                                                isWorking = true
                                                errorMessage = ""
                                                statusMessage = ""
                                                try {
                                                    val readToken =
                                                        publicApiKey() ?: return@launch
                                                    val (attachment, message) =
                                                        withContext(Dispatchers.IO) {
                                                            downloadPrivateFileAttachment(
                                                                BuildConfig.RUBELAPP_PROXY_URL,
                                                                BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                readToken,
                                                                fileFields[2]
                                                            )
                                                        }
                                                    if (attachment == null) {
                                                        errorMessage = message
                                                    } else {
                                                        pendingFileDownload = attachment
                                                        saveFileAttachmentPicker.launch(
                                                            attachment.name
                                                        )
                                                    }
                                                } catch (exception: SecurityException) {
                                                    Log.e("FileListDownload", "Android denied saving the attachment.", exception)
                                                    errorMessage =
                                                        "Android did not allow the downloaded file to be saved."
                                                } finally {
                                                    isWorking = false
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Download")
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        enabled = !isWorking,
                                        onClick = {
                                            try {
                                                fileAttachmentPicker.launch(arrayOf("*/*"))
                                            } catch (exception: android.content.ActivityNotFoundException) {
                                                Log.e("FileListUpload", "No file picker is available.", exception)
                                                errorMessage = "No file picker is available on this device."
                                            } catch (exception: SecurityException) {
                                                Log.e("FileListUpload", "Android denied opening the file picker.", exception)
                                                errorMessage = "Android did not allow the file picker to open."
                                            }
                                        }
                                    ) {
                                        Text("Upload File Replace")
                                    }
                                }
                                Text(
                                    text = "Images, CSV, PDF, audio, video, and other files up to 5 MiB are supported.",
                                    color = Color.DarkGray,
                                    fontSize = (12 * scale).sp
                                )
                            }
                        }
                    } else if (isContactList) {
                        val fieldGap = (4 * scale).dp
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = (8 * scale).dp),
                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                            ) {
                                contactListFieldLabels.chunked(2).forEachIndexed { rowIndex, labels ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                    ) {
                                        labels.forEachIndexed { columnIndex, label ->
                                            val fieldIndex = rowIndex * 2 + columnIndex
                                            EntryListOutlinedField(
                                                value = contactFields[fieldIndex],
                                                label = label,
                                                scale = scale,
                                                modifier = Modifier.weight(1f),
                                                readOnly = label == "cnID",
                                                phonePicker = label == "cnMobile",
                                                typeOptions = if (label == "cnType") {
                                                    contactListAllTypeOptions
                                                } else {
                                                    null
                                                },
                                                suggestionOptions = when (label) {
                                                    "cnGroup", "cnArea" ->
                                                        contactListColumnOptions(
                                                            if (showBankContactEditor) {
                                                                contactListLines
                                                            } else {
                                                                questions
                                                            },
                                                            fieldIndex
                                                        )
                                                    else -> null
                                                },
                                                onPickPhone = {
                                                    try {
                                                        contactPhonePicker.launch(
                                                            android.content.Intent(
                                                                android.content.Intent.ACTION_PICK,
                                                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                                            )
                                                        )
                                                    } catch (exception: android.content.ActivityNotFoundException) {
                                                        Log.e("ContactPicker", "No phone contacts picker is available.", exception)
                                                        val message =
                                                            "No contacts app is available to select a phone number."
                                                        if (showBankContactEditor) {
                                                            bankContactError = message
                                                        } else {
                                                            errorMessage = message
                                                        }
                                                    } catch (exception: SecurityException) {
                                                        Log.e("ContactPicker", "Android denied opening the contacts picker.", exception)
                                                        val message =
                                                            "Android did not allow the contacts picker to open."
                                                        if (showBankContactEditor) {
                                                            bankContactError = message
                                                        } else {
                                                            errorMessage = message
                                                        }
                                                    }
                                                },
                                                onValueChange = { value ->
                                                    contactFields = contactFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                    }
                                                    errorMessage = ""
                                                    saveSucceeded = false
                                                    statusMessage = ""
                                                }
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Type cnGroup/cnArea manually, or tap/long-press the right side of the field to choose an existing value. Type a phone number, or tap/long-press the right side of the phone field to choose one from your phone contacts.",
                                    color = Color.DarkGray,
                                    fontSize = (12 * scale).sp
                                )
                            }
                        }
                    } else if (isRawLineList) {
                        BasicTextField(
                            value = questionText,
                            onValueChange = {
                                questionText = it
                                saveSucceeded = false
                                statusMessage = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = (96 * scale).dp)
                                .background(Color.White, RoundedCornerShape((8 * scale).dp))
                                .border(
                                    BorderStroke((1 * scale).dp, Color.LightGray),
                                    RoundedCornerShape((8 * scale).dp)
                                )
                                .padding((12 * scale).dp),
                            textStyle = TextStyle(color = Color.Black, fontSize = (16 * scale).sp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (questionText.isEmpty()) {
                                        Text("Entry", color = Color.DarkGray)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        BasicTextField(
                            value = questionText,
                            onValueChange = {
                                questionText = it
                                saveSucceeded = false
                                statusMessage = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = (120 * scale).dp)
                                .background(Color.White, RoundedCornerShape((8 * scale).dp))
                                .border(
                                    BorderStroke((1 * scale).dp, Color.LightGray),
                                    RoundedCornerShape((8 * scale).dp)
                                )
                                .padding((12 * scale).dp)
                                .semantics { contentDescription = "qnQuestion" },
                            textStyle = TextStyle(color = Color.Black, fontSize = (16 * scale).sp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (questionText.isEmpty()) {
                                        Text("qnQuestion", color = Color.DarkGray)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Text(
                            text = "=",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = (6 * scale).dp),
                            color = Color.Black,
                            fontSize = (20 * scale).sp,
                            textAlign = TextAlign.Center
                        )
                        BasicTextField(
                            value = answerText,
                            onValueChange = {
                                answerText = it
                                saveSucceeded = false
                                statusMessage = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = (88 * scale).dp)
                                .background(Color.White, RoundedCornerShape((8 * scale).dp))
                                .border(
                                    BorderStroke((1 * scale).dp, Color.LightGray),
                                    RoundedCornerShape((8 * scale).dp)
                                )
                                .padding((12 * scale).dp)
                                .semantics { contentDescription = "qnAnswer" },
                            textStyle = TextStyle(color = Color.Black, fontSize = (16 * scale).sp),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (answerText.isEmpty()) {
                                        Text("qnAnswer", color = Color.DarkGray)
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    }
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFB42318),
                            modifier = Modifier.padding(top = (8 * scale).dp)
                        )
                    } else if (saveSucceeded) {
                        Text(
                            text = "Save Success",
                            color = submitGreen,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = (8 * scale).dp)
                        )
                    }
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        enabled = !isWorking,
                        onClick = { showEditor = false }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.weight(1f))
                TextButton(
                    enabled = !isWorking && !saveSucceeded &&
                        (!isItemList || itemFields[0].isNotBlank()) &&
                        (!isUnitList || !isDuplicateUnitDisplayValue(
                            questions,
                            unitFields[2],
                            editingIndex
                        )) &&
                        (if (isEntryList) {
                            entryItemSha.isNotBlank() &&
                                entryItemLoadError.isBlank() &&
                                calculateEntryItemTotal(entryFields[0], entryItemLines) != null &&
                                listOf(0, 1, 3, 4).all { entryFields[it].isNotBlank() }
                        } else if (isContactList) {
                            contactFields[0].isNotBlank() &&
                                contactFields[1].isNotBlank() &&
                                contactFields[2] in contactListAllTypeOptions
                        } else if (isBankList) {
                            bankFields[0].isNotBlank() && bankFields[1].isNotBlank()
                        } else if (isProductList) {
                            productFields[0].isNotBlank() &&
                                productFields[1].isNotBlank()
                        } else if (isUserList) {
                            userFields.take(4).all(String::isNotBlank) &&
                                userFields[5] in setOf("", "On", "Off")
                        } else if (isItemList) {
                            itemFields[1].isNotBlank() &&
                                itemFields[3].isNotBlank() &&
                                itemFields[4].isNotBlank() &&
                                itemFields[5].isNotBlank()
                        } else if (isUnitList) {
                            unitItemLoadError.isBlank() &&
                                unitFields.all(String::isNotBlank) &&
                                unitFields[3].trim().toBigDecimalOrNull() != null
                        } else if (isFileList) {
                            fileFields[0].isNotBlank() &&
                                fileFields[2].isNotBlank()
                        } else if (isRawLineList) {
                            questionText.isNotBlank()
                        } else {
                            questionText.isNotBlank()
                        }) && fileSha.isNotBlank(),
                    onClick = {
                        val cashBankTotal = if (isEntryList) {
                            calculateEntryCashBankTotal(entryFields[8], entryFields[9])
                        } else {
                            null
                        }
                        val entryItemTotal = if (isEntryList) {
                            calculateEntryItemTotal(entryFields[0], entryItemLines)
                        } else {
                            null
                        }
                        if (isEntryList && cashBankTotal == null) {
                            entryFormError = "enCash and enBank must contain valid numbers."
                            return@TextButton
                        }
                        if (isEntryList && entryItemSha.isBlank()) {
                            entryFormError = entryItemLoadError
                                .takeIf(String::isNotBlank)
                                ?: "ItemList is still loading. Refresh and try again."
                            return@TextButton
                        }
                        if (isEntryList && entryItemTotal == null) {
                            entryFormError =
                                "Could not calculate enITotal: a matching ItemList row has an invalid itTotal."
                            return@TextButton
                        }
                        val questionEntry = if (isEntryList) {
                            val fieldsToSave = entryFields.toMutableList().apply {
                                this[11] = cashBankTotal.orEmpty()
                                this[17] = entryItemTotal.orEmpty()
                            }
                            buildEntryListLine(fieldsToSave, entryTypeOptions)?.also {
                                entryFormError = ""
                            } ?: run {
                                entryFormError =
                                    "Fill required fields, select enType from TypeList, and do not use commas."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val contactEntry = if (isContactList) {
                            buildContactListLine(contactFields) ?: run {
                                errorMessage =
                                    "Enter cnID and cnName, select a valid cnType, and do not use commas in fields."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val bankEntry = if (isBankList) {
                            buildBankListLine(bankFields) ?: run {
                                errorMessage =
                                    "Enter bnID and bnName, and do not use commas in bank fields."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val productEntry = if (isProductList) {
                            buildProductListLine(productFields) ?: run {
                                errorMessage =
                                    "Enter pdID and pdName, and do not use commas in product fields."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val userEntry = if (isUserList) {
                            buildUserListLine(userFields) ?: run {
                                errorMessage =
                                    "Fill in every UserList field with a valid usEMail and do not use commas."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val itemEntry = if (isItemList) {
                            buildItemListLine(itemFields) ?: run {
                                errorMessage =
                                    "Enter itProduct, itRate, and itQty; if itUnit is selected, it must have a numeric utValue."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val unitEntry = if (isUnitList) {
                            buildUnitListLine(unitFields) ?: run {
                                errorMessage =
                                    "Enter utName, utValue, and a numeric utCount; utUV is generated automatically."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        if (isUnitList && isDuplicateUnitDisplayValue(
                                questions,
                                unitFields[2],
                                editingIndex
                            )
                        ) {
                            errorMessage = "This utUV already exists. Each unit display value must be unique."
                            return@TextButton
                        }
                        val fileEntry = if (isFileList) {
                            buildFileListLine(fileFields) ?: run {
                                errorMessage =
                                    "Enter flID and flData, and do not use commas in file fields."
                                return@TextButton
                            }
                        } else {
                            ""
                        }
                        val cleanLineEntry = questionText.trim()
                        if (isRawLineList && !isContactList && !isBankList &&
                            !isProductList && !isUserList && !isItemList && !isUnitList &&
                            !isFileList &&
                            (cleanLineEntry.isBlank() ||
                                '\n' in cleanLineEntry ||
                                '\r' in cleanLineEntry)
                        ) {
                            errorMessage = "Enter one non-empty line."
                            return@TextButton
                        }
                        val cleanQuestion = questionText
                            .replace('\r', ' ')
                            .replace('\n', ' ')
                            .trim()
                        val cleanAnswer = answerText
                            .replace('\r', ' ')
                            .replace('\n', ' ')
                            .trim()
                        if (!isLineList && containsDuplicateQuestionAnswer(
                                questions,
                                cleanQuestion,
                                cleanAnswer,
                                excludedIndex = editingIndex
                            )
                        ) {
                            showDuplicateQuestionAlert = true
                            return@TextButton
                        }
                        val questionEntryToSave = when {
                            isEntryList -> questionEntry
                            isContactList -> contactEntry
                            isBankList -> bankEntry
                            isProductList -> productEntry
                            isUserList -> userEntry
                            isItemList -> itemEntry
                            isUnitList -> unitEntry
                            isFileList -> fileEntry
                            isRawLineList -> cleanLineEntry
                            else -> "$cleanQuestion=$cleanAnswer"
                        }
                        val updatedQuestions = questions.toMutableList()
                        val index = editingIndex
                        if (index == null) {
                            updatedQuestions.add(questionEntryToSave)
                        } else {
                            updatedQuestions[index] = questionEntryToSave
                        }
                        scope.launch {
                            isWorking = true
                            errorMessage = ""
                            statusMessage = ""
                            try {
                                val writeToken = publicApiKey()
                                    ?: return@launch
                                val result = withContext(Dispatchers.IO) {
                                    savePrivateQuestionFile(
                                        BuildConfig.RUBELAPP_PROXY_URL,
                                        BuildConfig.RUBELAPP_PROXY_API_KEY,
                                        writeToken,
                                        fileName,
                                        displayName,
                                        fileSha,
                                        when {
                                            isEntryList ->
                                                serializeEntryListLines(updatedQuestions)
                                            isProductList ->
                                                serializeProductListLines(updatedQuestions)
                                            isUserList ->
                                                serializeUserListLines(updatedQuestions)
                                            isFileList ->
                                                serializeFileListLines(updatedQuestions)
                                            else -> updatedQuestions
                                        }
                                    )
                                }
                                if (result.success && result.fileSha.isNotBlank()) {
                                    questions = updatedQuestions
                                    fileSha = result.fileSha
                                    questionText = ""
                                    answerText = ""
                                    val nextEntryTime = Date()
                                    contactFields = if (isContactList) {
                                        newContactListFields(formatEntryListId(nextEntryTime))
                                    } else {
                                        List(contactListFieldLabels.size) { "" }
                                    }
                                    bankFields = if (isBankList) {
                                        listOf(formatEntryListId(nextEntryTime), "") +
                                            List(5) { "" }
                                    } else {
                                        List(bankListFieldLabels.size) { "" }
                                    }
                                    productFields = if (isProductList) {
                                        newProductListFields()
                                    } else {
                                        List(productListFieldLabels.size) { "" }
                                    }
                                    userFields = if (isUserList) {
                                        newUserListFields()
                                    } else {
                                        List(userListFieldLabels.size) { "" }
                                    }
                                    itemFields = if (isItemList) {
                                        newItemListFields(itemFields[0])
                                    } else {
                                        List(itemListFieldLabels.size) { "" }
                                    }
                                    unitFields = if (isUnitList) {
                                        newUnitListFields()
                                    } else {
                                        List(unitListFieldLabels.size) { "" }
                                    }
                                    fileFields = if (isFileList) {
                                        newFileListFields(questionSearch.trim())
                                    } else {
                                        List(fileListFieldLabels.size) { "" }
                                    }
                                    fileAttachmentPreview = null
                                    fileAttachmentPreviewPath = ""
                                    entryFields = if (isEntryList) {
                                        val loginEmail = context.getSharedPreferences(
                                            PREFERENCES_NAME,
                                            android.content.Context.MODE_PRIVATE
                                        ).getString(SAVED_LOGIN_EMAIL_KEY, "").orEmpty()
                                        newEntryListFields(nextEntryTime, loginEmail)
                                    } else {
                                        List(18) { "" }
                                    }
                                    entryTrackError = ""
                                    saveSucceeded = true
                                    statusMessage = "Save Success"
                                } else {
                                    errorMessage = result.message
                                        .takeIf(String::isNotBlank)
                                        ?: "Could not verify the saved question file. Refresh before editing."
                                }
                            } finally {
                                isWorking = false
                            }
                        }
                    }
                ) {
                    Text(
                        when {
                            isWorking -> "Saving..."
                            saveSucceeded -> "Save Success"
                            isContactList -> "Save Contact"
                            isBankList -> "Save Bank"
                            isProductList -> "Save Product"
                            isUserList -> "Save User"
                            isItemList -> "Save Item"
                            isUnitList -> "Save Unit"
                            isFileList -> "Save File"
                            else -> "Save"
                        }
                    )
                }
                }
            },
            dismissButton = {}
        )
    }
    if (showUserMobileSourcePicker) {
        AlertDialog(
            onDismissRequest = { showUserMobileSourcePicker = false },
            title = { Text("Choose phone number source") },
            text = {
                Column {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showUserMobileSourcePicker = false
                            try {
                                contactPhonePicker.launch(
                                    android.content.Intent(
                                        android.content.Intent.ACTION_PICK,
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                    )
                                )
                            } catch (exception: android.content.ActivityNotFoundException) {
                                Log.e("UserList", "No phone contacts picker is available.", exception)
                                errorMessage =
                                    "No contacts app is available to select a phone number."
                            } catch (exception: SecurityException) {
                                Log.e("UserList", "Android denied opening the contacts picker.", exception)
                                errorMessage = "Android did not allow the contacts picker to open."
                            }
                        }
                    ) {
                        Text("Phone Book")
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showUserMobileSourcePicker = false
                            chooseUserSimNumber()
                        }
                    ) {
                        Text("SIM Number")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserMobileSourcePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showUserSimPicker) {
        AlertDialog(
            onDismissRequest = { showUserSimPicker = false },
            title = { Text("Choose a SIM phone number") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                    userSimNumbers.forEachIndexed { index, sim ->
                        TextButton(
                            onClick = {
                                userFields = userFields.toMutableList().apply {
                                    this[2] = sim.number
                                }
                                showUserSimPicker = false
                                errorMessage = ""
                                saveSucceeded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(sim.label)
                                Text(sim.number, fontSize = (13 * scale).sp)
                            }
                        }
                        if (index < userSimNumbers.lastIndex) {
                            Spacer(Modifier.height((2 * scale).dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserSimPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showEntryBankEditor) {
                            AlertDialog(
                                modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(),
                                properties = DialogProperties(usePlatformDefaultWidth = false),
                                onDismissRequest = { if (!isSavingEntryBank) showEntryBankEditor = false },
                                title = { Text("Add Bank", color = Color.Black) },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .background(Color.White)
                                            .heightIn(max = (480 * scale).dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        val fieldGap = (4 * scale).dp
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = (8 * scale).dp),
                                                verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                                            ) {
                                                bankListFieldLabels.chunked(2).forEach { labels ->
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                                    ) {
                                                        labels.forEach { label ->
                                                            val fieldIndex = bankListFieldLabels.indexOf(label)
                                                            EntryListOutlinedField(
                                                                value = bankFields[fieldIndex],
                                                                label = label,
                                                                scale = scale,
                                                                modifier = Modifier.weight(1f),
                                                                readOnly = label == "bnID",
                                                                typeOptions = if (label == "bnContact") {
                                                                    (
                                                                        entryContactNames +
                                                                            listOfNotNull(
                                                                                bankFields[fieldIndex]
                                                                                    .takeIf(String::isNotBlank)
                                                                            )
                                                                        ).distinct()
                                                                } else {
                                                                    null
                                                                },
                                                                pickerEmptyLabel = if (label == "bnContact") {
                                                                    when {
                                                                        !entryContactsLoaded -> "Loading contacts..."
                                                                        entryContactLoadError.isNotBlank() ->
                                                                            "Contacts unavailable"
                                                                        entryContactNames.isEmpty() ->
                                                                            "No contacts available"
                                                                        else -> null
                                                                    }
                                                                } else {
                                                                    null
                                                                },
                                                                onLongClick = if (label == "bnContact") {
                                                                    {
                                                                        contactCreationEntryFieldIndex = null
                                                                        bankContactFields = newContactListFields(
                                                                            formatEntryListId(Date())
                                                                        )
                                                                        bankContactError = ""
                                                                        showBankContactEditor = true
                                                                    }
                                                                } else {
                                                                    null
                                                                },
                                                                onValueChange = { value ->
                                                                    bankFields = bankFields.toMutableList().apply {
                                                                        this[fieldIndex] = value
                                                                    }
                                                                    bankContactError = ""
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "Long-press bnContact to add a new contact.",
                                                    color = Color.DarkGray,
                                                    fontSize = (12 * scale).sp
                                                )
                                                if (bankContactError.isNotBlank()) {
                                                    Text(
                                                        text = bankContactError,
                                                        color = Color(0xFFB42318),
                                                        modifier = Modifier.padding(top = (8 * scale).dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                },
                                containerColor = Color.White,
                                titleContentColor = Color.Black,
                                textContentColor = Color.Black,
                                confirmButton = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                    TextButton(
                                        enabled = !isSavingEntryBank,
                                        onClick = { showEntryBankEditor = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                    Spacer(Modifier.weight(1f))
                                    TextButton(
                                        enabled = !isSavingEntryBank &&
                                            bankFields[0].isNotBlank() &&
                                            bankFields[1].isNotBlank() &&
                                            entryBankSha.isNotBlank(),
                                        onClick = {
                                            val bankLine = buildBankListLine(bankFields)
                                            if (bankLine == null) {
                                                bankContactError =
                                                    "Enter bnID and bnName, and do not use commas in bank fields."
                                            } else if (entryBankSha.isBlank()) {
                                                bankContactError =
                                                    "BankList is not loaded. Refresh and try again."
                                            } else {
                                                scope.launch {
                                                    isSavingEntryBank = true
                                                    bankContactError = ""
                                                    try {
                                                        val writeToken = publicApiKey()
                                                        if (writeToken == null) {
                                                            bankContactError =
                                                                "Admin sign-in is required. Please sign in again."
                                                            return@launch
                                                        }
                                                        val updatedBanks = entryBankLines + bankLine
                                                        val result = withContext(Dispatchers.IO) {
                                                            savePrivateQuestionFile(
                                                                BuildConfig.RUBELAPP_PROXY_URL,
                                                                BuildConfig.RUBELAPP_PROXY_API_KEY,
                                                                writeToken,
                                                                "BankList",
                                                                "Bank List",
                                                                entryBankSha,
                                                                updatedBanks
                                                            )
                                                        }
                                                        if (result.success && result.fileSha.isNotBlank()) {
                                                            entryBankLines = updatedBanks
                                                            entryBankSha = result.fileSha
                                                            entryFields = entryFields.toMutableList().apply {
                                                                this[10] = bankFields[1].trim()
                                                            }
                                                            entryFormError = ""
                                                            showEntryBankEditor = false
                                                        } else {
                                                            bankContactError = result.message
                                                                .takeIf(String::isNotBlank)
                                                                ?: "Could not verify the saved BankList. Refresh and try again."
                                                        }
                                                    } finally {
                                                        isSavingEntryBank = false
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text(if (isSavingEntryBank) "Saving..." else "Save Bank")
                                    }
                                    }
                                },
                                dismissButton = {}
                            )
                        }
    if (showItemProductEditor) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = {
                if (!isSavingItemProduct) showItemProductEditor = false
            },
            title = { Text("Add Product", color = Color.Black) },
            text = {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .heightIn(max = (480 * scale).dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val fieldGap = (4 * scale).dp
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (8 * scale).dp),
                            verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                        ) {
                            productListFieldLabels.chunked(2).forEach { labels ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                ) {
                                    labels.forEach { label ->
                                        val fieldIndex =
                                            productListFieldLabels.indexOf(label)
                                        EntryListOutlinedField(
                                            value = productFields[fieldIndex],
                                            label = label,
                                            scale = scale,
                                            modifier = Modifier.weight(1f),
                                            readOnly = label == "pdID",
                                            onValueChange = { value ->
                                                productFields =
                                                    productFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                    }
                                                itemProductError = ""
                                            }
                                        )
                                    }
                                    if (labels.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                            if (itemProductError.isNotBlank()) {
                                Text(
                                    text = itemProductError,
                                    color = Color(0xFFB42318),
                                    modifier = Modifier.padding(top = (8 * scale).dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                TextButton(
                    enabled = !isSavingItemProduct,
                    onClick = { showItemProductEditor = false }
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    enabled = !isSavingItemProduct &&
                        productFields[0].isNotBlank() &&
                        productFields[1].isNotBlank() &&
                        itemProductSha.isNotBlank(),
                    onClick = {
                        val productLine = buildProductListLine(productFields)
                        if (productLine == null) {
                            itemProductError =
                                "Enter pdID and pdName, and do not use commas in product fields."
                        } else if (itemProductSha.isBlank()) {
                            itemProductError =
                                "ProductList is not loaded. Refresh and try again."
                        } else {
                            scope.launch {
                                isSavingItemProduct = true
                                itemProductError = ""
                                try {
                                    val writeToken = publicApiKey()
                                    if (writeToken == null) {
                                        itemProductError =
                                            "Admin sign-in is required. Please sign in again."
                                        return@launch
                                    }
                                    val updatedProducts = itemProductLines + productLine
                                    val result = withContext(Dispatchers.IO) {
                                        savePrivateQuestionFile(
                                            BuildConfig.RUBELAPP_PROXY_URL,
                                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                                            writeToken,
                                            "ProductList",
                                            "Product List",
                                            itemProductSha,
                                            serializeProductListLines(updatedProducts)
                                        )
                                    }
                                    if (result.success && result.fileSha.isNotBlank()) {
                                        itemProductLines = updatedProducts
                                        itemProductSha = result.fileSha
                                        if (isItemList) {
                                            itemFields = itemFields.toMutableList().apply {
                                                this[1] = productFields[1].trim()
                                            }
                                        }
                                        showItemProductEditor = false
                                    } else {
                                        itemProductError = result.message
                                            .takeIf(String::isNotBlank)
                                            ?: "Could not verify the saved ProductList. Refresh and try again."
                                    }
                                } finally {
                                    isSavingItemProduct = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSavingItemProduct) "Saving..." else "Save Product")
                }
                }
            },
            dismissButton = {}
        )
    }
    if (showItemUnitEditor) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { if (!isSavingItemUnit) showItemUnitEditor = false },
            title = { Text("Add Unit", color = Color.Black) },
            text = {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .heightIn(max = (480 * scale).dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    unitListFieldLabels.forEach { label ->
                        val fieldIndex = unitListFieldLabels.indexOf(label)
                        EntryListOutlinedField(
                            value = unitFields[fieldIndex],
                            label = label,
                            scale = scale,
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = label == "utUV" || label == "utCount",
                            onValueChange = { value ->
                                unitFields = unitFields.toMutableList().apply {
                                    this[fieldIndex] = value
                                    this[2] = unitListDisplayValue(this[0], this[1])
                                    this[3] = countUnitUsage(
                                        unitItemLines,
                                        this[2]
                                    ).toString()
                                }
                                itemUnitError = ""
                            }
                        )
                    }
                    if (itemUnitLoadError.isNotBlank()) {
                        Text(
                            text = "Could not load UnitList: $itemUnitLoadError",
                            color = Color(0xFFB42318),
                            modifier = Modifier.padding(top = (8 * scale).dp)
                        )
                    }
                    if (itemUnitError.isNotBlank()) {
                        Text(
                            text = itemUnitError,
                            color = Color(0xFFB42318),
                            modifier = Modifier.padding(top = (8 * scale).dp)
                        )
                    } else if (isDuplicateUnitDisplayValue(itemUnitLines, unitFields[2])) {
                        Text(
                            text = "This utUV already exists. Enter a different unit name or value.",
                            color = Color(0xFFB42318),
                            modifier = Modifier.padding(top = (8 * scale).dp)
                        )
                    }
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                TextButton(
                    enabled = !isSavingItemUnit,
                    onClick = { showItemUnitEditor = false }
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    enabled = !isSavingItemUnit &&
                        unitFields[0].isNotBlank() &&
                        unitFields[1].isNotBlank() &&
                        unitFields[3].trim().toBigDecimalOrNull() != null &&
                        !isDuplicateUnitDisplayValue(itemUnitLines, unitFields[2]) &&
                        itemUnitSha.isNotBlank(),
                    onClick = {
                        val unitLine = buildUnitListLine(unitFields)
                        if (unitLine == null) {
                            itemUnitError =
                                "Enter utName, utValue, and a numeric utCount; utUV is generated automatically."
                        } else if (isDuplicateUnitDisplayValue(itemUnitLines, unitFields[2])) {
                            itemUnitError =
                                "This utUV already exists. Enter a different unit name or value."
                        } else if (itemUnitSha.isBlank()) {
                            itemUnitError = "UnitList is not loaded. Refresh and try again."
                        } else {
                            scope.launch {
                                isSavingItemUnit = true
                                itemUnitError = ""
                                try {
                                    val writeToken = publicApiKey()
                                    if (writeToken == null) {
                                        itemUnitError =
                                            "Admin sign-in is required. Please sign in again."
                                        return@launch
                                    }
                                    val updatedUnits = itemUnitLines + unitLine
                                    val result = withContext(Dispatchers.IO) {
                                        savePrivateQuestionFile(
                                            BuildConfig.RUBELAPP_PROXY_URL,
                                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                                            writeToken,
                                            "UnitList",
                                            "Unit List",
                                            itemUnitSha,
                                            updatedUnits
                                        )
                                    }
                                    if (result.success && result.fileSha.isNotBlank()) {
                                        itemUnitLines = updatedUnits
                                        itemUnitSha = result.fileSha
                                        itemFields = itemFields.toMutableList().apply {
                                            this[2] = unitFields[2].trim()
                                            this[5] = calculateItemListTotal(
                                                this[3],
                                                this[4],
                                                this[2],
                                                updatedUnits
                                            ).orEmpty()
                                        }
                                        showItemUnitEditor = false
                                    } else {
                                        itemUnitError = result.message
                                            .takeIf(String::isNotBlank)
                                            ?: "Could not verify the saved UnitList. Refresh and try again."
                                    }
                                } finally {
                                    isSavingItemUnit = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSavingItemUnit) "Saving..." else "Save Unit")
                }
                }
            },
            dismissButton = {}
        )
    }
    if (showBankContactEditor) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 2.dp).fillMaxWidth(),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { if (!isSavingBankContact) showBankContactEditor = false },
            title = { Text("Add Contact", color = Color.Black) },
            text = {
                Column(
                    modifier = Modifier
                        .background(Color.White)
                        .heightIn(max = (480 * scale).dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val fieldGap = (4 * scale).dp
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = (8 * scale).dp),
                            verticalArrangement = Arrangement.spacedBy((10 * scale).dp)
                        ) {
                            contactListFieldLabels.chunked(2).forEachIndexed { rowIndex, labels ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(fieldGap)
                                ) {
                                    labels.forEachIndexed { columnIndex, label ->
                                        val fieldIndex = rowIndex * 2 + columnIndex
                                        EntryListOutlinedField(
                                            value = bankContactFields[fieldIndex],
                                            label = label,
                                            scale = scale,
                                            modifier = Modifier.weight(1f),
                                            readOnly = label == "cnID",
                                            phonePicker = label == "cnMobile",
                                            typeOptions = if (label == "cnType") {
                                                contactListAllTypeOptions
                                            } else {
                                                null
                                            },
                                            suggestionOptions = when (label) {
                                                "cnGroup", "cnArea" ->
                                                    contactListColumnOptions(
                                                        questions,
                                                        fieldIndex
                                                    )
                                                else -> null
                                            },
                                            onPickPhone = {
                                                try {
                                                    contactPhonePicker.launch(
                                                        android.content.Intent(
                                                            android.content.Intent.ACTION_PICK,
                                                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                                        )
                                                    )
                                                } catch (exception: android.content.ActivityNotFoundException) {
                                                    Log.e(
                                                        "ContactPicker",
                                                        "No phone contacts picker is available.",
                                                        exception
                                                    )
                                                    bankContactError =
                                                        "No contacts app is available to select a phone number."
                                                } catch (exception: SecurityException) {
                                                    Log.e(
                                                        "ContactPicker",
                                                        "Android denied opening the contacts picker.",
                                                        exception
                                                    )
                                                    bankContactError =
                                                        "Android did not allow the contacts picker to open."
                                                }
                                            },
                                            onValueChange = { value ->
                                                bankContactFields =
                                                    bankContactFields.toMutableList().apply {
                                                        this[fieldIndex] = value
                                                    }
                                                bankContactError = ""
                                            }
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "Type cnGroup/cnArea manually, or tap/long-press the right side of the field to choose an existing value. Type a phone number, or tap/long-press the right side of the phone field to choose one from your phone contacts.",
                                color = Color.DarkGray,
                                fontSize = (12 * scale).sp
                            )
                            if (bankContactError.isNotBlank()) {
                                Text(
                                    text = bankContactError,
                                    color = Color(0xFFB42318),
                                    modifier = Modifier.padding(top = (8 * scale).dp)
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color.White,
            titleContentColor = Color.Black,
            textContentColor = Color.Black,
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                TextButton(
                    enabled = !isSavingBankContact,
                    onClick = { showBankContactEditor = false }
                ) {
                    Text("Cancel")
                }
                Spacer(Modifier.weight(1f))
                TextButton(
                    enabled = !isSavingBankContact &&
                        bankContactFields[0].isNotBlank() &&
                        bankContactFields[1].isNotBlank() &&
                        bankContactFields[2] in contactListAllTypeOptions &&
                        contactListSha.isNotBlank(),
                    onClick = {
                        val contactLine = buildContactListLine(bankContactFields)
                        if (contactLine == null) {
                            bankContactError =
                                "Enter cnID and cnName, select a valid cnType, and do not use commas in fields."
                        } else if (contactListSha.isBlank()) {
                            bankContactError =
                                "ContactList is not loaded. Refresh and try again."
                        } else {
                            scope.launch {
                                isSavingBankContact = true
                                bankContactError = ""
                                try {
                                    val writeToken = publicApiKey()
                                    if (writeToken == null) {
                                        bankContactError =
                                            "Admin sign-in is required. Please sign in again."
                                        return@launch
                                    }
                                    val updatedContacts = contactListLines + contactLine
                                    val result = withContext(Dispatchers.IO) {
                                        savePrivateQuestionFile(
                                            BuildConfig.RUBELAPP_PROXY_URL,
                                            BuildConfig.RUBELAPP_PROXY_API_KEY,
                                            writeToken,
                                            "ContactList",
                                            "Contact List",
                                            contactListSha,
                                            updatedContacts
                                        )
                                    }
                                    if (result.success && result.fileSha.isNotBlank()) {
                                        contactListLines = updatedContacts
                                        contactListSha = result.fileSha
                                        entryContactNames =
                                            parseEntryContactNames(updatedContacts)
                                        entryContactsLoaded = true
                                        val entryContactFieldIndex =
                                            contactCreationEntryFieldIndex
                                        if (entryContactFieldIndex != null) {
                                            entryFields = entryFields.toMutableList().apply {
                                                this[entryContactFieldIndex] =
                                                    bankContactFields[1].trim()
                                            }
                                            entryFormError = ""
                                        } else {
                                            bankFields = bankFields.toMutableList().apply {
                                                this[3] = bankContactFields[1].trim()
                                            }
                                        }
                                        showBankContactEditor = false
                                    } else {
                                        bankContactError = result.message
                                            .takeIf(String::isNotBlank)
                                            ?: "Could not verify the saved ContactList. Refresh and try again."
                                    }
                                } finally {
                                    isSavingBankContact = false
                                }
                            }
                        }
                    }
                ) {
                    Text(if (isSavingBankContact) "Saving..." else "Save Contact")
                }
                }
            },
            dismissButton = {}
        )
    }
    if (showDuplicateQuestionAlert) {
        AlertDialog(
            onDismissRequest = { showDuplicateQuestionAlert = false },
            title = { Text("Duplicate Question") },
            text = { Text("This Question=Answer already exists. Duplicate entries are not allowed.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateQuestionAlert = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun EntryListOutlinedField(
    value: String,
    label: String,
    scale: Float,
    modifier: Modifier = Modifier,
    topPadding: Boolean = true,
    typeOptions: List<String>? = null,
    suggestionOptions: List<String>? = null,
    pickerEmptyLabel: String? = null,
    readOnly: Boolean = false,
    selectionOnly: Boolean = false,
    clearable: Boolean = false,
    datePicker: Boolean = false,
    phonePicker: Boolean = false,
    onPickPhone: () -> Unit = {},
    trailingPicker: Boolean = false,
    trailingPickerEnabled: Boolean = true,
    onPickTrailing: () -> Unit = {},
    inputPlaceholder: String? = null,
    keyboardType: KeyboardType? = null,
    autofillEmail: Boolean = false,
    focusRequester: FocusRequester? = null,
    onLongClick: (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    val fieldShape = RoundedCornerShape(50)
    val hasPickerAction =
        typeOptions != null || suggestionOptions != null || datePicker ||
            phonePicker || trailingPicker
    val pickerTextInset = when {
        typeOptions != null || suggestionOptions != null || trailingPicker -> 24
        phonePicker || datePicker -> 20
        else -> 0
    }
    var showOptions by remember { mutableStateOf(false) }
    val context = LocalContext.current
    fun showDatePicker() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.isLenient = false
        val parsePosition = java.text.ParsePosition(0)
        val parsedDate = dateFormat.parse(value, parsePosition)
            ?.takeIf { parsePosition.index == value.length }
        val selectedDate = parsedDate ?: Date()
        val calendar = java.util.Calendar.getInstance().apply {
            time = selectedDate
        }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenDate = java.util.Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onValueChange(dateFormat.format(chosenDate.time))
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show()
    }
    Box(
        modifier = modifier
            .height((62 * scale).dp)
            .padding(
                top = if (typeOptions != null || !topPadding) {
                    0.dp
                } else {
                    (8 * scale).dp
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(fieldShape)
                .border(BorderStroke((2 * scale).dp, Color.Black), fieldShape)
                .padding(horizontal = if (hasPickerAction) 0.dp else (12 * scale).dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (typeOptions == null) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly || selectionOnly || datePicker,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (focusRequester != null) {
                                Modifier.focusRequester(focusRequester)
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (autofillEmail) {
                                Modifier.semantics {
                                    contentType = ContentType.EmailAddress
                                }
                            } else {
                                Modifier
                            }
                        )
                        .then(
                            if (pickerTextInset > 0) {
                                Modifier.padding(
                                    horizontal = (pickerTextInset * scale).dp
                                )
                            } else {
                                Modifier
                            }
                        ),
                    keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType ?: if (
                                label in setOf(
                                    "cnOBalance",
                                    "enCash",
                                    "enBank",
                                    "enDiscount",
                                    "enLabor",
                                    "enRent",
                                    "pdOStock",
                                    "pdBPrice",
                                    "pdSPrice",
                                    "utValue",
                                    "utCount",
                                    "itRate",
                                    "itQty",
                                    "itTotal"
                                )
                            ) {
                            KeyboardType.Number
                        } else {
                            KeyboardType.Text
                        }
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = (if (trailingPicker) 15 * scale else 14 * scale).sp,
                        fontWeight = if (trailingPicker) {
                            FontWeight.SemiBold
                        } else {
                            FontWeight.Normal
                        },
                        textAlign = TextAlign.Center
                    )
                )
                if (inputPlaceholder != null && value.isBlank()) {
                    Text(
                        text = inputPlaceholder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = (pickerTextInset * scale).dp),
                        color = Color.Gray,
                        fontSize = (14 * scale).sp,
                        textAlign = TextAlign.Center
                    )
                }
                if (datePicker) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size((48 * scale).dp)
                            .offset(x = (8 * scale).dp)
                            .combinedClickable(
                                onClick = ::showDatePicker,
                                onLongClick = ::showDatePicker
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = (26 * scale).sp)
                    }
                }
                if (phonePicker) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size((48 * scale).dp)
                            .offset(x = (8 * scale).dp)
                            .combinedClickable(
                                onClick = onPickPhone,
                                onLongClick = onPickPhone
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = (26 * scale).sp)
                    }
                }
                if (trailingPicker) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size((48 * scale).dp)
                            .offset(x = (8 * scale).dp)
                            .combinedClickable(
                                enabled = trailingPickerEnabled,
                                onClick = onPickTrailing,
                                onLongClick = onPickTrailing
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = (26 * scale).sp)
                    }
                }
                if (suggestionOptions != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size((48 * scale).dp)
                            .offset(x = (8 * scale).dp)
                            .then(
                                if (clearable && value.isNotEmpty()) {
                                    Modifier.offset(x = (-20 * scale).dp)
                                } else {
                                    Modifier
                                }
                            )
                            .combinedClickable(
                                onClick = { showOptions = true },
                                onLongClick = {
                                    if (onLongClick != null) {
                                        onLongClick()
                                    } else {
                                        showOptions = true
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = (26 * scale).sp)
                    }
                    DropdownMenu(
                        expanded = showOptions,
                        onDismissRequest = { showOptions = false }
                    ) {
                        if (suggestionOptions.isEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        pickerEmptyLabel
                                            ?: "No sectors for the selected type"
                                    )
                                },
                                enabled = false,
                                onClick = {}
                            )
                        } else {
                            suggestionOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        onValueChange(option)
                                        showOptions = false
                                    }
                                )
                            }
                        }
                    }
                    if (clearable && value.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size((24 * scale).dp)
                                .combinedClickable(
                                    onClick = { onValueChange("") },
                                    onLongClick = { onValueChange("") }
                                )
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isNotBlank()) {
                        Text(
                            text = value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (13 * scale).dp)
                                .padding(
                                    horizontal = 0.dp
                                ),
                            color = Color.Black,
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size((48 * scale).dp)
                            .offset(x = (8 * scale).dp)
                            .combinedClickable(
                                onClick = { showOptions = true },
                                onLongClick = {
                                    if (onLongClick != null) {
                                        onLongClick()
                                    } else {
                                        showOptions = true
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.Black, fontSize = (26 * scale).sp)
                    }
                }
                DropdownMenu(
                    expanded = showOptions,
                    onDismissRequest = { showOptions = false }
                ) {
                    if (pickerEmptyLabel != null || typeOptions.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(pickerEmptyLabel ?: "No options available")
                            },
                            enabled = false,
                            onClick = {}
                        )
                    }
                    typeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                showOptions = false
                            }
                        )
                    }
                }
            }
        }
        Text(
            text = label,
            color = Color.Black,
            fontSize = (12 * scale).sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-7 * scale).dp)
                .background(Color.White)
                .padding(horizontal = (4 * scale).dp)
        )
    }
}

@Composable
private fun ExamScreen(
    examName: String,
    questions: List<ExamQuestion>,
    onBackToDashboard: () -> Unit,
    onReloadQuestions: () -> Unit,
    pageTitle: String = examName,
    stateKey: String = examName,
    backContentDescription: String = "Back to dashboard"
) {
    val context = LocalContext.current
    val scale = rememberResponsiveScale()
    val frameShape = RoundedCornerShape((22 * scale).dp)
    val preferences = remember(context) {
        context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
    }
    val markedRows = remember(preferences, stateKey, questions) {
        val savedRows = preferences.getStringSet("read_questions_$stateKey", emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
        mutableStateMapOf<Int, Boolean>().apply {
            questions.forEach { question -> put(question.row, question.row in savedRows) }
        }
    }
    var filter by remember(preferences, stateKey) {
        mutableStateOf(
            when (preferences.getString("exam_filter_$stateKey", null)) {
                "Read" -> ExamQuestionFilter.Read
                "Unread" -> ExamQuestionFilter.Unread
                else -> ExamQuestionFilter.All
            }
        )
    }
    val questionFontSize = remember(preferences, stateKey) {
        preferences.getInt("exam_font_size_$stateKey", 14).coerceIn(14, 18)
    }
    var questionTextScale by remember(preferences, stateKey) {
        mutableStateOf(
            preferences.getFloat("exam_text_scale_$stateKey", 1f).coerceIn(0.7f, 2f)
        )
    }
    val currentQuestionTextScale by rememberUpdatedState(questionTextScale)
    var showQuestionAnswers by remember(preferences, stateKey) {
        mutableStateOf(preferences.getBoolean("exam_show_answers_$stateKey", true))
    }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = preferences
            .getInt("exam_scroll_index_$stateKey", 0)
            .coerceAtLeast(0),
        initialFirstVisibleItemScrollOffset = preferences
            .getInt("exam_scroll_offset_$stateKey", 0)
            .coerceAtLeast(0)
    )
    val visibleQuestions = when (filter) {
        ExamQuestionFilter.All -> questions
        ExamQuestionFilter.Read -> questions.filter { markedRows[it.row] == true }
        ExamQuestionFilter.Unread -> questions.filter { markedRows[it.row] != true }
    }

    LaunchedEffect(listState, preferences, stateKey) {
        snapshotFlow {
            listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                preferences.edit()
                    .putInt("exam_scroll_index_$stateKey", index)
                    .putInt("exam_scroll_offset_$stateKey", offset)
                    .apply()
            }
    }

    fun setRead(question: ExamQuestion, read: Boolean) {
        markedRows[question.row] = read
        val savedRows = markedRows.filterValues { it }
            .keys
            .mapTo(mutableSetOf()) { it.toString() }
        preferences.edit().putStringSet("read_questions_$stateKey", savedRows).apply()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding((14 * scale).dp)
                .clip(frameShape)
                .border(BorderStroke((2 * scale).dp, titleBlue), frameShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((56 * scale).dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = (10 * scale).dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExamFilterButton(
                        text = "↻",
                        contentDescription = "Reload questions",
                        color = titleBlue,
                        selected = false,
                        scale = scale,
                        onClick = onReloadQuestions
                    )
                    Text(
                        text = pageTitle,
                        color = Color(0xFF198C3A),
                        fontFamily = FontFamily.Serif,
                        fontSize = (16 * scale).sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (0.2 * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = (8 * scale).dp)
                    )
                    ExamFilterButton(
                        text = "All",
                        color = titleBlue,
                        selected = filter == ExamQuestionFilter.All,
                        scale = scale,
                        onClick = {
                            filter = ExamQuestionFilter.All
                            preferences.edit().putString("exam_filter_$stateKey", "All").apply()
                        }
                    )
                    ExamFilterButton(
                        text = "×",
                        contentDescription = "Show unread questions",
                        color = Color(0xFFD92332),
                        selected = filter == ExamQuestionFilter.Unread,
                        scale = scale,
                        onClick = {
                            filter = ExamQuestionFilter.Unread
                            preferences.edit().putString("exam_filter_$stateKey", "Unread").apply()
                        }
                    )
                    ExamFilterButton(
                        text = "✓",
                        contentDescription = "Show read questions",
                        color = Color(0xFF159A50),
                        selected = filter == ExamQuestionFilter.Read,
                        scale = scale,
                        onClick = {
                            filter = ExamQuestionFilter.Read
                            preferences.edit().putString("exam_filter_$stateKey", "Read").apply()
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height((2 * scale).dp)
                        .background(titleBlue)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(stateKey) {
                            awaitEachGesture {
                                val firstDown = awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial
                                )
                                val textColumnStart = 0f
                                val textColumnEnd = size.width * 0.9f
                                if (
                                    firstDown.position.x in textColumnStart..textColumnEnd
                                ) {
                                    var previousSpan = 0f
                                    var gestureScale = currentQuestionTextScale
                                    do {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val pointers = event.changes.filter { it.pressed }
                                        if (pointers.size >= 2) {
                                            val centroid = pointers
                                                .fold(Offset.Zero) { sum, pointer ->
                                                    sum + pointer.position
                                                } / pointers.size.toFloat()
                                            val span = pointers
                                                .map {
                                                    (it.position - centroid).getDistance()
                                                }
                                                .average()
                                                .toFloat()
                                            if (previousSpan > 0f && span > 0f) {
                                                val zoom = span / previousSpan
                                                if (abs(zoom - 1f) > 0.005f) {
                                                    gestureScale =
                                                        (gestureScale * zoom).coerceIn(0.7f, 2f)
                                                    questionTextScale = gestureScale
                                                    preferences.edit()
                                                        .putFloat(
                                                            "exam_text_scale_$stateKey",
                                                            gestureScale
                                                        )
                                                        .apply()
                                                }
                                            }
                                            previousSpan = span
                                            pointers.forEach { it.consume() }
                                        } else {
                                            previousSpan = 0f
                                        }
                                    } while (event.changes.any { it.pressed })
                                }
                            }
                        },
                    state = listState
                ) {
                    if (visibleQuestions.isEmpty()) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = when {
                                        questions.isEmpty() -> "No data found."
                                        filter == ExamQuestionFilter.Read -> "No read questions."
                                        else -> "No unread questions."
                                    },
                                    color = Color.Gray,
                                    fontSize = (13 * scale).sp,
                                    textAlign = TextAlign.Justify,
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .padding(
                                            start = (6 * scale).dp,
                                            end = (6 * scale).dp,
                                            top = (12 * scale).dp
                                        )
                                )
                                Spacer(Modifier.weight(0.1f))
                            }
                        }
                    } else {
                        items(visibleQuestions, key = { it.row }) { question ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = (48 * scale).dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .padding(
                                            start = (6 * scale).dp,
                                            end = (6 * scale).dp,
                                            top = (7 * scale).dp
                                        )
                                ) {
                                    val questionParts =
                                        question.question.split("=", limit = 2)
                                    val questionText = if (questionParts.size == 2) {
                                        if (showQuestionAnswers) {
                                            "${questionParts[0].trim()}\n= ${questionParts[1].trim()}"
                                        } else {
                                            questionParts[0].trim()
                                        }
                                    } else {
                                        question.question
                                    }
                                    SelectionContainer {
                                        Column {
                                            Text(
                                                text = "${question.row - 1}. $questionText",
                                                color = Color.Black,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = (
                                                    questionFontSize * questionTextScale * scale
                                                ).sp,
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = (
                                                    (questionFontSize + 4) * questionTextScale * scale
                                                ).sp,
                                                textAlign = TextAlign.Justify,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .semantics {
                                                        contentDescription = "qnQuestion"
                                                    }
                                            )
                                            if (showQuestionAnswers &&
                                                question.answer.isNotBlank()
                                            ) {
                                                Text(
                                                    text = "= ${question.answer}",
                                                    color = Color.Black,
                                                    fontFamily = FontFamily.Serif,
                                                    fontSize = (
                                                        (questionFontSize - 1) *
                                                            questionTextScale * scale
                                                    ).sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    lineHeight = (
                                                        (questionFontSize + 3) *
                                                            questionTextScale * scale
                                                    ).sp
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height((4 * scale).dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height((1 * scale).dp)
                                            .background(Color.DarkGray)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(0.1f)
                                        .padding(top = (6 * scale).dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    ExamFilterButton(
                                        text = if (markedRows[question.row] == true) "✓" else "×",
                                        contentDescription = if (markedRows[question.row] == true) {
                                            "Mark as unread"
                                        } else {
                                            "Mark as read"
                                        },
                                        color = if (markedRows[question.row] == true) {
                                            Color(0xFF159A50)
                                        } else {
                                            Color(0xFFD92332)
                                        },
                                        selected = true,
                                        scale = scale,
                                        onClick = {
                                            setRead(question, markedRows[question.row] != true)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Spacer(Modifier.weight(0.9f))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width((2 * scale).dp)
                            .background(titleBlue)
                    )
                    Spacer(Modifier.weight(0.1f))
                }
            }
            val markedCount = markedRows.values.count { it }
            val unreadCount = questions.size - markedCount
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = (5 * scale).dp)
                    .height((42 * scale).dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .border(
                        BorderStroke((2 * scale).dp, titleBlue),
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = (6 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExamFilterButton(
                    text = "←",
                    contentDescription = backContentDescription,
                    color = titleBlue,
                    selected = false,
                    scale = scale,
                    onClick = onBackToDashboard
                )
                ExamCountItem(
                    icon = "All",
                    count = questions.size,
                    label = "All questions",
                    color = titleBlue,
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
                ExamCountItem(
                    icon = "×",
                    count = unreadCount,
                    label = "Unmarked questions",
                    color = Color(0xFFD92332),
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
                ExamCountItem(
                    icon = "✓",
                    count = markedCount,
                    label = "Marked questions",
                    color = Color(0xFF159A50),
                    scale = scale,
                    modifier = Modifier.weight(1f)
                )
                ExamFilterButton(
                    text = if (showQuestionAnswers) "◉" else "⊘",
                    contentDescription = if (showQuestionAnswers) {
                        "Hide question answers"
                    } else {
                        "Show question answers"
                    },
                    color = titleBlue,
                    selected = showQuestionAnswers,
                    scale = scale,
                    onClick = {
                        showQuestionAnswers = !showQuestionAnswers
                        preferences.edit()
                            .putBoolean("exam_show_answers_$stateKey", showQuestionAnswers)
                            .apply()
                    }
                )
            }
        }
    }
}

private enum class ExamQuestionFilter {
    All,
    Read,
    Unread
}

@Composable
private fun ExamCountItem(
    icon: String,
    count: Int,
    label: String,
    color: Color,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.semantics { contentDescription = "$label: $count" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size((26 * scale).dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(BorderStroke((1.5 * scale).dp, color), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                color = color,
                fontSize = ((if (icon == "All") 9f else 18f) * scale).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Spacer(Modifier.width((5 * scale).dp))
        Text(
            text = count.toString(),
            color = Color.Black,
            fontSize = (14 * scale).sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExamFilterButton(
    text: String,
    contentDescription: String = text,
    color: Color,
    selected: Boolean,
    scale: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = (2 * scale).dp)
            .size((30 * scale).dp)
            .clip(CircleShape)
            .background(if (selected) color else Color.White)
            .border(BorderStroke((1.5 * scale).dp, color), CircleShape)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else color,
            fontSize = ((if (text == "All") 9f else 18f) * scale).sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun RegistrationScreen(onBackToLogin: () -> Unit) {
    val context = LocalContext.current
    var generatedCode by remember {
        mutableStateOf(generateRegistrationCode())
    }
    val androidId = remember(context) {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
    }
    val values = remember(androidId) {
        mutableStateListOf(generatedCode, androidId, "", "", "", "", "", "", "")
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var registrationResultMessage by remember { mutableStateOf("") }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var isRecovering by remember { mutableStateOf(false) }
    var recoveryEmailSent by remember { mutableStateOf(false) }
    var recoveryRecipientEmail by remember { mutableStateOf("") }
    var simNumbers by remember { mutableStateOf(emptyList<SimPhoneNumber>()) }
    var showSimPicker by remember { mutableStateOf(false) }
    var allowManualMobile by remember { mutableStateOf(false) }
    val scale = rememberResponsiveScale()
    val phonePermissions = remember {
        buildList {
            add(Manifest.permission.READ_PHONE_STATE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(Manifest.permission.READ_PHONE_NUMBERS)
            }
        }
    }

    fun clearRegistrationForm() {
        generatedCode = generateRegistrationCode()
        values.indices.forEach { values[it] = "" }
        values[0] = generatedCode
        values[1] = androidId
        values[6] = ""
        allowManualMobile = false
    }

    fun loadSimNumbers() {
        try {
            val subscriptions = context.getSystemService(SubscriptionManager::class.java)
                ?.activeSubscriptionInfoList.orEmpty()
            simNumbers = subscriptions.map { subscription ->
                SimPhoneNumber(
                    label = subscription.carrierName?.toString()
                        ?.takeIf(String::isNotBlank)
                        ?: subscription.displayName?.toString()
                        ?: "SIM ${subscription.simSlotIndex + 1}",
                    number = subscription.number.orEmpty()
                )
            }.filter { it.number.isNotBlank() }
            if (simNumbers.isNotEmpty()) {
                showSimPicker = true
            } else {
                allowManualMobile = true
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "This SIM does not provide its phone number. Enter it manually."
                    )
                }
            }
        } catch (exception: SecurityException) {
            allowManualMobile = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    exception.localizedMessage ?: "Permission is required to read the SIM number."
                )
            }
        }
    }

    val phonePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            loadSimNumbers()
        } else {
            allowManualMobile = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Phone permission was denied. Enter the mobile number manually."
                )
            }
        }
    }

    val allowPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = phonePermissions.all { permissions[it] == true }
        scope.launch {
            snackbarHostState.showSnackbar(
                if (allGranted) {
                    "Phone permissions granted."
                } else {
                    "Some permissions were denied. You can enter mobile manually."
                }
            )
        }
    }

    val accountPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val email = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!email.isNullOrBlank()) {
                values[4] = email.trim()
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Could not read the selected email account.")
                }
            }
        }
    }

    fun chooseEmailAccount() {
        try {
            accountPicker.launch(
                AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    arrayOf("com.google"),
                    false,
                    "Choose an email account",
                    null,
                    null,
                    null
                )
            )
        } catch (exception: android.content.ActivityNotFoundException) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "No email account picker is available. Enter the email manually."
                )
            }
        }
    }

    fun chooseSimNumber() {
        val missingPermissions = phonePermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isEmpty()) {
            loadSimNumbers()
        } else {
            phonePermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
                .padding(horizontal = (20 * scale).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((56 * scale).dp)
                    .padding(top = (8 * scale).dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .semantics { contentDescription = "Back to login" }
                ) {
                    Text("←", fontSize = (24 * scale).sp, color = titleBlue)
                }
                Text(
                    text = "Register Form",
                    color = titleBlue,
                    fontSize = (26 * scale).sp,
                    lineHeight = (32 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = (20 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            Spacer(Modifier.height((28 * scale).dp))

            Column(verticalArrangement = Arrangement.spacedBy((16 * scale).dp)) {
                registrationFields
                    .filterNot { it.id == "rgDevice" }
                    .chunked(2)
                    .forEach { rowFields ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)
                    ) {
                        rowFields.forEach { field ->
                            val valueIndex = field.valueIndex
                            RegistrationField(
                                value = values[valueIndex],
                                fieldId = field.id,
                                placeholder = field.label,
                                onValueChange = { values[valueIndex] = it },
                                readOnly = field.id in setOf("rgDevice", "rgRole", "rgSwitch"),
                                onClick = when (field.id) {
                                    "rgMobile" ->
                                        if (allowManualMobile) null else ::chooseSimNumber
                                    else -> null
                                },
                                trailingPicker = field.id in setOf("rgMail", "rgMobile"),
                                onPickTrailing = when (field.id) {
                                    "rgMail" -> ::chooseEmailAccount
                                    "rgMobile" -> ::chooseSimNumber
                                    else -> null
                                },
                                autofillEmail = field.id == "rgMail",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowFields.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }

                }
            }

            Spacer(Modifier.height((28 * scale).dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)
            ) {
                ActionButton(
                    text = "Clear",
                    color = submitGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    clearRegistrationForm()
                }
                ActionButton(
                    text = "Submit",
                    color = submitGreen,
                    modifier = Modifier.weight(1f),
                    enabled = !isSubmitting
                ) {
                    when {
                        registrationFields.any { field ->
                            field.id !in setOf("rgRole", "rgSwitch") &&
                                values[field.valueIndex].isBlank()
                        } -> scope.launch {
                            snackbarHostState.showSnackbar(
                                "Fill in rgMail, rgDevice, rgMobile, and rgName."
                            )
                        }
                        !isValidEmail(values[4]) -> scope.launch {
                            snackbarHostState.showSnackbar("Enter a valid email address.")
                        }
                        !isValidBangladeshiMobile(values[3]) -> scope.launch {
                            snackbarHostState.showSnackbar("Enter a valid mobile number.")
                        }
                        else -> scope.launch {
                            isSubmitting = true
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    submitRegistration(
                                        listOf(
                                            values[4],
                                            values[1],
                                            values[3],
                                            values[5],
                                            "",
                                            ""
                                        )
                                    )
                                }
                                if (result.success) {
                                    registrationResultMessage = result.message
                                    clearRegistrationForm()
                                    showSuccessDialog = true
                                } else if (result.duplicate) {
                                    recoveryEmailSent = false
                                    recoveryRecipientEmail = ""
                                    showDuplicateDialog = true
                                } else {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height((12 * scale).dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy((14 * scale).dp)
            ) {
                ActionButton(
                    text = "Accept",
                    color = submitGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    val missingPermissions = phonePermissions.filter {
                        ContextCompat.checkSelfPermission(
                            context,
                            it
                        ) != PackageManager.PERMISSION_GRANTED
                    }
                    if (missingPermissions.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Phone permissions are already allowed.")
                        }
                    } else {
                        allowPermissionsLauncher.launch(missingPermissions.toTypedArray())
                    }
                }
                Spacer(Modifier.weight(1f))
            }
        }
    }
    }

    CenterNoticeHost(snackbarHostState)

    if (showSimPicker) {
        AlertDialog(
            onDismissRequest = { showSimPicker = false },
            title = { Text("Choose a SIM phone number") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                    simNumbers.forEachIndexed { index, sim ->
                        TextButton(
                            onClick = {
                                values[3] = sim.number
                                showSimPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(sim.label)
                                Text(sim.number, fontSize = (13 * scale).sp)
                            }
                        }
                        if (index < simNumbers.lastIndex) {
                            Spacer(Modifier.height((2 * scale).dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSimPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text(registrationResultMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isRecovering) showDuplicateDialog = false
            },
            icon = {
                Text(
                    text = if (recoveryEmailSent) "✓" else "!",
                    color = if (recoveryEmailSent) submitGreen else Color(0xFFB45309),
                    fontSize = (26 * scale).sp,
                    modifier = Modifier
                        .background(
                            if (recoveryEmailSent) Color(0xFFEAF5E3) else Color(0xFFFFF3E0),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = (16 * scale).dp, vertical = (8 * scale).dp)
                )
            },
            title = {
                Text(
                    text = if (recoveryEmailSent) "Recovery email sent" else "Device already registered",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = if (recoveryEmailSent) {
                        if (recoveryRecipientEmail.isNotBlank()) {
                            "We sent your registration code to this email address: $recoveryRecipientEmail. If it does not arrive, check Spam/Junk."
                        } else {
                            "If this device is registered, a recovery email request was processed. Check Spam/Junk and confirm you used the registered device if nothing arrives."
                        }
                    } else {
                        "This device is already registered. Tap Forgot to email the registration code to the saved email address."
                    },
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoveryEmailSent) {
                            showDuplicateDialog = false
                        } else if (!isRecovering) {
                            scope.launch {
                                isRecovering = true
                                val result = sendRecoveryCode()
                                isRecovering = false
                                if (result.success && result.recoveryRecipientEmail.isNotBlank()) {
                                    recoveryEmailSent = true
                                    recoveryRecipientEmail = result.recoveryRecipientEmail
                                } else if (result.success) {
                                    snackbarHostState.showSnackbar(
                                        "If this device is registered, a recovery email request was processed. If nothing arrives, verify this device's registration email."
                                    )
                                } else {
                                    snackbarHostState.showSnackbar(result.message)
                                }
                            }
                        }
                    },
                    enabled = !isRecovering,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (recoveryEmailSent) submitGreen else titleBlue
                    )
                ) {
                    Text(
                        when {
                            isRecovering -> "Sending..."
                            recoveryEmailSent -> "Done"
                            else -> "Forgot"
                        }
                    )
                }
            },
            dismissButton = {
                if (!recoveryEmailSent) {
                    TextButton(
                        onClick = { showDuplicateDialog = false },
                        enabled = !isRecovering
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun RegistrationField(
    value: String,
    fieldId: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClick: (() -> Unit)? = null,
    trailingPicker: Boolean = false,
    onPickTrailing: (() -> Unit)? = null,
    autofillEmail: Boolean = false,
    readOnly: Boolean = false,
    isSwitch: Boolean = false,
    switchValue: Boolean = false,
    onSwitchChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scale = rememberResponsiveScale()
    val displayLabel = placeholder
    val keyboardType = when (fieldId) {
        "rgMobile" -> KeyboardType.Phone
        "rgMail" -> KeyboardType.Email
        else -> KeyboardType.Text
    }

    Box(
        modifier = modifier
            .height((58 * scale).dp)
            .semantics { contentDescription = fieldId },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height((50 * scale).dp)
                .border(BorderStroke((1.5 * scale).dp, Color.Black), RoundedCornerShape(50))
                .padding(
                    start = (14 * scale).dp,
                    end = ((14 + if (trailingPicker) 36 else 0) * scale).dp
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isSwitch) {
                Switch(
                    checked = switchValue,
                    onCheckedChange = onSwitchChange,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .semantics { contentDescription = fieldId }
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(color = Color.Black, fontSize = (15 * scale).sp),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    readOnly = readOnly || onClick != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (autofillEmail) {
                                Modifier.semantics {
                                    contentType = ContentType.EmailAddress
                                }
                            } else {
                                Modifier
                            }
                        )
                )
            }
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(onClick = onClick)
                        .semantics { contentDescription = "Choose $fieldId" }
                )
            }
        }
        if (trailingPicker && onPickTrailing != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((48 * scale).dp)
                    .clickable(onClick = onPickTrailing)
                    .semantics { contentDescription = "Choose $fieldId" },
                contentAlignment = Alignment.Center
            ) {
                Text("+", color = Color.Black, fontSize = (26 * scale).sp)
            }
        }
        Text(
            text = displayLabel,
            color = Color.Black,
            fontSize = (15 * scale).sp,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.White)
                .padding(horizontal = (4 * scale).dp)
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    leadingIcon: Painter? = null,
    onClick: () -> Unit
) {
    val scale = rememberResponsiveScale()

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(((if (compact) 40 else 50) * scale).dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        if (leadingIcon == null) {
            Text(
                text = text,
                fontFamily = FontFamily.Serif,
                fontSize = ((if (compact) 18 else 21) * scale).sp,
                letterSpacing = scale.sp,
                textAlign = TextAlign.Center
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(
                    painter = leadingIcon,
                    contentDescription = "WhatsApp",
                    modifier = Modifier.size((20 * scale).dp)
                )
                Spacer(Modifier.width((6 * scale).dp))
                Text(
                    text = text,
                    fontFamily = FontFamily.Serif,
                    fontSize = ((if (compact) 18 else 21) * scale).sp,
                    letterSpacing = scale.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}