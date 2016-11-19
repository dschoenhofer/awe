package app.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class VerSignalController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;

	// Download verified signal peptide information.
	@RequestMapping("/downloadSignalInfo")
	public String getSignalInfo() throws IOException {

		String total_prot = sql.queryForObject("SELECT COUNT(*) FROM proteins", String.class);
		int count = 0;
		SqlRowSet rs_genes = sql.queryForRowSet("SELECT prot_id from proteins");
		while (rs_genes.next()) {

			String prot_id = rs_genes.getString("prot_id");
			useUniprot(prot_id);
			useNcbi(prot_id);

			System.out.println("CheckVerPep: " + ++count + " / " + total_prot);
		}
		return "redirect:info";
	}

	public void intoDB(String[] results) {
		
		if(results[1].equals("1")){ //if there is allready an entry with 0 (no ver sigpep) in DB > update this entry, with ver sig pep information
			if (sql.queryForObject("SELECT COUNT(*) FROM sigpep WHERE ver_pep = \"0\" AND prot_id = \"" + results[0] + "\"", String.class).equals("1")){
				sql.execute("UPDATE sigpep SET ver_pep = \"1\", ver_pep_start = \"" + results[2] + "\", ver_pep_stop = \"" + results[3] + "\" WHERE prot_id = \"" + results[0] + "\" ");
			}else{
				sql.execute("INSERT IGNORE INTO sigpep (prot_id, ver_pep, ver_pep_start, ver_pep_stop) VALUES (\"" + results[0] + "\", \"" + results[1] + "\", \"" + results[2]	+ "\", \"" + results[3] + "\")");
			}
		}else{
			sql.execute("INSERT IGNORE INTO sigpep (prot_id, ver_pep, ver_pep_start, ver_pep_stop) VALUES (\"" + results[0] + "\", \"" + results[1] + "\", \"" + results[2]	+ "\", \"" + results[3] + "\")");
		}
		
	}

	public void useNcbi(String prot_id) throws MalformedURLException {

		// first get the ncbi id for given protein id
		Pattern p_id = Pattern.compile("<Id>(\\d+).+");
		URL url1 = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=protein&term=" + prot_id
				+ "&retmode=text");
		ArrayList<String> id_page = new DownloadController().downloadPage(url1);
		String id = "";
		for (String line : id_page) {
			Matcher m_id = p_id.matcher(line);
			if (m_id.matches()) {
				id = m_id.group(1);
			}
		}

		// then parse the file for signal peptide info
		Pattern p_signal = Pattern.compile("region_name\\tSignal");
		Pattern p_ver = Pattern.compile("evidence\\t(\\w+)");
		Pattern p_start_stop = Pattern.compile("^(\\d+)\\t(\\d+)");
		URL url2 = new URL("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&rettype=ft&id=" + id);
		ArrayList<String> info_page = new DownloadController().downloadPage(url2);

		if (info_page.size() <= 3) {
			System.out.println("no entry found for: " + url2);
		}

		for (int i = 0; i < info_page.size(); i++) {

			Matcher m_signal = p_signal.matcher(info_page.get(i));
			if (m_signal.matches()) {
				Matcher m_ver = p_ver.matcher(info_page.get(i + 2));
				if (m_ver.matches() && m_ver.group(1).equals("experimental")) {
					Matcher m_start_stop = p_start_stop.matcher(info_page.get(i - 1));
					intoDB(new String[] { prot_id, "1", m_start_stop.group(1), m_start_stop.group(2) });
					System.out.println("found: " + prot_id);
				}
			} else {
				intoDB(new String[] { prot_id, "0", "NULL", "NULL" });
			}
		}

	}

	public void useUniprot(String prot_id) throws MalformedURLException {

		Pattern p_ver_sigpep = Pattern.compile("Signal peptide\\t(\\d+)\\t(\\d+).+PubMed.+");
		URL down_url = new URL("http://www.uniprot.org/uniprot/?sort=score&format=gff&limit=1&query=" + prot_id);
		ArrayList<String> uniprot_page = new DownloadController().downloadPage(down_url);

		if (uniprot_page.size() <= 1) {
			System.out.println("no entry found for: " + down_url);
		}

		uniprot_page.forEach((line) -> {

			Matcher m_ver_sigpep = p_ver_sigpep.matcher(line);

			if (m_ver_sigpep.matches()) {
				System.out.println("Found: " + prot_id);
				intoDB(new String[] { prot_id, "1", m_ver_sigpep.group(1), m_ver_sigpep.group(2) });
			} else {
				intoDB(new String[] { prot_id, "0", "NULL", "NULL" });
			}
		});

	}

	

}
