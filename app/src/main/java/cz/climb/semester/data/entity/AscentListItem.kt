package cz.climb.semester.data.entity

data class AscentListItem(
    val id: String,
    val routeId: String,
    val routeName: String,
    val routeGrade: String,
    val sessionId: String?,
    val date: String,
    val style: String,
    val category: String,
    val success: Boolean,
    val notes: String,
    val createdAt: String,
    val updatedAt: String,
    val synced: Boolean,
)
