package com.fcfs.firstcomefirstservedeventsystem.service

import com.fcfs.firstcomefirstservedeventsystem.repository.CouponRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
// @ExtendWith(MockitoExtension::class)
// TODO: 단위 테스트 작성해보기
class ApplyServiceTest {

    @Autowired
    private lateinit var applyService: ApplyService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    @Test
    @DisplayName("한 번만 쿠폰 발급")
    fun singleApplication() {

    }

    @Test
    @DisplayName("동시성 테스트 - 여러 명 응모")
    fun multipleApplications() {
        // 쿠폰 발급 결과 확인
    }
}
