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
import damoim.shared.generated.resources.ic_chevron_left
import damoim.shared.generated.resources.ic_chevron_right
import damoim.shared.generated.resources.ic_close
import damoim.shared.generated.resources.ic_copy
import damoim.shared.generated.resources.ic_crown
import damoim.shared.generated.resources.ic_folder
import damoim.shared.generated.resources.ic_home
import damoim.shared.generated.resources.ic_info
import damoim.shared.generated.resources.ic_kakao
import damoim.shared.generated.resources.ic_link
import damoim.shared.generated.resources.ic_lock
import damoim.shared.generated.resources.ic_megaphone
import damoim.shared.generated.resources.ic_people
import damoim.shared.generated.resources.ic_person_plus
import damoim.shared.generated.resources.ic_plus
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
