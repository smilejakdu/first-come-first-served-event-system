package com.fcfs.firstcomefirstservedeventsystem.producer

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PaymentEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, Long>
) {
    fun publishPaymentEvent(paymentId: Long) {
        kafkaTemplate.send("payment_events", paymentId)
    }
}
