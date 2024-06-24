package com.fcfs.firstcomefirstservedeventsystem.repository

import com.fcfs.firstcomefirstservedeventsystem.domain.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CouponRepository: JpaRepository<Coupon,Long> {
}