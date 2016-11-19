package app.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import app.config.PathConfig;

@Controller
public class BayesTraitsController {
	
	// DB Connection
	@Autowired
	JdbcTemplate sql;

	@RequestMapping("/bayes_traits")
	public String bayes_traits() throws IOException{
		
		File[] og_folders = new File(PathConfig.working_dir).listFiles();
		for(int i = 0; i < og_folders.length; i++){
			
			if(convertToNexus(new File(og_folders[i].getAbsolutePath() + "/" + og_folders[i].getName() + ".newick"), og_folders[i].getName()).exists()){
				createBtFile(og_folders[i]);
			}
			
			
		}
		
		return "redirect:info";
	}
	
	public void createBtFile(File folder) throws IOException{
		
		File bt_file = new File(folder.getAbsolutePath() + "/" + folder.getName() + ".bt");
		ArrayList<String> prot_ids = new ArrayList<String>();

		SqlRowSet rs_prot_ids = sql.queryForRowSet("SELECT prot_id FROM ortho_groups WHERE og_id = \"" + folder.getName() + "\"");
		while(rs_prot_ids.next()){
			prot_ids.add(rs_prot_ids.getString("prot_id"));
		}
		
		FileWriter fw = new FileWriter(bt_file);
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(String prot_id : prot_ids){
			bw.write(prot_id);
			bw.newLine();
		}
		bw.close();
		
		
	}
	
	public File convertToNexus(File newick, String og_id) throws IOException{
		
		File nexus = new File(PathConfig.working_dir + "/" + og_id + "/" + og_id + ".nexus");
		
		if(newick.exists()){
						
			FileReader fr = new FileReader(newick);
			BufferedReader br = new BufferedReader(fr);
			String newick_tree = br.readLine();
			br.close();
			
			FileWriter fw = new FileWriter(nexus);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("#NEXUS");
			bw.newLine();
			bw.write("BEGIN TREES;");
			bw.newLine();
			bw.write("TREE BAUM = " + newick_tree);
			bw.newLine();
			bw.write("ENDBLOCK;");
			bw.close();
		}
		return nexus;
	}
	
}
