package de.flowsuite.mailflowapi.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
class UserResource {

    record CreateUserRequest() {}
}
