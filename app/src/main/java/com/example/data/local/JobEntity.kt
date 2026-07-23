package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_jobs")
data class JobEntity(
    @PrimaryKey val id: String,
    val mncId: String,
    val companyName: String,
    val title: String,
    val location: String,
    val category: String,
    val experienceYears: String,
    val salaryRange: String,
    val careerUrl: String,
    val description: String,
    val requiredSkillsCsv: String,
    val status: String = "Saved",
    val notes: String = "",
    val savedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "scan_logs")
data class ScanLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mncsChecked: Int,
    val newJobsFound: Int,
    val summary: String
)
