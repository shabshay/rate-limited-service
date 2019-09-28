package rateLimitedService.controllers;

import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final static Logger logger = Logger.getLogger(UserController.class);

    @RequestMapping("/")
    public ResponseEntity handleUserCall(@RequestParam(value = "clientId", defaultValue = "") String clientId) {

        logger.debug(String.format("Handling request for client id: %s", clientId));
        return ResponseEntity.ok("OK");
    }
}