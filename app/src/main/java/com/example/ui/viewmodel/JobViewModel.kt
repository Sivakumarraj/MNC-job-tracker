package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.JobEntity
import com.example.data.local.ScanLogEntity
import com.example.data.model.FeasibilityMetric
import com.example.data.model.IndianTechHub
import com.example.data.model.JobCategory
import com.example.data.model.JobListingItem
import com.example.data.model.MncCompany
import com.example.data.repository.MncRepository
import com.example.service.GeminiClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val label: String, val iconResName: String) {
    DIRECTORY("Top 25 MNCs", "ic_business"),
    LIVE_JOBS("5-Min Feed", "ic_radar"),
    AI_MATCHER("Gemini AI Match", "ic_spark"),
    SAVED("Saved Jobs", "ic_bookmark"),
    FEASIBILITY("Feasibility Report", "ic_report")
}

class JobViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MncRepository(application)

    val topMncs: List<MncCompany> = repository.top25MNCs

    private val _selectedTab = MutableStateFlow(AppNavTab.DIRECTORY)
    val selectedTab: StateFlow<AppNavTab> = _selectedTab

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedHub = MutableStateFlow(IndianTechHub.ALL)
    val selectedHub: StateFlow<IndianTechHub> = _selectedHub

    private val _selectedCategory = MutableStateFlow(JobCategory.ALL)
    val selectedCategory: StateFlow<JobCategory> = _selectedCategory

    // 5-minute countdown state (300 seconds)
    private val _countdownSeconds = MutableStateFlow(300)
    val countdownSeconds: StateFlow<Int> = _countdownSeconds

    private val _isAutoScanActive = MutableStateFlow(true)
    val isAutoScanActive: StateFlow<Boolean> = _isAutoScanActive

    private val _isScanningNow = MutableStateFlow(false)
    val isScanningNow: StateFlow<Boolean> = _isScanningNow

    private var timerJob: Job? = null

    // AI Matcher State
    private val _resumeText = MutableStateFlow(
        "Experienced Android Developer with 4 years in Kotlin, Jetpack Compose, Coroutines, Room DB, Retrofit, and Clean Architecture. Built high-traffic e-commerce and fintech mobile applications."
    )
    val resumeText: StateFlow<String> = _resumeText

    private val _aiTargetJobTitle = MutableStateFlow("Senior Android Engineer at Google India")
    val aiTargetJobTitle: StateFlow<String> = _aiTargetJobTitle

    private val _aiResultText = MutableStateFlow("")
    val aiResultText: StateFlow<String> = _aiResultText

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    val savedJobsFlow: StateFlow<List<JobEntity>> = repository.savedJobsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scanLogsFlow: StateFlow<List<ScanLogEntity>> = repository.scanLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredJobs: StateFlow<List<JobListingItem>> = combine(
        repository.liveJobsState,
        _searchQuery,
        _selectedHub,
        _selectedCategory
    ) { jobs, query, hub, cat ->
        jobs.filter { job ->
            val matchesQuery = query.isBlank() ||
                    job.companyName.contains(query, ignoreCase = true) ||
                    job.title.contains(query, ignoreCase = true) ||
                    job.description.contains(query, ignoreCase = true)

            val matchesHub = (hub == IndianTechHub.ALL) || (job.location == hub)
            val matchesCat = (cat == JobCategory.ALL) || (job.category == cat)

            matchesQuery && matchesHub && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        start5MinCountdownTimer()
    }

    fun selectTab(tab: AppNavTab) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectHub(hub: IndianTechHub) {
        _selectedHub.value = hub
    }

    fun selectCategory(cat: JobCategory) {
        _selectedCategory.value = cat
    }

    fun toggleAutoScan() {
        _isAutoScanActive.value = !_isAutoScanActive.value
    }

    fun setResumeText(text: String) {
        _resumeText.value = text
    }

    fun setAiTargetJobTitle(title: String) {
        _aiTargetJobTitle.value = title
    }

    fun runScanNow() {
        viewModelScope.launch {
            _isScanningNow.value = true
            _countdownSeconds.value = 300 // Reset timer
            repository.triggerAutoScan()
            delay(800)
            _isScanningNow.value = false
        }
    }

    private fun start5MinCountdownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isAutoScanActive.value) {
                    if (_countdownSeconds.value > 1) {
                        _countdownSeconds.value = _countdownSeconds.value - 1
                    } else {
                        // Trigger 5-minute scan cycle
                        _isScanningNow.value = true
                        repository.triggerAutoScan()
                        _countdownSeconds.value = 300
                        delay(500)
                        _isScanningNow.value = false
                    }
                }
            }
        }
    }

    fun saveJob(job: JobListingItem) {
        viewModelScope.launch {
            repository.saveJobToDb(job)
        }
    }

    fun deleteSavedJob(jobId: String) {
        viewModelScope.launch {
            repository.deleteSavedJob(jobId)
        }
    }

    fun analyzeWithGemini() {
        viewModelScope.launch {
            _isAiLoading.value = true
            _aiResultText.value = "Gemini is analyzing your candidate profile against $aiTargetJobTitle..."

            val prompt = """
                Act as an expert technical recruiter and talent advisor for top MNCs in India (Google, TCS, Infosys, Amazon, Microsoft).
                Target Job: ${_aiTargetJobTitle.value}
                Candidate Resume/Skills Summary: ${_resumeText.value}
                
                Please generate a structured analysis including:
                1. Match Percentage (0-100%)
                2. Key Matched Strengths
                3. Critical Missing Skills / Keywords for MNC ATS systems
                4. Tailored 3-paragraph Cover Letter / Pitch to Hiring Manager
                5. Top 3 Technical Interview Questions for this specific role in India.
            """.trimIndent()

            val response = GeminiClient.promptGemini(prompt)
            _aiResultText.value = response
            _isAiLoading.value = false
        }
    }

    fun getFeasibilityReport(): List<FeasibilityMetric> {
        return repository.getFeasibilityReport()
    }
}
