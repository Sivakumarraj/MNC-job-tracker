package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM saved_jobs ORDER BY savedTimestamp DESC")
    fun getAllSavedJobs(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJob(job: JobEntity)

    @Update
    suspend fun updateSavedJob(job: JobEntity)

    @Query("DELETE FROM saved_jobs WHERE id = :jobId")
    suspend fun deleteSavedJob(jobId: String)

    @Query("SELECT * FROM scan_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentScanLogs(): Flow<List<ScanLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScanLog(log: ScanLogEntity)
}
