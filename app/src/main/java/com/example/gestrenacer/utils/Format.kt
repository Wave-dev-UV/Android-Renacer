package com.example.gestrenacer.utils

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Format {
    companion object{
        fun timestampToString(timestamp: Timestamp): String{
            val date: Date = timestamp.toDate()
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return format.format(date)
        }
    }
}