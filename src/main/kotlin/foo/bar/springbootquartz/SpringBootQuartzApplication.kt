package foo.bar.springbootquartz

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SpringBootQuartzApplication

fun main(args: Array<String>) {
    runApplication<SpringBootQuartzApplication>(*args)
}
