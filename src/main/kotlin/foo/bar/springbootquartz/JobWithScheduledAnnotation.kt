package foo.bar.springbootquartz

import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class JobWithScheduledAnnotation(@Value("\${server.port}") private val port: String) {

    @Scheduled(fixedRate = 5_000)
    fun portAndTime() = println("######## SCHEDULED ANNOTATION Running on port: $port at ${Instant.now()}")
}
