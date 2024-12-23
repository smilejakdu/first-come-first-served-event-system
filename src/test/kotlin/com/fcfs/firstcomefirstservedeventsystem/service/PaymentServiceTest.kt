package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.domain.Payment
import com.fcfs.firstcomefirstservedeventsystem.producer.PaymentEventProducer
import com.fcfs.firstcomefirstservedeventsystem.repository.CouponCountRepository
import com.fcfs.firstcomefirstservedeventsystem.repository.PaymentRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class PaymentServiceTest {

    private val paymentRepository: PaymentRepository = mock(PaymentRepository::class.java)
    private val paymentEventProducer: PaymentEventProducer = mock(PaymentEventProducer::class.java)
    private val couponCountRepository: CouponCountRepository = mock(CouponCountRepository::class.java)
    private val paymentService = PaymentService(paymentRepository, paymentEventProducer, couponCountRepository)

    @Test
    @DisplayName("이벤트 처리 - 결제 완료")
    fun `processPayment saves payment and publishes event`() {
        // Arrange
        val userId = 1L
        val amount = 100.0
        val payment = Payment(userId, amount, "PENDING")
        `when`(paymentRepository.save(any(Payment::class.java))).thenAnswer { invocation ->
            val savedPayment = invocation.arguments[0] as Payment
            savedPayment.setIdForTest(1L) // ID 설정
            savedPayment
        }
        `when`(couponCountRepository.increment()).thenReturn(50L)

        // Act
        paymentService.processPayment(userId, amount)

        // Assert
        verify(paymentRepository, times(2)).save(any(Payment::class.java))
        verify(paymentEventProducer).publishPaymentEvent(1L)
    }
}
