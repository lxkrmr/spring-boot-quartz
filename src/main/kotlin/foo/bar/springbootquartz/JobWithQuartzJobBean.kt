package foo.bar.springbootquartz

import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component
import java.time.Instant

@DisallowConcurrentExecution
@Component
class JobWithQuartzJobBean(@Value("\${server.port}") private val port: String) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) =
        println("######## QUARTZ JOB BEAN: Running on port: $port at ${Instant.now()}")
}
