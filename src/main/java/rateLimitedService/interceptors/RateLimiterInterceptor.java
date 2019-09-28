package rateLimitedService.interceptors;

import es.moki.ratelimitj.core.limiter.concurrent.ConcurrentLimitRule;
import es.moki.ratelimitj.inmemory.concurrent.InMemoryConcurrentRequestRateLimiter;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final static Logger logger = Logger.getLogger(RateLimiterInterceptor.class);
    private static InMemoryConcurrentRequestRateLimiter limiter;

    public static void initialize(int requestsLimit, int seconds){
        limiter = new InMemoryConcurrentRequestRateLimiter(
                ConcurrentLimitRule.of(requestsLimit, TimeUnit.SECONDS, seconds));
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String clientId = request.getParameter("clientId");

        logger.debug(String.format("Checking rate limit for user: %s", clientId));

        if(clientId == null || clientId.isEmpty()){
            logger.warn(String.format("Invalid client id, returning bad request", clientId));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        if (!limiter.acquire(clientId).hasAcquired()) {
            logger.warn(String.format("User %s has exceeded the rate limit, returning service unavailable", clientId));
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }

        logger.debug(String.format("User %s has not exceeded the rate limit", clientId));
        return true;
    }
}
