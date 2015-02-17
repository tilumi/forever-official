package tw.jms.forever.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/woogle")
public class WoogleController {

	@RequestMapping(value="/index.html", method=RequestMethod.GET)
	public String index(Model model){
		model.addAttribute("message", "Hello Woogle!");
		return "woogle/index";
	}		
}
