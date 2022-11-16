# Spring Boot and Quartz

# TL;DR

Don't do it!

We can't recommend using Quartz or at least `spring-boot-starter-quartz`.

## Try yourself

Start the application at least twice from the terminal and override the port

    SERVER_PORT=8887 ./gradlew bootRun
    SERVER_PORT=8888 ./gradlew bootRun

**Expected behaviour**: The second instance of the service should not trigger the job, so you should not see messages like

`######## Running on port: 8888 at 2022-11-14T15:30:01.598440Z`

And the actuator should provide metrics regarding quartz, e.g.

`http://localhost:8887/actuator/quartz`

## What we needed

In K8s we have multiple instances of our application.
Besides, of handling requests we also have the use-case of executing e.g. e clean up job every <foo> seconds.
But this job should only run on one instance at the time, so we needed something which can elect a leader which is
performing the job and makes sure that in case of an error another instance becomes the leader and will continue / restart
the job.

And we thought [Quartz](http://www.quartz-scheduler.org/) is the right choice.
And as we are using Spring Boot we decided to pick the [spring-boot-starter-quartz](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/io.html#io.quartz).

## Why we learned that we don't want to use Quartz

### Information are confusing / misleading

Important: Here I would like to point out that this topic became valid because of the `spring-boot-starter-quartz`

We are normal developers, so start to google stuff, and we found the following sources which did not always helped.

[Baeldung](https://www.baeldung.com/spring-quartz-schedule) is known by many developers and next to Stackoverflow maybe
one of the most often used sources - at least for me. 

But if you look through the article then you already see the misery.
Everything can be done in 2 different ways:

```Java
@Bean
public JobDetail jobDetail() {
    return JobBuilder.newJob().ofType(SampleJob.class)
      .storeDurably()
      .withIdentity("Qrtz_Job_Detail")  
      .withDescription("Invoke Sample Job service...")
      .build();
}
```

And then Spring offer another way to do **the same thing**:

```Java
@Bean
public JobDetailFactoryBean jobDetail() {
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(SampleJob.class);
    jobDetailFactory.setDescription("Invoke Sample Job service...");
    jobDetailFactory.setDurability(true);
    return jobDetailFactory;
}
```

**So as soon you try to google something, you get mixed up in all this different ways of doing the same thing. And from time to time you see that your way is not offering this details you would like to use ;(**

Plus some Information just did not work for us, like this

```Java
@Configuration
@EnableAutoConfiguration
public class SpringQrtzScheduler {

    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

Afterwards our application failed to start complaining about missing attributes (e.g. name, jdbc url, ...) for the datasource.

[Spring.io](https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/io.html#io.quartz) is sadly one of the saddest experience when it comes to documentation.
Look at the link, would you be able to set up a skeleton with this info?

And I don't want to blame the sources. We are thankful for all the effort people invested. So all I want to say is, that we struggled to build something as we were not able to reproduce the success of the others.

Other sources we found and tried to use:

* [Quartz Scheduler with SpringBoot](https://medium.com/@manvendrapsingh/quartz-scheduling-in-springboot-7cea1b7b19e7)
* [How to Schedule Jobs With Quartz in Spring Boot](https://hackernoon.com/how-to-schedule-jobs-with-quartz-in-spring-boot)
* [Spring Boot using Quartz in mode Cluster](https://medium.com/javarevisited/spring-boot-using-quartz-in-mode-cluster-e1d71e4af4b9)
* [Guide to Quartz with Spring Boot - Job Scheduling and Automation](https://stackabuse.com/guide-to-quartz-with-spring-boot-job-scheduling-and-automation/)
* [Scheduling in Spring with Quartz](https://www.baeldung.com/spring-quartz-schedule)
* [Quartz Tutorials](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/)

In the end this discussion on github helped us to realize that Spring Boot is already doing the needed config and we should not mess with it:

https://github.com/spring-projects/spring-framework/issues/27709

* using the Scheduler annotation works, but no jobs appearing in the DB
* by default quartz does not start in cluster mode, so multiple instances will run the scheduled task which is not wanted

### Additional pitfalls

1. Non concurrency is defined with the annotation `@DisallowConcurrentExecution`

As mentioned under `What we needed` we wanted to use Quartz to ensure that one Job is only executed once at a time on
one instance. To achieve this behavior one has to use the `@DisallowConcurrentExecution` annotation on the job like this.

**! Important**: If you forget the annotation each instance will execute the job!

```Kotlin
@DisallowConcurrentExecution
@Component
class JobWithQuartzJobBean(@Value("\${server.port}") private val port: String) : QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) =
        println("######## QUARTZ JOB BEAN: Running on port: $port at ${Instant.now()}")
}
```

Sadly this does not align with the way one has / can define the `JobDetails` and `Trigger`

```Kotlin
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
```

We would prefer to set `DisallowConcurrentExecution` in the Builder.
Or even better to opt in for an option to ensure that every Job is `DisallowConcurrentExecution`
by default, because we think it is very easy to overlook the annotation and introduce unwanted behaviour.

2. Updating jobs / triggers is hard

In the step before we saw that it could happen that we forget the `DisallowConcurrentExecution` annotation.
Or another scenario could be that we see that a Job should run more or less often.
So how can we update / replace job details and triggers.

Quartz provides code snippets for use-cases like this in the [Cookbook](http://www.quartz-scheduler.org/documentation/quartz-2.3.0/cookbook/).
For example here the snippet to update an existing job:

```Java
// Add the new job to the scheduler, instructing it to "replace"
//  the existing job with the given name and group (if any)
JobDetail job1 = newJob(MyJobClass.class)
    .withIdentity("job1", "group1")
    .build();

// store, and set overwrite flag to 'true'     
scheduler.addJob(job1, true);
```

The problem is, that with Spring Boot we don't configure the `scheduler` directly.
This happens with Spring Magic in the background.
So of course we could write logic to solve the issue, but it is cumbersome, and it felt strange to us to do an update this explicit.
E.g. should we do two deployments then? One containing the logic to update the job and then another one where this code can be replaced,
to keep the code base clean?

AND we had some unwanted side effects when messing around with the Job.
Somehow the job was triggered twice on an instance or multiple instances started the 
job and did not care anymore about the `DisallowConcurrentExecution` annotation.

FYI: We recommend to stay away from the following config, as it messed up our jobs:

```
spring.quartz.overwrite-existing-jobs = true
```

3. @Scheduled annotation does not work as expected

In the beginning it seemed that we could use the known annotation `@Scheduled` from Spring.
But this Job did not appear in the Quartz tables and therefor was not managed to run only on one instance.

## Summary

We decided to **not** use Quartz, as it introduced more complexity and did not reliably solve our problem.
Instead, we will use K8s to deploy a dedicated Pod using a `basic`` Spring Scheduler.
Not the best solution, but at least we don't have to wonder about issues in Quartz.

