package tw.jms.forever.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/finder")
@Controller
public class FinderController {

	@RequestMapping("/index.html")
	public String index(Model model){		
		return "finder/index";
	}
}
