# first-come-first-served-event-system

## docker
### docker install
```shell
brew install docker
brew link docker
docker version
```

### run mysql using docker
```shell
docker pull mysql
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=1234 --name mysql mysql
docker ps
docker exec -it mysql bash
mysql -u root -p
create database coupon;
```

### run redis using docker

```shell
docker pull redis
docker run -d -p 6379:6379 --name redis redis
docker ps
docker exec -it redis bash
```

## Requirements

```markdown
- The system must be able to handle sudden spikes in traffic.
- No more than 101 must be paid.
- It must be able to withstand sudden surges of traffic.
```

write down the test code for create coupon
and then run the test code

```kotlin
@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private lateinit var applyService: ApplyService

    @Autowired
    private lateinit var couponRepository: CouponRepository

    @Test
    @DisplayName("Multiple Applications")
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
```

maybe you get response like this

```shell
Expected :100
Actual   :116
```

We expected only 100 items to be generated, but we have confirmed that this is not the case.
Why is this happening?
Race condition occurs when multiple threads access the same resource at the same time.
A race condition occurs when two or more threads access and attempt to work on shared resources simultaneously.

```kotlin
// CouponCountRepository.kt
@Repository
class CouponCountRepository(
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun increment(): Long? {
        return redisTemplate
            .opsForValue()
            .increment("coupon_count")
    }
}

// ApplyService.kt
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
```

The problem is that the increment method is not atomic.  
The increment method is not atomic, so it is possible for multiple threads to access the same resource at the same time.  
After modifying the code as described above and running the test cases again,  
we can see that the issue is resolved. The reason is that Redis operates on a single-threaded basis.  
Even if multiple threads are running, Thread 2 will wait for Thread 1 to complete its work before it starts executing.  

## Requirements Two
Let's assume that MySQL can handle 100 inserts per minute.  

At 10:00 AM, there is a request to generate 10,000 coupons.  
At 10:01 AM, there is a request to create orders.  
At 10:02 AM, there is a request for user registrations.  

When a large number of requests are made in a short period, it can lead to heavy usage of the DB server's resources.  
This load can cause delays or even errors in the service.  

### kafka docker
docker-compose.yml
```shell
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka:2.12-2.5.0
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

```shell
docker-compose up -d
docker ps
```

### kafka
The basic structure of Kafka is  
It consists of Producer, Topic, and Consumer.  
Topic is similar to Queue.  
And the producer inserts data into the topic.  
The Consumer is the one that takes the data inserted into the Topic.  

So, Kafka is a platform that helps stream data from producer to consumer to its destination in real time.  

create topic
```shell
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic testTopic
```
run producer
```shell
docker exec -it kafka kafka-console-producer.sh --topic testTopic --broker-list 0.0.0.0:9092
```
run consumer
```shell
docker exec -it kafka kafka-console-consumer.sh --topic testTopic --bootstrap-server localhost:9092
```

after running the producer, you can see the message in the consumer.
```shell
> hello

hello
```

## Requirements Three

> config > KafkaProducerConfig.kt
```kotlin
@Configuration
class KafkaProducerConfig {

    @Bean
    fun producerFactory(): ProducerFactory<String, Long> {
        val config = hashMapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to LongSerializer::class.java
        )
        return DefaultKafkaProducerFactory(config)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Long> {
        return KafkaTemplate(producerFactory())
    }
}
```

> producer > KafkaProducer.kt
```kotlin
@Component
class CouponCreateProducer(
    private val kafkaTemplate: KafkaTemplate<String, Long>
) {

    fun create(userId: Long) {
        kafkaTemplate.send("coupon_create", userId)
    }
}
```

> service > ApplyService.kt
```kotlin
@Service
class ApplyService(
    private val couponRepository: CouponRepository,
    private val couponCountRepository: CouponCountRepository,
    private val couponCreateProducer: CouponCreateProducer
) {

    fun apply(userId: Long) {
//        val cnt = couponRepository.count()
        val cnt = couponCountRepository.increment()!!
        if (cnt > 100) {
            return
        }
        couponCreateProducer.create(userId)
//        return couponRepository.save(Coupon(userId))
    }
}
```

consumer > CouponCreatedConsumer.kt
```kotlin
@Component
class CouponCreatedConsumer(
    private val couponRepository: CouponRepository,
) {

    @KafkaListener(topics = ["coupon_create"], groupId = "group_1")
    fun listener(userId: Long) {
        println("userId: $userId")
    }
}
```

create topic
```shell
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --create --topic coupon_create
```

topic list
```shell
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

remove topic
```shell
docker exec -it kafka kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic coupon_create
```

run producer
```shell
docker exec -it kafka kafka-console-producer.sh --topic testTopic --broker-list 0.0.0.0:9092
```

run consumer
```shell
docker exec -it kafka kafka-console-consumer.sh --topic coupon_create --bootstrap-server localhost:9092 --key-deserializer "org.apache.kafka.common.serialization.StringDeserializer" --value-deserializer "org.apache.kafka.common.serialization.LongDeserializer"
```

If you execute as above, a print will be taken.
```markdown
2024-06-24T18:19:14.490+09:00  INFO 42384 --- [ntainer#0-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : group_1: partitions assigned: [coupon_create-0]
userId: 23
userId: 28
userId: 3
userId: 21
userId: 17
userId: 9
userId: 31
userId: 13
userId: 29
userId: 10
userId: 20
userId: 2
userId: 12

....
```


# Conclusion
```markdown
resolves race condition for issuing 100 items due to redis single-threaded nature  
however load may occur on DB throughput when inserting  
through kafka middleware, 100 coupon storage events can be stored and the consumer can take them out  
and process them when desired, thereby distributing the processing load.  
you don't have to issue coupons right away, so if events are well issued in Kafka, you can take them out  
and save them in the early morning when db workload is less.

You don't have to issue coupons right away, so you can take them out and save them when DB workload is low. though kafka
```