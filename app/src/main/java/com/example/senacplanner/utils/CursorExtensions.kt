package com.example.senacplanner.utils

import android.database.Cursor

fun Cursor.getStringOrNull(columnName: String): String? {
    val index = getColumnIndexOrThrow(columnName)
    return if (isNull(index)) null else getString(index)
}
