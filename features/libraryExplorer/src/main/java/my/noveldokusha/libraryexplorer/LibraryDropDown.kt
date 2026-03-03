package my.noveldokusha.libraryexplorer

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable

@Composable
internal fun LibraryDropDown(
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // No menu items
    }
}
