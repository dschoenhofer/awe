package app.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import app.config.PathConfig;

@Controller
public class MsaController {

	// multiple sequence alignments, using clustal omega
	@RequestMapping("/alignMsa")
	public String alignMsa() throws InterruptedException, IOException{
		
		File[] og_folders = new File(PathConfig.working_dir).listFiles();
		for(int i = 0; i < og_folders.length; i++){
									
			ArrayList<String> clustalo_args = new ArrayList<String>();
			clustalo_args.add(PathConfig.path_clustalo);
			clustalo_args.add("-i");
			clustalo_args.add(og_folders[i].getAbsolutePath() + "/all_" + og_folders[i].getName() + ".fasta");
			clustalo_args.add("-o");
			clustalo_args.add(og_folders[i].getAbsolutePath() + "/" + og_folders[i].getName() + ".msa");
			clustalo_args.add("--force");
			clustalo_args.add("--outfmt");
			clustalo_args.add("phy");
			
			ProcessBuilder clustalo = new ProcessBuilder(clustalo_args);
			clustalo.start().waitFor();
			System.out.println("clustalo: " + (i+1) + " / " + og_folders.length);
		}
		
		return "redirect:info";
	}
	
	
}
