package rateLimitedService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RateLimitedServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

	@Value("${server.requests.limit}")
	private int rateLimit;

	@Value("${server.requests.seconds}")
	private int rateSeconds;

    @Test
    public void invalidClientIdTest() throws Exception {
        this.mvc.perform(get("/")).andExpect(status().isBadRequest());
    }

    @Test
    public void validOneTimeRequestTest() throws Exception {
        this.mvc.perform(get("/?clientId=5")).andExpect(status().isOk())
				.andExpect(content().string("OK"));
    }

	@Test
	public void allowedRequestsPerTimeFrameTest() throws Exception {
    	final int callsAllowed = rateLimit;
    	final int timeFrameInSeconds = rateSeconds;

		// wait for time frame to be expired (if exists from previous calls)
		Thread.sleep(timeFrameInSeconds * 1000);

    	// create allowed number of calls within the time frame
    	for (int i = 0; i < callsAllowed; i++){
			this.mvc.perform(get("/?clientId=5"))
					.andExpect(status().isOk()).andExpect(content().string("OK"));
		}

    	// verify limited request
		this.mvc.perform(get("/?clientId=5"))
				.andExpect(status().isServiceUnavailable());

    	// wait for time frame to be expired
    	Thread.sleep(timeFrameInSeconds * 1000);

    	// create again allowed calls with new time frame
		for (int i = 0; i < callsAllowed; i++){
			this.mvc.perform(get("/?clientId=5"))
					.andExpect(status().isOk()).andExpect(content().string("OK"));
		}

		// verify limited request
		this.mvc.perform(get("/?clientId=5"))
				.andExpect(status().isServiceUnavailable());

		// wait for time frame to be expired (for next calls)
		Thread.sleep(timeFrameInSeconds * 1000);
	}

}
