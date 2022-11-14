package foo.bar.springbootquartz

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SimpleTask {

    @Scheduled(fixedRate = 5_000)
    fun printAString() = println("######## This is a string at ${Instant.now()}")
}
