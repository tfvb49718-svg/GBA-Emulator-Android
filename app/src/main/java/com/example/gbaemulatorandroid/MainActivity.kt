package com.example.gbaemulatorandroid

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private val Ink = Color(0xFF20231F)
private val Muted = Color(0xFF777D75)
private val Green = Color(0xFF427A5B)
private val Canvas = Color(0xFFF6F7F2)
private val Card = Color.White

private data class Todo(val id: String, val title: String, val completed: Boolean = false)
private enum class TodoFilter { All, Active, Completed }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Canvas) {
                    TodoScreen(applicationContext)
                }
            }
        }
    }
}

@Composable
private fun TodoScreen(context: Context) {
    var todos by remember { mutableStateOf(loadTodos(context)) }
    var draft by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TodoFilter.All) }
    val focusManager = LocalFocusManager.current

    fun save(updated: List<Todo>) {
        todos = updated
        persistTodos(context, updated)
    }

    val visibleTodos = todos.filter {
        when (filter) {
            TodoFilter.All -> true
            TodoFilter.Active -> !it.completed
            TodoFilter.Completed -> it.completed
        }
    }
    val remaining = todos.count { !it.completed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(52.dp))
        Text("YOUR DAY,\nIN GOOD ORDER.", color = Ink, fontSize = 31.sp, lineHeight = 37.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Text("A little progress adds up.", color = Muted, fontSize = 16.sp)
        Spacer(Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Card)
                .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                singleLine = true,
                textStyle = TextStyle(color = Ink, fontSize = 16.sp),
                decorationBox = { inner ->
                    if (draft.isEmpty()) Text("What needs doing?", color = Muted, fontSize = 16.sp)
                    inner()
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    val title = draft.trim()
                    if (title.isNotEmpty()) {
                        save(listOf(Todo(UUID.randomUUID().toString(), title)) + todos)
                        draft = ""
                        focusManager.clearFocus()
                    }
                })
            )
            Button(
                onClick = {
                    val title = draft.trim()
                    if (title.isNotEmpty()) {
                        save(listOf(Todo(UUID.randomUUID().toString(), title)) + todos)
                        draft = ""
                        focusManager.clearFocus()
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 12.dp)
            ) { Text("Add", fontWeight = FontWeight.SemiBold) }
        }

        Spacer(Modifier.height(26.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("YOUR TASKS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(Modifier.weight(1f))
            if (todos.any { it.completed }) {
                TextButton(onClick = { save(todos.filterNot { it.completed }) }) {
                    Text("Clear done", color = Green, fontSize = 13.sp)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TodoFilter.entries.forEach { option ->
                val selected = filter == option
                Text(
                    text = option.name,
                    color = if (selected) Color.White else Muted,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selected) Ink else Color.Transparent)
                        .clickable { filter = option }
                        .padding(horizontal = 15.dp, vertical = 9.dp)
                )
            }
        }

        if (visibleTodos.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.TopCenter) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 46.dp)) {
                    Text(if (todos.isEmpty()) "✦" else "✓", color = Green, fontSize = 32.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(if (todos.isEmpty()) "Nothing on your list yet" else "Nothing to see here", color = Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (todos.isEmpty()) "Add a task above to get started." else "Try another filter.", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(visibleTodos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        onToggle = { save(todos.map { if (it.id == todo.id) it.copy(completed = !it.completed) else it }) },
                        onDelete = { save(todos.filterNot { it.id == todo.id }) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("$remaining ${if (remaining == 1) "task" else "tasks"} left", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TodoRow(todo: Todo, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Card)
            .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = todo.completed, onCheckedChange = { onToggle() })
        Text(
            text = todo.title,
            modifier = Modifier.weight(1f).padding(start = 6.dp),
            color = if (todo.completed) Muted else Ink,
            fontSize = 15.sp,
            textDecoration = if (todo.completed) TextDecoration.LineThrough else TextDecoration.None
        )
        Text(
            text = "×",
            color = Muted,
            fontSize = 24.sp,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onDelete)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun loadTodos(context: Context): List<Todo> {
    val saved = context.getSharedPreferences("daily_tasks", Context.MODE_PRIVATE).getString("todos", null)
        ?: return emptyList()
    return runCatching {
        val array = JSONArray(saved)
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            Todo(
                id = item.getString("id"),
                title = item.getString("title"),
                completed = item.optBoolean("completed", false)
            )
        }
    }.getOrDefault(emptyList())
}

private fun persistTodos(context: Context, todos: List<Todo>) {
    val array = JSONArray()
    todos.forEach { todo ->
        array.put(JSONObject().apply {
            put("id", todo.id)
            put("title", todo.title)
            put("completed", todo.completed)
        })
    }
    context.getSharedPreferences("daily_tasks", Context.MODE_PRIVATE)
        .edit()
        .putString("todos", array.toString())
        .apply()
}
