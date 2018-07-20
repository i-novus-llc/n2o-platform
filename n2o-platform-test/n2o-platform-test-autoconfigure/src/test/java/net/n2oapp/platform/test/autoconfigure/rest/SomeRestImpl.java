package net.n2oapp.platform.test.autoconfigure.rest;

import net.n2oapp.platform.test.autoconfigure.rest.api.SomeRest;
import org.springframework.stereotype.Controller;

@Controller
public class SomeRestImpl implements SomeRest {
    @Override
    public String echo() {
        return "echo";
    }
}
