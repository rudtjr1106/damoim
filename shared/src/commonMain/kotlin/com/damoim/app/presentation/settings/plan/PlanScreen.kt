package com.damoim.app.presentation.settings.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damoim.app.core.di.AppGraph
import com.damoim.app.domain.model.BillingProducts
import com.damoim.app.domain.model.PlanTier
import com.damoim.app.domain.model.SubscriptionPlan
import com.damoim.app.platform.BillingResult
import com.damoim.app.platform.rememberSubscriptionBilling
import com.damoim.app.presentation.component.CheckIcon
import com.damoim.app.presentation.component.CloseIcon
import com.damoim.app.presentation.component.noRippleClick
import com.damoim.app.presentation.settings.SettingsTopBar
import com.damoim.app.presentation.theme.DamoimStrings
import com.damoim.app.presentation.theme.DamoimTheme

@Composable
fun PlanRoute(
    viewModel: PlanViewModel = viewModel(key = "plan") { PlanViewModel(AppGraph.subscriptionUseCase) },
    onBack: () -> Unit = {},
    onPaySuccess: () -> Unit = {},
    onPayFail: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val billing = rememberSubscriptionBilling()
    PlanScreen(
        state = state,
        onBack = onBack,
        onStart = { plan ->
            billing(BillingProducts.forTier(plan.tier), "${plan.priceLabel} / 월") { result ->
                when (result) {
                    BillingResult.SUCCESS -> { viewModel.subscribe(plan.tier); onPaySuccess() }
                    BillingResult.FAILURE -> onPayFail()
                    BillingResult.CANCELLED -> {}
                }
            }
        },
    )
}

@Composable
fun PlanScreen(
    state: PlanUiState = PlanUiState(),
    onBack: () -> Unit = {},
    onStart: (SubscriptionPlan) -> Unit = {},
) {
    val colors = DamoimTheme.colors
    Column(Modifier.fillMaxSize().background(colors.surface)) {
        SettingsTopBar(DamoimStrings.PLAN_TITLE, onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(DamoimStrings.PLAN_HEADING, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 31.sp), color = colors.textPrimary)
                Text(DamoimStrings.planSubtitle(state.memberCount), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted)
            }
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.plans.forEach { plan ->
                    PlanCard(plan, isCurrent = plan.tier == state.currentTier, onStart = { onStart(plan) })
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: SubscriptionPlan, isCurrent: Boolean, onStart: () -> Unit) {
    val colors = DamoimTheme.colors
    val highlighted = plan.recommended && !isCurrent
    val showCta = !isCurrent && plan.tier != PlanTier.FREE
    Box {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(colors.surface)
                .border(if (highlighted) 2.dp else 1.dp, if (highlighted) colors.primary else colors.divider, RoundedCornerShape(20.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(plan.name, style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp), color = if (highlighted) colors.primaryDark else colors.textPrimary, modifier = Modifier.weight(1f))
                if (isCurrent) Text(DamoimStrings.PLAN_CURRENT_BADGE, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = colors.textMuted, modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(colors.surfaceVariant).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(plan.priceLabel, style = DamoimTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp), color = colors.textPrimary)
                Text(DamoimStrings.PLAN_PER_MONTH, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = colors.textMuted, modifier = Modifier.padding(bottom = 4.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                plan.features.forEach { f ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (f.included) CheckIcon(colors.primary, Modifier.size(14.dp)) else CloseIcon(colors.outlineStrong, Modifier.size(14.dp))
                        Text(f.text, style = DamoimTheme.typography.caption.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp), color = if (f.included) colors.textTertiary else colors.textDisabled)
                    }
                }
            }
            if (showCta) {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.primary).noRippleClick(onStart).padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text(DamoimStrings.planStartCta(plan.name), style = DamoimTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp), color = colors.onPrimary)
                }
            }
        }
        if (highlighted) {
            Text(DamoimStrings.PLAN_RECOMMEND_BADGE, style = DamoimTheme.typography.label.copy(fontWeight = FontWeight.ExtraBold, fontSize = 11.sp), color = colors.onPrimary, modifier = Modifier.offset(x = 20.dp, y = (-11).dp).clip(RoundedCornerShape(999.dp)).background(colors.primary).padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}
