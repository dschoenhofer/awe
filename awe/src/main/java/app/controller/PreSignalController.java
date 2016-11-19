package app.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import app.config.PathConfig;


@Controller
public class PreSignalController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;

	@RequestMapping("/predictSigPep")
	public String predictSigPep() throws FileNotFoundException, IOException, InterruptedException {

		//downloadGeneSeq();
		downloadGenome();

		return "redirect:info";
	}

	public void downloadGeneSeq() throws MalformedURLException {

		SqlRowSet rs_gene = sql.queryForRowSet("SELECT gene_id FROM proteins");
		int count = 0;
		int all_count = sql.queryForObject("SELECT COUNT(*) FROM proteins", Integer.class);
		while (rs_gene.next()) {

			String gene_id = rs_gene.getString("gene_id");

			URL ebi_url = new URL("http://www.ebi.ac.uk/ena/data/view/" + gene_id + "&display=fasta");
			ArrayList<String> gene_fasta = new DownloadController().downloadPage(ebi_url);

			String one_line_fasta = "";
			for (String line_fasta : gene_fasta) {

				if (line_fasta.charAt(0) != '>') {
					one_line_fasta += line_fasta;
				}

			}

			if (one_line_fasta.contains("Entry: " + gene_id + " display type is either not supported or entry is not found.")) {
				System.out.println("No gene sequence found for Protein: " + gene_id);
			} else {
				sql.execute("UPDATE proteins SET gene_seq = \"" + one_line_fasta + "\" WHERE prot_id = \"" + gene_id + "\"");
			}
			count ++;
			System.out.println("Download gene: " + count + " / " + all_count);
		}

	}

	public void downloadGenome() throws FileNotFoundException, IOException, InterruptedException {

		SqlRowSet rs_org_taxid = sql.queryForRowSet("SELECT DISTINCT org_taxid FROM proteins");
		while(rs_org_taxid.next()){
			
			String org_taxid = rs_org_taxid.getString("org_taxid");
			URL url_ncbi_id = new URL ("http://www.ncbi.nlm.nih.gov/genome/?term=txid" + org_taxid);
			ArrayList<String> id_page = new DownloadController().downloadPage(url_ncbi_id);
			Pattern p_id = Pattern.compile("Download sequences in FASTA format for <a href=\"(ftp.+gz)\">genome");
			File genome_url = new File("");
			
			for(String line : id_page){
				Matcher m_id = p_id.matcher(line);
				if(m_id.matches()){
					genome_url = new File(m_id.group(1));
				}
			}
			
			File folder_genomes = new File(PathConfig.working_dir + "/genomes");
			folder_genomes.mkdir();
			
			System.out.println(org_taxid);
			
			ArrayList<String> wget_args = new ArrayList<String>();
			wget_args.add("wget");
			wget_args.add("-P");
			wget_args.add(folder_genomes.getAbsolutePath());
			wget_args.add(genome_url.getAbsolutePath());
			
			ProcessBuilder wget = new ProcessBuilder(wget_args);
			wget.start().waitFor();
			
			genome_url = new File(folder_genomes.getAbsolutePath() + "/" + genome_url.getName());
			
			GZIPInputStream gis = new GZIPInputStream(new FileInputStream(genome_url));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(folder_genomes.getAbsolutePath() + "/" + org_taxid + ".fasta"));			
			
			int bte;
			while((bte = gis.read()) != -1){
				bos.write(bte);
			}
			bos.close();
			gis.close();
		}
		
		
	}

}
