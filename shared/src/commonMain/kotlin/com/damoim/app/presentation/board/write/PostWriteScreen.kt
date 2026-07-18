package com.damoim.app.presentation.board.write

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BoardCategory
import com.damoim.app.domain.model.BoardPost
import com.damoim.app.domain.model.DraftDocFile
import com.damoim.app.domain.model.DraftImage
import com.damoim.app.domain.model.DraftLink
import com.damoim.app.domain.model.PollDraft
import com.damoim.app.domain.model.PostAttachment
import com.damoim.app.domain.model.PostDraft
import com.damoim.app.domain.model.RecruitDraft
import com.damoim.app.platform.PlatformBackHandler
import com.damoim.app.platform.rememberCameraLauncher
import com.damoim.app.platform.rememberDocumentPickerLauncher
import com.damoim.app.presentation.board.boardCategoryLabel
import com.damoim.app.presentation.component.CalendarIcon
import com.damoim.app.presentation.component.CameraIcon
import com.damoim.app.presentation.component.ChartIcon
import com.damoim.app.presentation.component.ChevronDownIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.ImageIcon
import com.damoim.app.presentation.component.ImageStore
import com.damoim.app.presentation.component.LinkIcon
import com.damoim.app.presentation.component.NetworkImage
import com.damoim.app.presentation.component.LockIcon
import com.damoim.app.presentation.component.PaperclipIcon
import com.damoim.app.presentation.component.PersonPlusIcon
import com.damoim.app.presentation.component.PlusIcon
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.image.picker.toImageBitmap
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private sealed interface WriteSheet {
    data object Category : WriteSheet
    data object Attach : WriteSheet
    data object PollDeadline : WriteSheet
    data object RecruitDeadline : WriteSheet
}

/**
 * 화면 15/34/35/39/70 게시글 작성/수정 — Route. [editPostId]가 있으면 수정 모드(프리필).
 * 사진(갤러리·카메라)/문서 첨부는 실제 플랫폼 피커로 동작하고, 임시저장하면 다음 작성 때 복원된다.
 */
@Composable
fun PostWriteRoute(
    initialCategory: BoardCategory = BoardCategory.FREE,
    editPostId: Long? = null,
    viewModel: PostWriteViewModel = viewModel(key = "postwrite_${editPostId ?: "new"}") {
        PostWriteViewModel(AppGraph.submitPostUseCase, AppGraph.getPostDetailUseCase, AppGraph.observeMyContextUseCase, editPostId)
    },
    onCancel: () -> Unit = {},
    onDone: (edited: Boolean) -> Unit = { _ -> },
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    // 새 작성 진입(컴포지션)마다 임시저장 초안을 읽는다 — VM은 재사용될 수 있으므로 init이 아닌 여기서
    val restoredDraft = remember(editPostId) { if (editPostId == null) viewModel.loadDraft() else null }
    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PostWriteSideEffect.Toast -> onToast(effect.message)
                PostWriteSideEffect.Done -> onDone(editPostId != null)
            }
        }
    }
    // 임시저장 복원 안내
    LaunchedEffect(restoredDraft) {
        if (restoredDraft != null) onToast(DamoimStrings.TOAST_DRAFT_LOADED)
    }
    if (!state.editLoaded) return  // 수정 모드 프리필 로딩 대기(순간적)
    PostWriteScreen(
        initialCategory = initialCategory,
        prefill = state.editing,
        prefillDraft = restoredDraft,
        isAdmin = state.isAdmin,
        isSaving = state.isSaving,
        onCancel = onCancel,
        onSubmit = viewModel::submit,
        onTempSave = viewModel::saveDraft,
        onToast = onToast,
    )
}

@Composable
fun PostWriteScreen(
    initialCategory: BoardCategory = BoardCategory.FREE,
    prefill: BoardPost? = null,
    prefillDraft: PostDraft? = null,
    isAdmin: Boolean = false,
    isSaving: Boolean = false,
    onCancel: () -> Unit = {},
    onSubmit: (PostDraft) -> Unit = {},
    onTempSave: (PostDraft) -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val scope = rememberCoroutineScope()

    // ── 작성 상태 (수정 모드 원본 > 임시저장 초안 > 빈 값) ──
    var title by remember(prefill, prefillDraft) { mutableStateOf(prefill?.title ?: prefillDraft?.title ?: "") }
    var body by remember(prefill, prefillDraft) { mutableStateOf(prefill?.content ?: prefillDraft?.content ?: "") }
    var category by remember(prefill, prefillDraft) { mutableStateOf(prefill?.category ?: prefillDraft?.category ?: initialCategory) }
    var pinned by remember(prefill, prefillDraft) { mutableStateOf(prefill?.isPinned ?: prefillDraft?.pinned ?: false) }
    // 수정 모드는 기존 이미지(url)를 프리필. 임시저장은 미디어를 보존하지 않아 여기선 비어 있음.
    val photos = remember(prefill, prefillDraft) {
        mutableStateListOf<DraftImage>().apply {
            prefill?.attachments?.filterIsInstance<PostAttachment.Image>()
                ?.forEach { add(DraftImage(url = it.url, storageKey = it.storageKey)) }
        }
    }
    val docs = remember(prefill, prefillDraft) {
        mutableStateListOf<DraftDocFile>().apply {
            prefill?.attachments?.filterIsInstance<PostAttachment.FileDoc>()
                ?.forEach { add(DraftDocFile(name = it.name, sizeLabel = it.size, url = it.url, storageKey = it.storageKey)) }
        }
    }
    // 링크는 여러 개 동시 첨부 가능 — 각 항목은 편집 중인 URL 문자열.
    val links = remember(prefill, prefillDraft) {
        mutableStateListOf<String>().apply {
            prefill?.attachments?.filterIsInstance<PostAttachment.Link>()?.forEach { it.url.takeIf { u -> u.isNotBlank() }?.let(::add) }
            if (isEmpty()) prefillDraft?.links?.forEach { it.url.takeIf { u -> u.isNotBlank() }?.let(::add) }
        }
    }
    val pollOptions = remember(prefill, prefillDraft) {
        mutableStateListOf<String>().apply {
            prefill?.poll?.options?.forEach { add(it.label) }
            if (isEmpty()) prefillDraft?.poll?.options?.forEach { add(it) }
            if (isEmpty()) { add(""); add("") }
        }
    }
    var pollMulti by remember(prefill, prefillDraft) { mutableStateOf(prefill?.poll?.multiSelect ?: prefillDraft?.poll?.multiSelect ?: false) }
    var pollAnon by remember(prefill, prefillDraft) { mutableStateOf(prefill?.poll?.anonymous ?: prefillDraft?.poll?.anonymous ?: true) }
    var pollDeadlineLabel by remember(prefill, prefillDraft) { mutableStateOf(prefill?.poll?.deadlineLabel ?: prefillDraft?.poll?.deadlineLabel ?: "") }
    var pollDeadlineMillis by remember(prefill, prefillDraft) { mutableStateOf(prefill?.poll?.deadlineEpochMillis ?: prefillDraft?.poll?.deadlineEpochMillis) }
    var recruitCapacity by remember(prefill, prefillDraft) { mutableStateOf(prefill?.recruit?.capacity ?: prefillDraft?.recruit?.capacity ?: 5) }
    var recruitDeadlineLabel by remember(prefill, prefillDraft) { mutableStateOf(prefill?.recruit?.deadlineLabel ?: prefillDraft?.recruit?.deadlineLabel ?: "") }
    var recruitDeadlineMillis by remember(prefill, prefillDraft) { mutableStateOf(prefill?.recruit?.deadlineEpochMillis ?: prefillDraft?.recruit?.deadlineEpochMillis) }
    var recruitDday by remember(prefill, prefillDraft) { mutableStateOf(prefill?.recruit?.dday ?: prefillDraft?.recruit?.dday) }
    var recruitFirstCome by remember(prefill, prefillDraft) { mutableStateOf((prefill?.recruit?.method ?: if (prefillDraft?.recruit?.firstCome == false) "승인제" else null) != "승인제") }
    // 투표 첨부 여부 — 이미지·문서·링크와 독립적으로 공존할 수 있다.
    var pollEnabled by remember(prefill, prefillDraft) { mutableStateOf(prefill?.poll != null || prefillDraft?.poll != null) }
    var sheet by remember { mutableStateOf<WriteSheet?>(null) }

    // 오버레이(시트)는 시스템 뒤로가기로 닫힌다
    PlatformBackHandler(enabled = sheet != null) { sheet = null }

    // ── 실제 첨부 피커들 ──
    val photoPicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Multiple(maxSelection = 10),
        scope = scope,
        onResult = { byteArrays ->
            val before = photos.size
            byteArrays.forEach { bytes ->
                if (photos.size < 10) {
                    // 바이트는 업로드용으로 보존, 로컬 미리보기는 ImageStore에 디코드해 둔다.
                    val localKey = ImageStore.put(bytes.toImageBitmap())
                    photos.add(DraftImage(bytes = bytes, localKey = localKey))
                }
            }
            // 10장을 넘겨 고른 경우 일부만 담기므로 안내한다.
            if (before + byteArrays.size > 10) onToast(DamoimStrings.TOAST_PHOTO_MAX)
        },
    )
    val cameraLauncher = rememberCameraLauncher { bytes ->
        if (bytes != null) {
            if (photos.size < 10) {
                val localKey = ImageStore.put(bytes.toImageBitmap())
                photos.add(DraftImage(bytes = bytes, contentType = "image/jpeg", localKey = localKey))
            } else {
                onToast(DamoimStrings.TOAST_PHOTO_MAX)
            }
        } else {
            onToast(DamoimStrings.TOAST_CAMERA_UNAVAILABLE)
        }
    }
    // 이미 10장이면 피커/카메라를 열지 않고 바로 안내한다.
    fun tryPickPhoto() { if (photos.size >= 10) onToast(DamoimStrings.TOAST_PHOTO_MAX) else photoPicker.launch() }
    fun tryCamera() { if (photos.size >= 10) onToast(DamoimStrings.TOAST_PHOTO_MAX) else cameraLauncher.launch() }
    val documentPicker = rememberDocumentPickerLauncher { doc ->
        if (doc != null) {
            docs.add(DraftDocFile(name = doc.name, sizeLabel = doc.sizeLabel, bytes = doc.bytes, contentType = doc.contentType))
        }
    }

    fun buildDraft(): PostDraft = PostDraft(
        category = category,
        title = title.trim(),
        content = body.trim(),
        // 첨부는 서로 배타적이지 않다 — 이미지·문서·링크·투표를 동시에 담는다.
        images = photos.toList(),
        docs = docs.toList(),
        links = links.mapNotNull { raw ->
            val url = raw.trim()
            if (url.isBlank()) return@mapNotNull null
            // 스킴이 없으면 https:// 보정 → 전체 URL 보존(웹 이동용).
            val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val host = normalized.substringAfter("://").substringBefore("/").ifBlank { normalized }
            DraftLink(url = normalized, title = host, domain = host)
        },
        // 공백 항목을 제거하고 2개 이상일 때만 투표를 담는다(서버는 유효 항목 2개 이상을 요구 → 전체 글 롤백 방지).
        poll = pollOptions.map { it.trim() }.filter { it.isNotEmpty() }
            .takeIf { pollEnabled && it.size >= 2 }
            ?.let { opts -> PollDraft(opts, pollAnon, pollMulti, pollDeadlineLabel.ifBlank { DamoimStrings.PICKER_TITLE }, pollDeadlineMillis) },
        recruit = if (category == BoardCategory.RECRUIT) {
            RecruitDraft(recruitCapacity, recruitDeadlineLabel.ifBlank { "미정" }, recruitDday, recruitFirstCome, recruitDeadlineMillis)
        } else null,
        pinned = category == BoardCategory.NOTICE && pinned,
    )

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(colors.surface).safeDrawingPadding()) {
            // 헤더
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(DamoimStrings.COMMON_CANCEL, style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp), color = colors.textMuted, modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel))
                Text(DamoimStrings.WRITE_TITLE, style = DamoimTheme.typography.titleMedium.copy(fontSize = 16.sp), color = colors.textPrimary, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                Text(
                    DamoimStrings.WRITE_SUBMIT,
                    style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp),
                    color = colors.primary,
                    modifier = Modifier.alpha(if (isSaving) 0.4f else 1f)
                        .clickable(enabled = !isSaving, interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            // 투표를 켰는데 유효 항목이 2개 미만이면 조용히 빠지지 않도록 미리 안내한다.
                            if (pollEnabled && pollOptions.count { it.isNotBlank() } < 2) onToast(DamoimStrings.TOAST_POLL_MIN_OPTIONS)
                            else onSubmit(buildDraft())
                        },
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
            // 카테고리 + 잠금 안내
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clip(RoundedCornerShape(999.dp)).background(colors.primaryContainer).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { sheet = WriteSheet.Category }.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${boardCategoryLabel(category)} 게시판", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primaryDark)
                    ChevronDownIcon(colors.primaryDark, Modifier.size(12.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    LockIcon(colors.textDisabled, Modifier.size(11.dp))
                    Text(DamoimStrings.WRITE_NOTICE_LOCK, style = DamoimTheme.typography.label, color = colors.textDisabled)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))

            val hasAttach = photos.isNotEmpty() || docs.isNotEmpty() || links.isNotEmpty() || pollEnabled
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                // 제목
                Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp)) {
                    BasicTextField(title, { title = it }, singleLine = true, textStyle = DamoimTheme.typography.titleLarge.copy(color = colors.textPrimary, fontSize = 19.sp), cursorBrush = SolidColor(colors.primary), modifier = Modifier.fillMaxWidth(), decorationBox = { inner -> if (title.isEmpty()) Text(DamoimStrings.WRITE_TITLE_PLACEHOLDER, style = DamoimTheme.typography.titleLarge.copy(fontSize = 19.sp), color = colors.textDisabled); inner() })
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
                // 39 모집 필드 (정원·마감·선착순)
                if (category == BoardCategory.RECRUIT) {
                    RecruitFields(
                        capacity = recruitCapacity,
                        deadlineLabel = recruitDeadlineLabel,
                        firstCome = recruitFirstCome,
                        onCapacity = { recruitCapacity = it },
                        onPickDeadline = { sheet = WriteSheet.RecruitDeadline },
                        onFirstCome = { recruitFirstCome = it },
                    )
                    Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
                }
                // 본문
                Box(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
                    BasicTextField(body, { body = it }, textStyle = DamoimTheme.typography.body.copy(color = colors.textSecondary, fontSize = 15.sp, lineHeight = 25.sp), cursorBrush = SolidColor(colors.primary), modifier = Modifier.fillMaxWidth().height(if (!hasAttach && category != BoardCategory.RECRUIT) 180.dp else 90.dp), decorationBox = { inner -> if (body.isEmpty()) Text(DamoimStrings.WRITE_BODY_PLACEHOLDER, style = DamoimTheme.typography.body.copy(fontSize = 15.sp), color = colors.textDisabled); inner() })
                }
                // 첨부 영역 — 여러 종류를 동시에 쌓아 보여준다(상호 배타 아님)
                if (photos.isNotEmpty()) {
                    PhotoAttach(photos = photos, onAdd = { tryPickPhoto() }, onRemove = { photos.removeAt(it) })
                    Spacer(Modifier.height(12.dp))
                }
                if (docs.isNotEmpty()) {
                    DocAttach(docs = docs, onAdd = { documentPicker.launch() }, onRemove = { docs.removeAt(it) })
                    Spacer(Modifier.height(12.dp))
                }
                links.forEachIndexed { i, url ->
                    LinkAttach(url = url, onUrlChange = { links[i] = it }, onClear = { links.removeAt(i) })
                    Spacer(Modifier.height(12.dp))
                }
                if (pollEnabled) {
                    PollBuilder(
                        options = pollOptions,
                        multi = pollMulti,
                        anon = pollAnon,
                        deadlineLabel = pollDeadlineLabel,
                        onOptionChange = { i, v -> pollOptions[i] = v },
                        onAddOption = { pollOptions.add("") },
                        onRemoveOption = { if (pollOptions.size > 1) pollOptions.removeAt(it) },
                        onMulti = { pollMulti = it },
                        onAnon = { pollAnon = it },
                        onPickDeadline = { sheet = WriteSheet.PollDeadline },
                        onClose = { pollEnabled = false },
                    )
                }
                if (!hasAttach) AddTile(count = 0, onClick = { sheet = WriteSheet.Attach })
                Spacer(Modifier.height(20.dp))
            }
            // 하단 툴바 — 각 아이콘이 실제 피커/모드로 연결
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.dividerLight))
            Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                ToolIcon({ CameraIcon(it, Modifier.size(22.dp)) }, active = false) { tryCamera() }
                ToolIcon({ ImageIcon(it, Modifier.size(22.dp)) }, active = photos.isNotEmpty()) { tryPickPhoto() }
                ToolIcon({ PaperclipIcon(it, Modifier.size(22.dp)) }, active = docs.isNotEmpty()) { documentPicker.launch() }
                ToolIcon({ LinkIcon(it, Modifier.size(22.dp)) }, active = links.isNotEmpty()) { links.add("") }
                ToolIcon({ ChartIcon(it, Modifier.size(22.dp)) }, active = pollEnabled) { pollEnabled = true }
                Spacer(Modifier.weight(1f))
                Text(
                    DamoimStrings.WRITE_TEMP_SAVE,
                    style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.textTertiary,
                    modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onTempSave(buildDraft()) },
                )
            }
        }

        // ── 오버레이 ──
        when (sheet) {
            WriteSheet.Category -> CategorySheet(
                isAdmin = isAdmin, current = category,
                onDismiss = { sheet = null },
                onConfirm = { choice ->
                    category = choice.category
                    pinned = choice.pinned
                    sheet = null
                },
            )
            WriteSheet.Attach -> AttachSheet(
                onPhoto = { sheet = null; tryPickPhoto() },
                onCamera = { sheet = null; tryCamera() },
                onDocument = { sheet = null; documentPicker.launch() },
                onLink = { links.add(""); sheet = null },
                onPoll = { pollEnabled = true; sheet = null },
                onDismiss = { sheet = null },
            )
            WriteSheet.PollDeadline -> DatePickerSheet(
                initial = pollDeadlineMillis?.let { pickedDeadlineFromMillis(it) },
                onDismiss = { sheet = null },
                onConfirm = { picked ->
                    pollDeadlineLabel = picked.label
                    pollDeadlineMillis = picked.toEpochMillis()
                    sheet = null
                },
            )
            WriteSheet.RecruitDeadline -> DatePickerSheet(
                initial = recruitDeadlineMillis?.let { pickedDeadlineFromMillis(it) },
                onDismiss = { sheet = null },
                onConfirm = { picked ->
                    recruitDeadlineLabel = picked.label
                    recruitDday = ddayLabel(picked.date, today)
                    recruitDeadlineMillis = picked.toEpochMillis()
                    sheet = null
                },
            )
            null -> {}
        }
    }
}

@Composable
private fun ToolIcon(icon: @Composable (Color) -> Unit, active: Boolean, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Box(Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)) {
        icon(if (active) colors.primary else colors.textTertiary)
    }
}

// 빈 상태 이미지 추가 타일
@Composable
private fun AddTile(count: Int, onClick: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, colors.outline, RoundedCornerShape(14.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            PlusIcon(colors.textMuted, Modifier.size(18.dp))
            Spacer(Modifier.height(2.dp))
            Text(DamoimStrings.imageCount(count, 10), style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted)
        }
    }
}

// 15/PHOTO — 실제 선택/촬영 이미지 썸네일(삭제 가능) + 추가 타일
@Composable
private fun PhotoAttach(photos: List<DraftImage>, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    val colors = DamoimTheme.colors
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 추가(+) 타일은 항상 맨 앞에 고정 — 사진이 늘어도 위치가 바뀌지 않는다.
        if (photos.size < 10) {
            Column(Modifier.size(76.dp).clip(RoundedCornerShape(14.dp)).border(1.5.dp, colors.outline, RoundedCornerShape(14.dp)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onAdd), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                PlusIcon(colors.textMuted, Modifier.size(18.dp)); Spacer(Modifier.height(2.dp)); Text(DamoimStrings.imageCount(photos.size, 10), style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted)
            }
        }
        photos.forEachIndexed { i, img ->
            Box(Modifier.size(76.dp)) {
                // 방금 고른 사진=로컬 미리보기(localKey), 수정 프리필=서버 이미지(url).
                NetworkImage(url = img.url, localKey = img.localKey, modifier = Modifier.size(76.dp), cornerRadius = 14.dp)
                Box(
                    Modifier.align(Alignment.TopEnd).size(20.dp).clip(RoundedCornerShape(999.dp)).background(colors.textPrimary)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRemove(i) },
                    contentAlignment = Alignment.Center,
                ) { CloseIcon(colors.surface, Modifier.size(10.dp)) }
            }
        }
    }
}

// 70/DOC — 실제 문서 피커로 추가한 파일 목록(삭제 가능)
@Composable
private fun DocAttach(docs: List<DraftDocFile>, onAdd: () -> Unit, onRemove: (Int) -> Unit) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        docs.forEachIndexed { i, doc ->
            val ext = doc.name.substringAfterLast('.', "").uppercase().take(3).ifBlank { "DOC" }
            val tileColor = if (ext == "PDF") colors.error else Color(0xFF1F9D55)
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.dp, colors.divider, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tileColor), contentAlignment = Alignment.Center) { Text(ext, style = DamoimTheme.typography.labelSmall, color = colors.onPrimary) }
                Column(Modifier.weight(1f)) {
                    Text(doc.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary, maxLines = 1)
                    if (doc.sizeLabel.isNotEmpty()) Text(doc.sizeLabel, style = DamoimTheme.typography.label, color = colors.textDisabled)
                }
                Box(Modifier.size(24.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onRemove(i) }, contentAlignment = Alignment.Center) {
                    CloseIcon(colors.textDisabled, Modifier.size(16.dp))
                }
            }
        }
        // 파일 추가 → 실제 문서 피커
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.5.dp, colors.outline, RoundedCornerShape(12.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onAdd)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
        ) {
            PlusIcon(colors.textMuted, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp))
            Text(DamoimStrings.ATTACH_DOC, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textMuted)
        }
    }
}

// 34/LINK — URL 입력 행(미리보기 이미지 없음 — 링크만 첨부). X로 이 링크를 제거한다.
@Composable
private fun LinkAttach(url: String, onUrlChange: (String) -> Unit, onClear: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(colors.surfaceInput).padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LinkIcon(colors.textMuted, Modifier.size(14.dp))
            Box(Modifier.weight(1f)) {
                if (url.isEmpty()) Text(DamoimStrings.ATTACH_LINK_DESC, style = DamoimTheme.typography.caption, color = colors.textDisabled)
                BasicTextField(url, onUrlChange, singleLine = true, textStyle = DamoimTheme.typography.caption.copy(color = colors.textTertiary), cursorBrush = SolidColor(colors.primary), modifier = Modifier.fillMaxWidth())
            }
            Box(
                Modifier.size(22.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClear),
                contentAlignment = Alignment.Center,
            ) { CloseIcon(colors.textDisabled, Modifier.size(13.dp)) }
        }
    }
}

// 35/POLL — 투표 만들기 (항목 추가/삭제·토글·마감 실동작)
@Composable
private fun PollBuilder(
    options: List<String>,
    multi: Boolean,
    anon: Boolean,
    deadlineLabel: String,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onMulti: (Boolean) -> Unit,
    onAnon: (Boolean) -> Unit,
    onPickDeadline: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.5.dp, colors.primary, RoundedCornerShape(18.dp)).background(colors.primaryContainer.copy(alpha = 0.25f)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChartIcon(colors.primary, Modifier.size(17.dp)); Spacer(Modifier.width(8.dp))
                Text(DamoimStrings.POLL_LABEL, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold), color = colors.primaryDeep, modifier = Modifier.weight(1f))
                Box(Modifier.size(24.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose), contentAlignment = Alignment.Center) {
                    CloseIcon(colors.textMuted, Modifier.size(15.dp))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEachIndexed { i, option ->
                    PollOptionInput(option, onChange = { onOptionChange(i, it) }, onRemove = { onRemoveOption(i) })
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(1.5.dp, colors.outline, RoundedCornerShape(12.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onAddOption)
                        .padding(vertical = 13.dp),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlusIcon(colors.textMuted, Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text("항목 추가", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.textMuted)
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(colors.divider))
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(DamoimStrings.POLL_MULTI_ON, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textTertiary, modifier = Modifier.weight(1f))
                    MiniToggle(multi) { onMulti(!multi) }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(DamoimStrings.POLL_ANON, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textTertiary, modifier = Modifier.weight(1f))
                    MiniToggle(anon) { onAnon(!anon) }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("마감", style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal), color = colors.textTertiary, modifier = Modifier.weight(1f))
                    Text(
                        deadlineLabel.ifBlank { DamoimStrings.PICKER_TITLE },
                        style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary,
                        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPickDeadline),
                    )
                }
            }
        }
    }
}

@Composable
private fun PollOptionInput(text: String, onChange: (String) -> Unit, onRemove: () -> Unit) {
    val colors = DamoimTheme.colors
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.surface).border(1.dp, colors.divider, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            if (text.isEmpty()) Text("항목 입력", style = DamoimTheme.typography.body.copy(fontSize = 14.sp), color = colors.textDisabled)
            BasicTextField(text, onChange, singleLine = true, textStyle = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = colors.textPrimary), cursorBrush = SolidColor(colors.primary), modifier = Modifier.fillMaxWidth())
        }
        Box(Modifier.size(22.dp).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onRemove), contentAlignment = Alignment.Center) {
            CloseIcon(colors.outlineStrong, Modifier.size(14.dp))
        }
    }
}

// 39 모집 필드 — 정원·마감·선착순
@Composable
private fun RecruitFields(
    capacity: Int,
    deadlineLabel: String,
    firstCome: Boolean,
    onCapacity: (Int) -> Unit,
    onPickDeadline: () -> Unit,
    onFirstCome: (Boolean) -> Unit,
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        // 정원
        Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PersonPlusIcon(colors.textSecondary, Modifier.size(18.dp))
            Text("모집 정원", style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                StepBtn("−", colors.surfaceVariant, colors.textTertiary) { if (capacity > 1) onCapacity(capacity - 1) }
                // +/- 버튼 외에 텍스트로도 정원을 직접 입력할 수 있다.
                var capacityText by remember { mutableStateOf(capacity.toString()) }
                // 외부(±버튼)로 값이 바뀌면 입력 텍스트도 맞춘다.
                LaunchedEffect(capacity) { if (capacityText.toIntOrNull() != capacity) capacityText = capacity.toString() }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = capacityText,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(4)
                            capacityText = digits
                            digits.toIntOrNull()?.takeIf { it >= 1 }?.let(onCapacity)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = colors.textPrimary, textAlign = TextAlign.Center),
                        cursorBrush = SolidColor(colors.primary),
                        modifier = Modifier.width(32.dp),
                    )
                    Text("명", style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = colors.textPrimary)
                }
                StepBtn("+", colors.primaryContainer, colors.primary) { onCapacity(capacity + 1) }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
        // 마감 (86 피커 — 실동작)
        Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalendarIcon(colors.textSecondary, Modifier.size(18.dp))
            Text("모집 마감", style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            Text(
                deadlineLabel.ifBlank { DamoimStrings.PICKER_TITLE },
                style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = colors.primary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(colors.primaryContainer)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onPickDeadline)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
        // 선착순
        Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChartIcon(colors.textSecondary, Modifier.size(18.dp))
            Text("선착순 마감", style = DamoimTheme.typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp), color = colors.textPrimary, modifier = Modifier.weight(1f))
            MiniToggle(firstCome) { onFirstCome(!firstCome) }
        }
    }
}

@Composable
private fun StepBtn(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Box(Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).background(bg).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = fg)
    }
}

@Preview
@Composable
private fun PostWriteFreePreview() {
    DamoimTheme { PostWriteScreen(initialCategory = BoardCategory.FREE) }
}

@Preview
@Composable
private fun PostWriteRecruitPreview() {
    DamoimTheme { PostWriteScreen(initialCategory = BoardCategory.RECRUIT) }
}
