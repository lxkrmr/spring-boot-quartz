# Spring boot and Quartz

This is a simple web application to test how we can integrate [quartz](http://www.quartz-scheduler.org/) in our spring boot web application.

## Current issues

* using the Scheduler annotation works, but no jobs appearing in the DB
* by default quartz does not start in cluster mode, so multiple instances will run the scheduled task which is not wanted

## Try yourself

Start the application at least twice from the terminal and override the port
    
    SERVER_PORT=8887 ./gradlew bootRun
    SERVER_PORT=8888 ./gradlew bootRun

Expected behaviour: The second instance of the service should not trigger the job, so you should not see messages like

`######## Running on port: 8888 at 2022-11-14T15:30:01.598440Z`

And the actuator should provide metrics regarding quartz, e.g.

`http://localhost:8887/actuator/quartz`
