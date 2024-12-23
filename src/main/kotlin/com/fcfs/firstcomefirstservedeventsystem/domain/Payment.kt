package com.fcfs.firstcomefirstservedeventsystem.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Payment(
    var userId: Long,
    var amount: Double,
    var status: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        private set

    // Test-only method to set ID
    fun setIdForTest(id: Long) {
        this.id = id
    }
}