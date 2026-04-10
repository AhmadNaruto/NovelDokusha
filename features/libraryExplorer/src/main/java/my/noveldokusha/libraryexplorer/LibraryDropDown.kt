package my.noveldokusha.libraryexplorer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun LibraryDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onManageCategories: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.Category, stringResource(id = R.string.manage_categories))
            },
            text = { Text(stringResource(id = R.string.manage_categories)) },
            onClick = {
                onManageCategories()
                onDismiss()
            }
        )
    }
}