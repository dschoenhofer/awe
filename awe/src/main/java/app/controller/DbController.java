package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DbController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;

	// create tables an indexes
	public void createTables() {
		sql.execute(
				"CREATE TABLE IF NOT EXISTS ortho_groups(og_id VARCHAR(10), prot_id VARCHAR(8), avg_seq_identity VARCHAR(6), UNIQUE KEY og_id(og_id, prot_id))");
		sql.execute("CREATE INDEX og ON ortho_groups(og_id)");
		sql.execute("CREATE INDEX og_prot ON ortho_groups(og_id, prot_id)");
		sql.execute(
				"CREATE TABLE IF NOT EXISTS proteins(prot_id VARCHAR(8) UNIQUE, uniprot_de VARCHAR(100), org_name VARCHAR(100), org_taxid INT, prot_seq VARCHAR(1000), prot_seq_min100 VARCHAR(100),gene_id VARCHAR(10), gene_seq VARCHAR(5000), core_pan VARCHAR(4))");
		sql.execute("CREATE INDEX prot ON proteins(prot_id)");
		sql.execute(
				"CREATE TABLE IF NOT EXISTS alignments(prot1_id VARCHAR(8), prot2_id VARCHAR(8), seq_identity DECIMAL(3,2), UNIQUE KEY prot1_id(prot1_id, prot2_id))");
		sql.execute("CREATE INDEX prot1 ON alignments(prot1_id)");
		sql.execute("CREATE INDEX prot1_prot2 ON alignments(prot1_id, prot2_id)");
		sql.execute(
				"CREATE TABLE IF NOT EXISTS sigpep(prot_id VARCHAR(8), ver_pep BOOL, ver_pep_start INT, ver_pep_stop INT, pre_pep BOOL, pre_pep_start INT, pre_pep_stop INT, UNIQUE KEY prot_id(prot_id))");
		sql.execute("CREATE INDEX prot ON sigpep(prot_id)");
		sql.execute("CREATE INDEX prot_verpep ON sigpep(prot_id, ver_pep)");
		sql.execute("CREATE INDEX prot_prepep ON sigpep(prot_id, pre_pep)");
		sql.execute("CREATE TABLE IF NOT EXISTS tree(og_id VARCHAR(10), tree VARCHAR(2000), UNIQUE KEY og_id(og_id))");
		sql.execute(
				"CREATE TABLE IF NOT EXISTS genomes(org_taxid INT, genome(VARCHAR(100000)), UNIQUE KEY org_taxid(org_taxid))");
	}
	
	@RequestMapping("/dropTables")
	public String dropTables(@RequestParam(required = false) String ortho_groups, @RequestParam(required = false) String proteins, @RequestParam(required = false) String alignments, @RequestParam(required = false) String sigpep, @RequestParam(required = false) String tree, @RequestParam(required = false) String genomes){
		
		if(ortho_groups != null){ 
			sql.execute("DROP TABLE IF EXISTS ortho_groups");
		}
		if(proteins != null){ 
			sql.execute("DROP TABLE IF EXISTS proteins");
		}
		if(alignments != null){
			sql.execute("DROP TABLE IF EXISTS alignments");
		}
		if(sigpep != null){
			sql.execute("DROP TABLE IF EXISTS sigpep");
		}
		if(tree != null){
			sql.execute("DROP TABLE IF EXISTS tree");
		}
		if(genomes != null){
			sql.execute("DROP TABLE IF EXISTS genomes");
		}
		
		return "redirect:info";
	}
	
	public void intoDB(String og_id, String org_taxid, String org_name, String prot_id, String uniprot_de, String prot_seq, String gene_id) {
		sql.execute("INSERT IGNORE INTO ortho_groups VALUES(\"" + og_id + "\",\"" + prot_id + "\"," + "NULL)");
		sql.execute("INSERT IGNORE INTO proteins VALUES(\"" + prot_id + "\",\"" + uniprot_de + "\",\"" + org_name + "\"," + org_taxid + ",\"" + prot_seq + "\",NULL, \"" + gene_id + "\", NULL, NULL)");
	}
	
	public void intoDB(String prot1_id, String prot2_id, double identity) {
		sql.execute("INSERT IGNORE INTO alignments VALUES(\"" + prot1_id + "\",\"" + prot2_id + "\"," + identity + ")");
	}
	
	public void intoDB(String og, String avg_identity){
		sql.execute("UPDATE ortho_groups SET avg_seq_identity = \"" + avg_identity + "\" WHERE og_id like \"" + og + "\"");
	}


}
