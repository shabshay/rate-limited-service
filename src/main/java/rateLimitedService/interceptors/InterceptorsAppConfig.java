package rateLimitedService.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class InterceptorsAppConfig implements WebMvcConfigurer {
    @Autowired
    RateLimiterInterceptor serviceInterceptor;

    @Value("${server.requests.limit}")
    private int rateLimit;

    @Value("${server.requests.seconds}")
    private int rateSeconds;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        RateLimiterInterceptor.initialize(rateLimit, rateSeconds);
        registry.addInterceptor(serviceInterceptor);
    }
}
