package app.controller;


import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {
	
	// DB Connection
	@Autowired
	JdbcTemplate sql;

	@RequestMapping("/")
	public ModelAndView home() {

		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "Gain and loss of Signal peptides - Tool");
		mav.addObject("contend", "Hier könnte eine Beschreibung stehen.");
		mav.addObject("fragment", "info");
		
		return mav;

	}
	
	@RequestMapping("/help")
	public ModelAndView help(){
		
		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "Dataset edit options");
		mav.addObject("contend", "This is helpful.");
		mav.addObject("fragment", "info");
		
		return mav;
	}
	
	@RequestMapping("/info")
	public ModelAndView info(){
		
		ModelAndView mav = new ModelAndView("template");
		mav.addObject("title", "Operation completed successfully.");
		mav.addObject("contend", "Operation completed successfully!");
		mav.addObject("fragment", "info");
		
		return mav;
	}
	
	@RequestMapping("/edit-options")
	public ModelAndView editOptions(){
		
		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "Dataset edit options");
		mav.addObject("fragment", "edit");
		
		return mav;
	}
	
	@RequestMapping("/tree")
	public ModelAndView tree(){
		
		ArrayList<String> og_list = new ArrayList<String>();
		SqlRowSet og_rs = sql.queryForRowSet("SELECT og_id FROM tree");
		while(og_rs.next()){
			og_list.add(og_rs.getString("og_id"));
		}
		
		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "View phylogenetic Tree");
		mav.addObject("fragment", "tree");
		mav.addObject("og_list", og_list);
		
		return mav;
	}
	
	@RequestMapping("/show_og")
	public ModelAndView show_og(){
		
		ArrayList<String> og_list = new ArrayList<String>();
		SqlRowSet og_rs = sql.queryForRowSet("SELECT og_id FROM ortho_groups");
		while(og_rs.next()){
			og_list.add(og_rs.getString("og_id"));
		}
		
		ModelAndView mav = new ModelAndView("/template");
		mav.addObject("title", "View overview of orthologous group.");
		mav.addObject("fragment", "show");
		mav.addObject("og_list", og_list);
		
		
		return mav;		
	}
	

}
