package my.noveldokusha.libraryexplorer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import my.noveldokusha.feature.local_database.tables.LibraryCategory

@Composable
internal fun ManageCategoriesDialog(
    viewModel: LibraryPageViewModel,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<LibraryCategory?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.manage_categories)) },
        text = {
            LazyColumn {
                item {
                    TextButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Text(" " + stringResource(id = R.string.add_category))
                    }
                }
                items(viewModel.categoriesFlow.value) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.name,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editingCategory = category }) {
                            Icon(Icons.Default.Edit, null)
                        }
                        IconButton(onClick = { 
                            scope.launch { viewModel.deleteCategory(category) }
                        }) {
                            Icon(Icons.Default.Delete, null)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.ok))
            }
        }
    )

    if (showAddDialog) {
        CategoryNameDialog(
            title = stringResource(id = R.string.add_category),
            onSave = { name ->
                scope.launch { viewModel.createCategory(name) }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    if (editingCategory != null) {
        CategoryNameDialog(
            title = stringResource(id = R.string.edit_category),
            initialName = editingCategory!!.name,
            onSave = { name ->
                scope.launch { viewModel.updateCategory(editingCategory!!.id, name) }
                editingCategory = null
            },
            onDismiss = { editingCategory = null }
        )
    }
}

@Composable
private fun CategoryNameDialog(
    title: String,
    initialName: String = "",
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.category_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(id = android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = android.R.string.cancel))
            }
        }
    )
}
