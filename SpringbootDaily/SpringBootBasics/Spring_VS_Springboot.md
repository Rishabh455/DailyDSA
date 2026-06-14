So, in Spring, we faced a few challenges. First, when we created beans, we had to manually register them using XML. Second, we had to explicitly search for beans we needed. Lastly, we had to configure everything manually—the dispatcher servlet, view resolvers, object mapper, transaction manager, even the Tomcat server. This infrastructure setup was quite heavy compared to business logic.

Now, Spring Boot solved these issues. With @Configuration, we just annotate and beans are registered automatically—no XML needed. With @ComponentScan, beans like services or repositories are automatically detected, again without XML. Finally, @EnableAutoConfiguration does the magic—based on dependencies like Spring Boot Web, it automatically configures things like the dispatcher servlet. If something is missing, it will set it up. In short, I no longer need to handle infrastructure manually; Spring Boot does it for me.
/////////

In traditional Spring, developers spend significant time configuring infrastructure such as DispatcherServlet, ViewResolver, DataSource, TransactionManager, and Tomcat. Spring Boot eliminates this overhead through auto-configuration, starter dependencies, and embedded servers. As a result, developers can focus more on business logic rather than configuration, leading to faster development, easier deployment, and better support for microservices architectures.

///
Why Spring Boot over Spring? (Interview Answer)

Auto Configuration → Manual configuration ki zaroorat nahi padti.
Starter Dependencies → Ek dependency add karo, required libraries automatically aa jati hain.
Embedded Server → Tomcat alag se install/deploy nahi karna padta.
Less Boilerplate Code → XML aur configuration code bahut kam ho jata hai.
Production Ready Features → Actuator, health checks, metrics, monitoring built-in milte hain.
Faster Development → Infrastructure setup ki jagah business logic par focus kar sakte hain.