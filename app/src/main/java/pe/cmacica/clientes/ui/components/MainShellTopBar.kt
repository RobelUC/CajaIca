package pe.cmacica.clientes.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.cmacica.clientes.R
import pe.cmacica.clientes.ui.theme.CajaIcaGold
import pe.cmacica.clientes.ui.theme.CajaIcaRed

@Composable
fun MainShellTopBar(
    isHome: Boolean,
    nombre: String,
    documento: String,
    iniciales: String,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 10.dp,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFE8C547),
                            CajaIcaGold,
                            Color(0xFF9A7B1A)
                        )
                    )
                )
        ) {
            HeaderBubble(
                size = 150.dp,
                offsetX = (-48).dp,
                offsetY = (-56).dp,
                alpha = 0.12f
            )
            HeaderBubble(
                size = 90.dp,
                offsetX = 260.dp,
                offsetY = 8.dp,
                alpha = 0.09f
            )
            HeaderBubble(
                size = 52.dp,
                offsetX = 180.dp,
                offsetY = (-16).dp,
                alpha = 0.07f
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isHome) {
                            Text(
                                text = stringResource(R.string.dashboard_title).uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.78f),
                                letterSpacing = 1.4.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(
                                    R.string.dashboard_greeting,
                                    nombre.ifBlank { "Cliente" }
                                ),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (documento.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                DocumentoChip(documento = documento)
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.nav_services),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.services_header_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }

                    ProfileAvatarButton(
                        iniciales = iniciales,
                        onClick = onOpenProfile,
                        showInitials = isHome
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                CajaIcaRed.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun HeaderBubble(
    size: Dp,
    offsetX: Dp,
    offsetY: Dp,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .offset(x = offsetX, y = offsetY)
            .size(size)
            .background(Color.White.copy(alpha = alpha), CircleShape)
    )
}

@Composable
private fun DocumentoChip(documento: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Color.White.copy(alpha = 0.92f), CircleShape)
            )
            Text(
                text = stringResource(R.string.dashboard_document_chip, documento),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileAvatarButton(
    iniciales: String,
    onClick: () -> Unit,
    showInitials: Boolean
) {
    IconButton(onClick = onClick) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.22f),
            border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier.size(46.dp),
                contentAlignment = Alignment.Center
            ) {
                if (showInitials && iniciales.isNotBlank()) {
                    Text(
                        text = iniciales,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.PersonOutline,
                        contentDescription = stringResource(R.string.action_profile),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
