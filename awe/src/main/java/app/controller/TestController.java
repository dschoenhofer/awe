package app.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;


	
	@RequestMapping("/test_code")
	public String testCode(){
		
		sql.execute("CREATE TABLE IF NOT EXISTS test_table(test_eintrag VARCHAR(10))");
		
		ArrayList<String> test_list = new ArrayList<String>();
		SqlRowSet test_rs = sql.queryForRowSet("SELECT test_eintrag FROM test_table");
		while(test_rs.next()){
			test_list.add(test_rs.getString("test_eintrag"));
		}
		
		testMethode();
		
		return "redirect:test_page";
	}
	
	public void testMethode(){
	
		System.out.println("Ich war hier!");
	
	}
	



}
