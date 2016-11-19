package app.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import app.config.PathConfig;

@Controller
public class TreeController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;

	@RequestMapping("/computeTree")
	public String computeTree() throws InterruptedException, IOException {

		runPhyml();

		return "redirect:info";
	}

	public void runPhyml() throws InterruptedException, IOException {

		File[] og_folders = new File(PathConfig.working_dir).listFiles();
		for (int i = 0; i < og_folders.length; i++) {

			ArrayList<String> phyml_args = new ArrayList<String>();
			phyml_args.add(PathConfig.path_phyml);
			phyml_args.add("--input");
			phyml_args.add(og_folders[i].getAbsolutePath() + "/" + og_folders[i].getName() + ".msa");
			phyml_args.add("--datatype");
			phyml_args.add("aa");
			phyml_args.add("--model");
			phyml_args.add("LG");
			phyml_args.add("--quiet");

			ProcessBuilder phyml = new ProcessBuilder(phyml_args);
			phyml.start().waitFor();
			
			//delete file with additonal information after phyml run
			new File(og_folders[i].getAbsolutePath() + "/" + og_folders[i].getName() + ".msa_phyml_stats.txt").delete();
			
			intoDB(og_folders[i].getName());
			
			System.out.println("phyml: " + (i + 1) + " / " + og_folders.length);
		}
	}
	

	public File intoDB(String og_id) throws IOException {

		File phyml_file = new File(PathConfig.working_dir + "/" + og_id + "/" + og_id + ".msa_phyml_tree.txt");
		phyml_file.renameTo(new File(PathConfig.working_dir + "/" + og_id + "/" + og_id + ".newick"));
		phyml_file = new File(PathConfig.working_dir + "/" + og_id + "/" + og_id + ".newick");

		if (phyml_file.length() == 0) {

			// no tree for og's with only 2 proteins
			phyml_file.delete();

		} else {

			FileReader fr = new FileReader(phyml_file);
			BufferedReader br = new BufferedReader(fr);
			
			sql.execute("INSERT IGNORE INTO tree VALUES(\"" + og_id + "\", \"" + br.readLine() + "\")");

			br.close();
		}
		return phyml_file;

	}

}
