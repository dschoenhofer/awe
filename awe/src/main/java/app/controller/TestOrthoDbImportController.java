package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.io.*;

@Controller
public class TestOrthoDbImportController {
	
	DbController db = new DbController();

	// orthoDB fasta file
	@RequestMapping("/importOrthoDB")
	public String importOrthoDB(@RequestParam String path) throws IOException {

		// path to orthodb fasta download
		String orthodb_fasta = path;
		
		//create DB tables
		//db.createTables();
		
		// read file from disc and extract info
		System.out.println("Starting import..");
		FileReader fr = new FileReader(orthodb_fasta);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		System.out.println("file found..");

		while (line != null) {

			String[] split = line.split("\\t", -1);
			String og_id = split[0];
			String org_taxid = split[2];
			String org_name = split[3];
			String prot_id = split[5];
			String gene_id = split[6];
			String uniprot_de = split[7];
			String prot_seq = split[8];
			

			if (!split[0].equals("pub_og_id")) {
				db.intoDB(og_id, org_taxid, org_name, prot_id, uniprot_de, prot_seq, gene_id);
			}

			line = br.readLine();
		}
		br.close();

		System.out.println("Import complete.");
		return "redirect:info";
	}
	


}
