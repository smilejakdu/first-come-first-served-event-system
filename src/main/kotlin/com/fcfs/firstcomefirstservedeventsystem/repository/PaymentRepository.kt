package com.fcfs.firstcomefirstservedeventsystem.repository

import com.fcfs.firstcomefirstservedeventsystem.domain.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>