package com.fcfs.firstcomefirstservedeventsystem.consumer

import com.fcfs.firstcomefirstservedeventsystem.repository.CouponRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CouponCreatedConsumer(
    private val couponRepository: CouponRepository,
) {

    @KafkaListener(topics = ["coupon_create"], groupId = "group_1")
    fun listener(userId: Long) {
        println("userId: $userId")
    }
}