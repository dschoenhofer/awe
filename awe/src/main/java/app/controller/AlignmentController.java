package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import app.config.PathConfig;

import org.springframework.jdbc.support.rowset.SqlRowSet;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
public class AlignmentController {

	// DB Connection
	@Autowired
	JdbcTemplate sql;
	DbController db = new DbController();
	
	// alignment all to all of members of orthologous groups
	@RequestMapping("/alignGroups1On1")
	public String alignGroups1On1() throws IOException, InterruptedException {

		align();
		readFiles();
		
		System.out.println("1on1 alignment finished.");
		return "redirect:info";
	}

	public void align() throws IOException, InterruptedException {

		// create dirs in /tmp
		File folder = new File(PathConfig.working_dir);
		if (folder.mkdir()) {
			System.out.println("Folder " + PathConfig.working_dir + " created.");
		} else {
			System.out.println("Folder " + PathConfig.working_dir + "could not be created!");
		}
		// get all ortho_groups
		int count_og = 0;
		SqlRowSet rs_ogs = sql.queryForRowSet("SELECT DISTINCT og_id from ortho_groups LIMIT 10");

		while (rs_ogs.next()) {
			String og_id = rs_ogs.getString("og_id");
			// create folder for each group
			new File(PathConfig.working_dir + "/" + og_id).mkdir();
			// get all proteins for actual OG and create fasta file in folder +
			// fasta file with all sequences
			File all_fasta = new File(PathConfig.working_dir + "/" + og_id + "/all_" + og_id + ".fasta");
			String current_folder = PathConfig.working_dir + "/" + og_id;
			FileWriter a_fw = new FileWriter(all_fasta); // file with all
															// sequences
			BufferedWriter a_bw = new BufferedWriter(a_fw);
			SqlRowSet rs_proteins = sql
					.queryForRowSet("SELECT p.prot_id, p.prot_seq FROM proteins p, ortho_groups o WHERE o.og_id LIKE \""
							+ og_id + "\" AND o.prot_id = p.prot_id");
			while (rs_proteins.next()) { // write fasta files
				String prot_id = rs_proteins.getString("prot_id");
				String prot_seq = rs_proteins.getString("prot_seq");
				String file_name = current_folder + "/" + prot_id + ".fasta";
				FileWriter fw = new FileWriter(file_name);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(">" + prot_id);
				bw.newLine();
				bw.write(prot_seq);
				bw.close();
				a_bw.append(">" + prot_id);
				a_bw.newLine();
				a_bw.append(prot_seq);
				a_bw.newLine();
			}
			a_bw.close();
			// create sequence alignments with needle
			File[] og_folder_contend = new File(current_folder).listFiles();
			for (int i = 0; i < og_folder_contend.length; i++) {
				if (!og_folder_contend[i].getName().equals(all_fasta.getName())) {
					ArrayList<String> needle_args = new ArrayList<String>();
					needle_args.add(PathConfig.path_needle);
					needle_args.add("-gapopen");
					needle_args.add("10.0");
					needle_args.add("-gapextend");
					needle_args.add("0.5");
					needle_args.add("-outfile");
					needle_args.add(og_folder_contend[i].getAbsolutePath().replaceFirst(".fasta", ".needle"));
					needle_args.add(og_folder_contend[i].getAbsolutePath());
					needle_args.add(all_fasta.getAbsolutePath());
					ProcessBuilder needle = new ProcessBuilder(needle_args);
					needle.start().waitFor();
					// System.out.println(current_folder);
				}
			}
			count_og++;
			System.out.println("needle og: " + count_og + " / " + og_folder_contend.length);
		}
	}

	public void readFiles() throws IOException {
		// read the created .needle files into database

		Pattern p_prot1_id = Pattern.compile("^# 1: (.+)");
		Pattern p_prot2_id = Pattern.compile("^# 2: (.+)");
		Pattern p_identity = Pattern.compile("^# Identity:.+\\((.+)%\\)");

		File[] folders = new File(PathConfig.working_dir).listFiles();
		for (int i = 0; i < folders.length; i++) {

			ArrayList<String> prot1_id = new ArrayList<String>();
			ArrayList<String> prot2_id = new ArrayList<String>();
			ArrayList<Double> identity = new ArrayList<Double>();
			double sum_identity = 0;

			File[] files = folders[i].listFiles();
			for (int j = 0; j < files.length; j++) {
				if (files[j].getName().contains(".needle")) {
					FileReader fr = new FileReader(files[j].getAbsolutePath());
					BufferedReader br = new BufferedReader(fr);
					String line = br.readLine();
					while (line != null) {

						Matcher m_prot1_id = p_prot1_id.matcher(line);
						if (m_prot1_id.matches()) {
							prot1_id.add(m_prot1_id.group(1));
						}
						Matcher m_prot2_id = p_prot2_id.matcher(line);
						if (m_prot2_id.matches()) {
							prot2_id.add(m_prot2_id.group(1));
						}
						Matcher m_identity = p_identity.matcher(line);
						if (m_identity.matches()) {
							identity.add(Double.parseDouble(m_identity.group(1)));
							sum_identity += Double.parseDouble(m_identity.group(1));
						}

						line = br.readLine();
					}
					br.close();
				}
			}
			//insert into alginment DB
			for (int w = 0; w < prot1_id.size(); w++) {
				db.intoDB(prot1_id.get(w), prot2_id.get(w), identity.get(w));
			}
			//insert arithmetic mean to ortho_groups table
			db.intoDB(folders[i].getName() ,new DecimalFormat("#0.00").format(sum_identity / identity.size()));
		}
	}

}
