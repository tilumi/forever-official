package tw.jms.forever.rest.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.jms.forever.util.EnvKeys;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by lucasmf on 3/4/15.
 */
@RestController
@RequestMapping("/rest/evaStatus")
public class EvaStatusRestController {

    @Autowired
    Environment environment;

    @RequestMapping("/index.html/")
    public String index() throws IOException {
        URL evaStatusUrl = new URL(environment.getProperty(EnvKeys.EVA_STATUS_URL));
        BufferedReader in = new BufferedReader(
                new InputStreamReader(evaStatusUrl.openStream()));
        String evaStatusJsonString = IOUtils.toString(in);
        return evaStatusJsonString;
    }
}
