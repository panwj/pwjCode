package test.com.mvvm.dao

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "word_table")
data class Word (
    @PrimaryKey(autoGenerate = true)
    val id : Int,
    @ColumnInfo
    @NonNull
    val name : String
)