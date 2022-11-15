package foo.bar.springbootquartz

import org.quartz.*
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class JobConfig {

    @Bean
    @QuartzDataSource
    fun quartzDataSource(): DataSource = DataSourceBuilder.create().build()

    @Bean
    fun aJobWithQuartzJobBean(): JobDetail = JobBuilder
        .newJob(JobWithQuartzJobBean::class.java)
        .withIdentity("JobWithQuartzJobBean")
        .storeDurably()
        .build()

    @Bean
    fun aTriggerForJobWithQuartzJobBean(): Trigger = TriggerBuilder
        .newTrigger()
        .forJob("JobWithQuartzJobBean")
        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever())
        .build()
}
