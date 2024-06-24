package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.repository.CouponRepository
import org.junit.jupiter.api.DisplayName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private lateinit var applyService: ApplyService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    @Test
    @DisplayName("한번만 쿠폰을 발급")
    fun apply() {
        val userId = 1L
        val coupon = applyService.apply(userId)

        assertEquals(1, coupon?.userId)
    }

    @Test
    @DisplayName("여러 명 응모")
    @Throws(InterruptedException::class)
    fun multipleApplications() {
        val threadCount = 1000
        val executorService: ExecutorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        for (i in 0 until threadCount) {
            val userId = i.toLong()
            executorService.submit {
                try {
                    applyService.apply(userId)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()

        Thread.sleep(10000)

        val count = couponRepository.count()

        assertEquals(100, count)
    }
}