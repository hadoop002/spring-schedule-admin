## Spring Schedule Admin
Spring Schedule Admin简单、易用、轻量级的Spring定时任务web管理工具。无需修改代码，一个注解即可提供对定时任务的查看、启动、暂停、运行等功能。

## 实现原理
- 实现org.springframework.scheduling.annotation.SchedulingConfigurer接口，拿到ScheduledTaskRegistrar对象的实例。
- 从ScheduledTaskRegistrar实例中获取所有已经解析好的@Scheduled定时任务。
- 使用自定义的org.springframework.scheduling.TaskScheduler对所有定时任务进行接管，获取到所有任务执行结果的引用。
- 拿到定时任务的引用、执行结果的引用即可对定时任务进行执行、停止、启动等操作

## Requirements:
- Java 8 or Above
- Apache Maven 3.x

## Maven dependency
- Spring 4.3.x

## 适用Spring版本范围
- 支持4.3.x、5.0.x、5.1.x、5.2.x
- 不支持4.2.x
- <=4.1.x 没有测试

## 如何使用
在项目中引入maven依赖。最新版本已经deploy到maven的中央仓库了 [查看最新版](https://search.maven.org/search?q=a:spring-schedule-admin)
```xml
  <dependency>
      <groupId>com.github.hadoop002</groupId>
      <artifactId>spring-schedule-admin</artifactId>
      <version>使用最新版本</version>
  </dependency>
```
直接在启动类上加上 @EnableScheduleAdmin
```java
@EnableScheduleAdmin
@EnableScheduling
@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
```

## Job管理页面
启动应用，在浏览器中访问 /schedule/dashboard.html
![Job管理页面](https://tangshiyi.oss-cn-shenzhen.aliyuncs.com/public/SpringScheduleAdmin.png)

## 参数说明
- schedule.admin.enabled  是否启用Job管理功能，默认true

## 常见问题
- 1.1 访问Job管理页面/schedule/dashboard.html 404
  如果项目使用的是Springboot，一般没有这个问题。但是如果只是使用SpringMVC，则可能会有这个问题，解决方法如下
  ```java
      @Configuration
      public class AppWebMvcConfigurer extends WebMvcConfigurerAdapter {
      
          public void addResourceHandlers(ResourceHandlerRegistry registry) {
              registry.addResourceHandler("/schedule/**").addResourceLocations("classpath:/public/schedule/");
          }
      }
  ```
## 后期规划
- 管理页面增加权限控制

## 打包
mvn clean package
