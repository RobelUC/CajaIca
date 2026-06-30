package pe.cmacica.clientes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class MenuOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color = Color.White,
    val badgeCount: Int = 0
)

@Composable
fun CircularMenuGrid(
    options: List<MenuOption>,
    onOptionClick: (MenuOption) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEach { option ->
                    CircularMenuItem(
                        option = option,
                        onClick = { onOptionClick(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowOptions.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CircularMenuItem(
    option: MenuOption,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            BadgedBox(
                badge = {
                    if (option.badgeCount > 0) {
                        Badge { Text(option.badgeCount.toString()) }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(option.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.label,
                        tint = option.iconTint,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        Text(
            text = option.label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            minLines = 2
        )
    }
}
