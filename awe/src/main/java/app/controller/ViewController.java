package app.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {
	


	@RequestMapping("/")
	public ModelAndView home() {

		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "Dies ist die Home Seite");
		mav.addObject("fragment", "home");
		
		return mav;

	}

	
	@RequestMapping("/test_page")
	public ModelAndView test_mav(){
		
		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "Titel der Seite");
		mav.addObject("contend", "Inhalt als String übergeben.");
		mav.addObject("fragment", "test");
		
		return mav;
	}
	
	

}
