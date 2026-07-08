package com.damoim.app.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import damoim.shared.generated.resources.Res
import damoim.shared.generated.resources.ic_camera
import damoim.shared.generated.resources.ic_check
import damoim.shared.generated.resources.ic_chevron_left
import damoim.shared.generated.resources.ic_chevron_right
import damoim.shared.generated.resources.ic_close
import damoim.shared.generated.resources.ic_info
import damoim.shared.generated.resources.ic_lock
import damoim.shared.generated.resources.ic_people
import damoim.shared.generated.resources.ic_plus
import damoim.shared.generated.resources.ic_warning

/**
 * 아이콘 = composeResources/drawable 의 벡터 드로어블(디자인 원본 SVG path 기반)을 tint해서 렌더.
 *
 * 각 함수는 얇은 래퍼로, `tint`로 색을 지정하고 `modifier.size`로 크기를 정한다.
 * 새 아이콘은 drawable/ic_*.xml 추가 후 여기 래퍼만 한 줄 늘리면 된다.
 */

@Composable
fun CheckIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) =
    Icon(painterResource(Res.drawable.ic_check), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun BackChevronIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) =
    Icon(painterResource(Res.drawable.ic_chevron_left), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun ChevronRightIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) =
    Icon(painterResource(Res.drawable.ic_chevron_right), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun PlusIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) =
    Icon(painterResource(Res.drawable.ic_plus), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun LockIcon(tint: Color, modifier: Modifier = Modifier.size(21.dp)) =
    Icon(painterResource(Res.drawable.ic_lock), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun InfoIcon(tint: Color, modifier: Modifier = Modifier.size(18.dp)) =
    Icon(painterResource(Res.drawable.ic_info), contentDescription = null, tint = tint, modifier = modifier)

/** 닫기/거절(X). 화면 38 가입 거절 아이콘. */
@Composable
fun CloseIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) =
    Icon(painterResource(Res.drawable.ic_close), contentDescription = null, tint = tint, modifier = modifier)

/** 경고(느낌표 삼각형). 오류 안내 배너 등. */
@Composable
fun WarningIcon(tint: Color, modifier: Modifier = Modifier.size(24.dp)) =
    Icon(painterResource(Res.drawable.ic_warning), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun CameraIcon(tint: Color, modifier: Modifier = Modifier.size(16.dp)) =
    Icon(painterResource(Res.drawable.ic_camera), contentDescription = null, tint = tint, modifier = modifier)

@Composable
fun PeopleIcon(tint: Color, modifier: Modifier = Modifier.size(40.dp)) =
    Icon(painterResource(Res.drawable.ic_people), contentDescription = null, tint = tint, modifier = modifier)
