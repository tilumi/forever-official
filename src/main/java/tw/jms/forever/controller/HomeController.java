package tw.jms.forever.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import tw.jms.forever.security.GoogleAuthenticationProvider;

@Controller
public class HomeController {

	static Logger LOG = LogManager.getLogger(HomeController.class
			.getName());
	
	@Autowired
	GoogleAuthenticationProvider authenticationProvider;
	@Autowired
	private Environment env;

	@RequestMapping("/index.html")
	public String index(Model model) {
		model.addAttribute("message", "Hello World!");
		return "index";
	}
	
}
