package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.producer.CouponCreateProducer
import com.fcfs.firstcomefirstservedeventsystem.repository.AppliedUserRepository
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponCountRepository
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponRepository
import org.springframework.stereotype.Service

@Service
class ApplyService(
    private val couponRepository: CouponRepository,
    private val couponCountRepository: CouponCountRepository,
    private val couponCreateProducer: CouponCreateProducer,
    private val appliedUserRepository: AppliedUserRepository
) {

//    fun apply(userId: Long) {
////        val cnt = couponRepository.count()
//        val cnt = couponCountRepository.increment()!!
//        if (cnt > 100) {
//            return
//        }
//        couponCreateProducer.create(userId)
////        return couponRepository.save(Coupon(userId))
//    }

    fun apply(userId: Long) {
        val apply = appliedUserRepository.add(userId)

        if (apply != 1L) {
            return
        }

        val count = couponCountRepository.increment()

        if (count!! > 100L) {
            return
        }

        couponCreateProducer.create(userId)
    }
}