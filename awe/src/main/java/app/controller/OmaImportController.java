package app.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OmaImportController {
	
	DbController db = new DbController();
	
	@RequestMapping("/importOMA")
	public String importOMA(@RequestParam String path) throws IOException{
		
		File oma_folder = new File(path);
		File[] oma_folder_content = oma_folder.listFiles();
		
		for(int i = 0; i < oma_folder_content.length; i++){
			
			Pattern p_prot_org_name = Pattern.compile("^>(.+) \\[(.+)\\].*");
			
			String og_name = oma_folder_content[i].getName().substring(oma_folder_content[i].getName().length() - 3);
			FileReader fr = new FileReader(oma_folder_content[i].getAbsolutePath());
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			while(line != null){
				
				String prot_name = "";
				String org_name = "";
				String prot_seq = "";
				int got_header = 0;
				int got_seq = 0;
				
				Matcher m_prot_org_name = p_prot_org_name.matcher(line);
				if(m_prot_org_name.matches()){
					prot_name = m_prot_org_name.group(1);
					org_name = m_prot_org_name.group(2);
					got_header = 1;
				}
				
				if(got_header == 1 && !line.equals("")){
					prot_seq += line;
					got_seq = 1;
				}
				
				if(line.equals("") && got_header == 1 && got_seq == 1){
					db.intoDB(og_name, org_name, org_name, prot_name, "", prot_seq, prot_name);
					got_header = 0;
					got_seq = 0;
				}
				
				line = br.readLine();
			}
			br.close();
		}
		
		System.out.println("Import complete.");
		return "redirect:info";
	}
	
}
