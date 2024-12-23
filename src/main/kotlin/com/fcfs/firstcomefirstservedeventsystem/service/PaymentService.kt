package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.domain.Payment
import com.fcfs.firstcomefirstservedeventsystem.producer.PaymentEventProducer
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponCountRepository
import com.fcfs.firstcomefirstservedeventsystem.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentEventProducer: PaymentEventProducer,
    private val couponCountRepository: CouponCountRepository
) {

    @Transactional
    fun processPayment(userId: Long, amount: Double) {
        // Step 1: Check coupon availability
        val count = couponCountRepository.increment()
        if (count != null && count > 100L) {
            throw IllegalStateException("Coupons are no longer available")
        }

        // Step 2: Save payment record
        val payment = Payment(userId, amount, "PENDING")
        paymentRepository.save(payment)

        // Step 3: Publish payment event with payment ID
        try {
            paymentEventProducer.publishPaymentEvent(payment.id ?: throw IllegalStateException("Payment ID cannot be null"))
        } catch (ex: Exception) {
            throw RuntimeException("Failed to publish payment event", ex)
        }

        // Step 4: Update payment status
        payment.status = "COMPLETED"
        paymentRepository.save(payment)
    }
}
