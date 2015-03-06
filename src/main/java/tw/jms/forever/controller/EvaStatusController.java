package tw.jms.forever.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by lucasmf on 3/5/15.
 */
@Controller
@RequestMapping("/eva_status")
public class EvaStatusController {

    @RequestMapping("/index.html")
    public String index(){
        return "eva_status/index";
    }
}
