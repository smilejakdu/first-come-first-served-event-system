package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.domain.Coupon
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponCountRepository
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponRepository
import org.springframework.stereotype.Service

@Service
class ApplyService(
    private val couponRepository: CouponRepository,
    private val couponCountRepository: CouponCountRepository
) {

    fun apply(userId: Long): Coupon? {
//        val cnt = couponRepository.count()
        val cnt = couponCountRepository.increment()!!
        if (cnt > 100) {
            return null
        }
        return couponRepository.save(Coupon(userId))
    }
}