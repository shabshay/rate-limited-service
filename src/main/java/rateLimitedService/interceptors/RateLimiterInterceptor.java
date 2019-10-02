package rateLimitedService.interceptors;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import rateLimiter.IRateLimiter;
import rateLimiter.InMemory.InMemoryRateLimiterBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * Interceptor that will validate the rate limitation by using the in memory rate limiter
 */
@Component
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final static Logger logger = Logger.getLogger(RateLimiterInterceptor.class);
    private static IRateLimiter<String> limiter;

    public static void initialize(int requestsLimit, int seconds) {
        limiter = InMemoryRateLimiterBuilder
                .newBuilder()
                .setRateLimit(requestsLimit)
                .setIntervalTime(seconds)
                .setTimeUnit(TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String clientId = request.getParameter("clientId");

        logger.debug(String.format("Checking rate limit for user: %s", clientId));

        if (clientId == null || clientId.isEmpty()) {
            logger.warn(String.format("Invalid client id, returning bad request", clientId));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return false;
        }

        var result = limiter.acquire(clientId);
        logger.debug("[Client - " + clientId + "] Acquire result: " + result.isSucceeded() +
                ", expiration time: " + result.getExpirationTime().getTime().toString() +
                ", remaining hits: " + result.getRemainHits());

        if (!result.isSucceeded()) {
            logger.warn(String.format("User %s has exceeded the rate limit, returning service unavailable", clientId));
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return false;
        }

        logger.debug(String.format("User %s has not exceeded the rate limit", clientId));
        return true;
    }
}
