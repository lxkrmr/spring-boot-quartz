package foo.bar.springbootquartz

import org.quartz.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JobConfig {

    @Bean
    fun aJobWithQuartzJobBean(): JobDetail = JobBuilder
        .newJob(JobWithQuartzJobBean::class.java)
        .withIdentity("JobWithQuartzJobBean")
        .storeDurably()
        .build()

    @Bean
    fun aTriggerForJobWithQuartzJobBean(): Trigger = TriggerBuilder
        .newTrigger()
        .withIdentity("TriggerForJobWithQuartzJobBean")
        .forJob("JobWithQuartzJobBean")
        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(4))
        .build()
}
