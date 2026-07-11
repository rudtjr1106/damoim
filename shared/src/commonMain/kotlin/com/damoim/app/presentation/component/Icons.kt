package com.damoim.app.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import damoim.shared.generated.resources.Res
import damoim.shared.generated.resources.ic_arrow_right
import damoim.shared.generated.resources.ic_bell
import damoim.shared.generated.resources.ic_board
import damoim.shared.generated.resources.ic_calendar
import damoim.shared.generated.resources.ic_camera
import damoim.shared.generated.resources.ic_chart
import damoim.shared.generated.resources.ic_check
import damoim.shared.generated.resources.ic_chevron_down
import damoim.shared.generated.resources.ic_clock
import damoim.shared.generated.resources.ic_edit
import damoim.shared.generated.resources.ic_list
import damoim.shared.generated.resources.ic_location
import damoim.shared.generated.resources.ic_reply
import damoim.shared.generated.resources.ic_share
import damoim.shared.generated.resources.ic_trash
import damoim.shared.generated.resources.ic_upload
import damoim.shared.generated.resources.ic_chevron_left
import damoim.shared.generated.resources.ic_chevron_right
import damoim.shared.generated.resources.ic_close
import damoim.shared.generated.resources.ic_comment
import damoim.shared.generated.resources.ic_copy
import damoim.shared.generated.resources.ic_crown
import damoim.shared.generated.resources.ic_download
import damoim.shared.generated.resources.ic_folder
import damoim.shared.generated.resources.ic_heart
import damoim.shared.generated.resources.ic_home
import damoim.shared.generated.resources.ic_image
import damoim.shared.generated.resources.ic_info
import damoim.shared.generated.resources.ic_kakao
import damoim.shared.generated.resources.ic_link
import damoim.shared.generated.resources.ic_lock
import damoim.shared.generated.resources.ic_megaphone
import damoim.shared.generated.resources.ic_more
import damoim.shared.generated.resources.ic_paperclip
import damoim.shared.generated.resources.ic_people
import damoim.shared.generated.resources.ic_person_minus
import damoim.shared.generated.resources.ic_person_plus
import damoim.shared.generated.resources.ic_door_exit
import damoim.shared.generated.resources.ic_plus
import damoim.shared.generated.resources.ic_search
import damoim.shared.generated.resources.ic_send
import damoim.shared.generated.resources.ic_settings
import damoim.shared.generated.resources.ic_user_single
import damoim.shared.generated.resources.ic_warning
import org.jetbrains.compose.resources.DrawableResource

/**
 * 아이콘 = composeResources/drawable 의 벡터 드로어블(디자인 원본 SVG path 기반)을 tint해서 렌더.
 * 각 함수는 얇은 래퍼로, `tint`로 색을 지정하고 `modifier.size`로 크기를 정한다.
 */

@Composable
private fun VectorIcon(res: DrawableResource, tint: Color, modifier: Modifier) =
    Icon(painterResource(res), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun KakaoBubbleIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_kakao, tint, modifier)

@Composable
fun CheckIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_check, tint, modifier)

@Composable
fun BackChevronIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) = VectorIcon(Res.drawable.ic_chevron_left, tint, modifier)

@Composable
fun ChevronRightIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_chevron_right, tint, modifier)

@Composable
fun PlusIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) = VectorIcon(Res.drawable.ic_plus, tint, modifier)

@Composable
fun LockIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) = VectorIcon(Res.drawable.ic_lock, tint, modifier)

@Composable
fun InfoIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_info, tint, modifier)

/** 닫기/거절(X). */
@Composable
fun CloseIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) = VectorIcon(Res.drawable.ic_close, tint, modifier)

/** 경고(느낌표 삼각형). */
@Composable
fun WarningIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) = VectorIcon(Res.drawable.ic_warning, tint, modifier)

@Composable
fun CameraIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) = VectorIcon(Res.drawable.ic_camera, tint, modifier)

/** 사람들(다중) — 회원/빈 상태. */
@Composable
fun PeopleIcon(tint: Color, modifier: Modifier = Modifier.size(40.dp)) = VectorIcon(Res.drawable.ic_people, tint, modifier)

// ── B 그룹 ──
@Composable
fun BellIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_bell, tint, modifier)

@Composable
fun CrownIcon(tint: Color, modifier: Modifier = Modifier.size(12.dp)) = VectorIcon(Res.drawable.ic_crown, tint, modifier)

@Composable
fun PersonPlusIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_person_plus, tint, modifier)

@Composable
fun ArrowRightIcon(tint: Color, modifier: Modifier = Modifier.size(14.dp)) = VectorIcon(Res.drawable.ic_arrow_right, tint, modifier)

@Composable
fun BoardIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_board, tint, modifier)

@Composable
fun CalendarIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_calendar, tint, modifier)

@Composable
fun FolderIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_folder, tint, modifier)

@Composable
fun MegaphoneIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_megaphone, tint, modifier)

@Composable
fun UserSingleIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_user_single, tint, modifier)

@Composable
fun CopyIcon(tint: Color, modifier: Modifier = Modifier.size(17.dp)) = VectorIcon(Res.drawable.ic_copy, tint, modifier)

@Composable
fun LinkIcon(tint: Color, modifier: Modifier = Modifier.size(17.dp)) = VectorIcon(Res.drawable.ic_link, tint, modifier)

@Composable
fun ChartIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_chart, tint, modifier)

@Composable
fun HomeIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) = VectorIcon(Res.drawable.ic_home, tint, modifier)

@Composable
fun SettingsIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) = VectorIcon(Res.drawable.ic_settings, tint, modifier)

// ── C 게시판 ──
@Composable
fun SearchIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_search, tint, modifier)

@Composable
fun HeartIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) = VectorIcon(Res.drawable.ic_heart, tint, modifier)

@Composable
fun CommentIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) = VectorIcon(Res.drawable.ic_comment, tint, modifier)

/** 케밥(수평 3점) 메뉴 — 채워진 아이콘. */
@Composable
fun MoreIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_more, tint, modifier)

@Composable
fun SendIcon(tint: Color, modifier: Modifier = Modifier.size(17.dp)) = VectorIcon(Res.drawable.ic_send, tint, modifier)

@Composable
fun ChevronDownIcon(tint: Color, modifier: Modifier = Modifier.size(12.dp)) = VectorIcon(Res.drawable.ic_chevron_down, tint, modifier)

@Composable
fun DownloadIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_download, tint, modifier)

/** 클립(파일 첨부) — ic_link(체인)와 다른 단일 클립 글리프. */
@Composable
fun PaperclipIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_paperclip, tint, modifier)

@Composable
fun ImageIcon(tint: Color, modifier: Modifier = Modifier.size(22.dp)) = VectorIcon(Res.drawable.ic_image, tint, modifier)

@Composable
fun ClockIcon(tint: Color, modifier: Modifier = Modifier.size(17.dp)) = VectorIcon(Res.drawable.ic_clock, tint, modifier)

/** 장소 핀(21/22/24 일정 위치). */
@Composable
fun LocationIcon(tint: Color, modifier: Modifier = Modifier.size(17.dp)) = VectorIcon(Res.drawable.ic_location, tint, modifier)

/** 신청자/명단 리스트(불균형 3선). */
@Composable
fun ListIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_list, tint, modifier)

@Composable
fun EditIcon(tint: Color, modifier: Modifier = Modifier.size(19.dp)) = VectorIcon(Res.drawable.ic_edit, tint, modifier)

@Composable
fun ShareIcon(tint: Color, modifier: Modifier = Modifier.size(19.dp)) = VectorIcon(Res.drawable.ic_share, tint, modifier)

@Composable
fun TrashIcon(tint: Color, modifier: Modifier = Modifier.size(19.dp)) = VectorIcon(Res.drawable.ic_trash, tint, modifier)

@Composable
fun ReplyIcon(tint: Color, modifier: Modifier = Modifier.size(19.dp)) = VectorIcon(Res.drawable.ic_reply, tint, modifier)

// ── D 자료실 ──
/** 업로드(밑줄 위로 향하는 화살표) — 67 FAB · 69 드롭존. */
@Composable
fun UploadIcon(tint: Color, modifier: Modifier = Modifier.size(20.dp)) = VectorIcon(Res.drawable.ic_upload, tint, modifier)

// ── E 회원·기수 ──
/** 회원 내보내기(사람 - ) — 18/43. */
@Composable
fun PersonMinusIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_person_minus, tint, modifier)

/** 문 나가기(로그아웃/전환/탈퇴) — 20 동아리 전환 · 60 탈퇴. */
@Composable
fun DoorExitIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) = VectorIcon(Res.drawable.ic_door_exit, tint, modifier)
