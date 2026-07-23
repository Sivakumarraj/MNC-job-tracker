package com.example.data.model

import java.util.UUID

enum class JobCategory(val displayName: String) {
    ALL("All Categories"),
    SOFTWARE_ENG("Software Engineering"),
    DATA_AI("Data & AI"),
    CLOUD_DEVOPS("Cloud & DevOps"),
    PRODUCT_MGMT("Product & Design"),
    CYBERSECURITY("Cybersecurity"),
    CONSULTING("Consulting & Biz")
}

enum class IndianTechHub(val displayName: String) {
    ALL("All India Hubs"),
    BENGALURU("Bengaluru"),
    HYDERABAD("Hyderabad"),
    PUNE("Pune"),
    GURGAON("Gurgaon / NCR"),
    CHENNAI("Chennai"),
    MUMBAI("Mumbai / Thane"),
    NOIDA("Noida")
}

data class MncCompany(
    val id: String,
    val name: String,
    val rank: Int,
    val headquarter: String,
    val IndiaEmployees: String,
    val careerUrl: String,
    val primaryColorHex: String,
    val badgeText: String,
    val keyHubs: List<IndianTechHub>,
    val description: String,
    val totalActiveJobs: Int = 120
)

data class JobListingItem(
    val id: String = UUID.randomUUID().toString(),
    val mncId: String,
    val companyName: String,
    val title: String,
    val location: IndianTechHub,
    val category: JobCategory,
    val experienceYears: String,
    val salaryRange: String,
    val postedTimeAgo: String,
    val careerUrl: String,
    val description: String,
    val requiredSkills: List<String>,
    val isNew: Boolean = false,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class ApplicationStatus(val label: String) {
    SAVED("Saved"),
    APPLIED("Applied"),
    INTERVIEWING("Interviewing"),
    OFFER("Offer Received"),
    REJECTED("Not Selected")
}

data class FeasibilityMetric(
    val title: String,
    val status: String, // "Free Viable", "Limited", "Requires Paid/Proxy"
    val isFeasibleOnFreeTier: Boolean,
    val summary: String,
    val technicalDetails: String,
    val mitigationStrategy: String
)
