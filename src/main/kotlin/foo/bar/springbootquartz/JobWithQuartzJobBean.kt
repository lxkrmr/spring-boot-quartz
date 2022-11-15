package foo.bar.springbootquartz

import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.Instant

class JobWithQuartzJobBean(@Value("\${server.port}") private val port: String) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) =
        println("######## ${this.javaClass.name} Running on port: $port at ${Instant.now()}")
}
