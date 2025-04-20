Spacer(modifier = Modifier.height(16.dp))
OutlinedTextField(
    value = comment,
    onValueChange = { comment = it },
    label = { Text("Comments (optional)") },
    modifier = Modifier.fillMaxWidth()
)

confirmButton = {
    Button(onClick = { onSubmit(rating, comment) }) {
        Text("Submit")
    }
}

dismissButton = {
    TextButton(onClick = onDismiss) {
        Text("Cancel")
    }
}
