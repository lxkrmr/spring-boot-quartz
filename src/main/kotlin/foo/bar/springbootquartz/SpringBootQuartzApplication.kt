package foo.bar.springbootquartz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SpringBootQuartzApplication

fun main(args: Array<String>) {
    runApplication<SpringBootQuartzApplication>(*args)
}
