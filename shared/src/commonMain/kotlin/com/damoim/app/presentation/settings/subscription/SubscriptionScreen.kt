package com.damoim.app.presentation.settings.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.PaymentRecord
import com.damoim.app.domain.model.SubscriptionState
import com.damoim.app.presentation.component.ChevronRightIcon
import com.damoim.app.presentation.component.DamoimConfirmDialog
import com.damoim.app.presentation.settings.SettingsRow
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun SubscriptionRoute(
    viewModel: SubscriptionViewModel = viewModel(key = "subManage") { SubscriptionViewModel(AppGraph.subscriptionUseCase) },
    onBack: () -> Unit = {},
    onChangePlan: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.sideEffect.collect { if (it is SubManageSideEffect.Toast) onToast(it.message) } }
    SubscriptionScreen(state, onBack, onChangePlan, viewModel::cancel, onToast)
}

@Composable
fun SubscriptionScreen(
    state: SubManageUiState = SubManageUiState(),
    onBack: () -> Unit = {},
    onChangePlan: () -> Unit = {},
    onCancel: () -> Unit = {},
    onToast: (String) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    val sub = state.sub ?: return
    var confirmCancel by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(colors.surface)) {
        Column(Modifier.fillMaxSize()) {
            SettingsTopBar(DamoimStrings.SUB_MANAGE_TITLE, onBack)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Hero(sub)
                // 메뉴
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp)) {
                    SettingsRow(DamoimStrings.SUB_CHANGE_PLAN, onChangePlan, showDivider = sub.active, trailing = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(DamoimStrings.SUB_CHANGE_PLAN_SUB, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                            ChevronRightIcon(colors.outlineStrong, Modifier.size(16.dp))
                        }
                    })
                    if (sub.active) {
                        SettingsRow(DamoimStrings.SUB_PAYMENT_METHOD, { onToast(DamoimStrings.TOAST_EXTERNAL_STORE) }, showDivider = !sub.canceled, trailing = {
                            Text(DamoimStrings.SUB_PAYMENT_METHOD_SUB, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                        })
                        // 이미 해지 예약이면 해지 행을 숨기고 갱신일까지 이용 가능 안내만 남긴다.
                        if (!sub.canceled) {
                            SettingsRow(DamoimStrings.SUB_CANCEL, { confirmCancel = true }, labelColor = colors.error, showDivider = false, trailing = {
                                Text(DamoimStrings.SUB_CANCEL_SUB, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal), color = colors.textDisabled)
                            })
                        }
                    }
                }
                // 결제 내역
                if (sub.payments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(DamoimStrings.SUB_PAYMENT_HISTORY, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp), color = colors.textMuted, modifier = Modifier.padding(horizontal = 4.dp))
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(colors.surface).padding(horizontal = 18.dp)) {
                            sub.payments.forEachIndexed { i, p -> PaymentRow(p, showDivider = i < sub.payments.lastIndex) }
                        }
                    }
                }
            }
        }
        if (confirmCancel) {
            DamoimConfirmDialog(
                title = DamoimStrings.SUB_CANCEL_DIALOG_TITLE, desc = DamoimStrings.SUB_CANCEL_DIALOG_DESC,
                confirm = DamoimStrings.SUB_CANCEL_CONFIRM, destructive = true,
                onConfirm = { confirmCancel = false; onCancel() }, onDismiss = { confirmCancel = false },
            )
        }
    }
}

@Composable
private fun Hero(sub: SubscriptionState) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.onDarkNavy).padding(22.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(sub.planName, style = DamoimTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.onPrimary, modifier = Modifier.weight(1f))
            if (sub.active) {
                Text(
                    if (sub.canceled) DamoimStrings.SUB_CANCELED_BADGE else DamoimStrings.SUB_ACTIVE_BADGE,
                    style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp),
                    color = if (sub.canceled) colors.onPrimary.copy(alpha = 0.6f) else colors.accentSky,
                    modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.onPrimary.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            HeroStat(DamoimStrings.SUB_MONTHLY_FEE, sub.monthlyPriceLabel)
            HeroStat(if (sub.canceled) DamoimStrings.SUB_USABLE_UNTIL else DamoimStrings.SUB_NEXT_BILLING, sub.nextBillingLabel)
            HeroStat(DamoimStrings.SUB_USAGE, "${sub.memberUsed}/${sub.memberLimit}")
        }
        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)).background(colors.onPrimary.copy(alpha = 0.15f))) {
            Box(Modifier.fillMaxWidth(sub.usageRatio).height(6.dp).clip(RoundedCornerShape(99.dp)).background(colors.accentSky))
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    val colors = DamoimTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.sp), color = colors.onPrimary.copy(alpha = 0.5f))
        Text(value, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp), color = colors.onPrimary)
    }
}

@Composable
private fun PaymentRow(p: PaymentRecord, showDivider: Boolean) {
    val colors = DamoimTheme.colors
    Column {
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(p.title, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.textPrimary)
                Text(p.dateLabel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Normal, fontSize = 11.sp), color = colors.textDisabled)
            }
            Text(p.amountLabel, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.5.sp), color = colors.textPrimary)
            Text(p.channel, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = colors.primaryDark, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(colors.primaryContainer).padding(horizontal = 8.dp, vertical = 4.dp))
        }
        if (showDivider) Box(Modifier.fillMaxWidth().height(1.dp).background(colors.surfaceDim))
    }
}
