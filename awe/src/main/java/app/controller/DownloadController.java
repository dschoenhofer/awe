package app.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

public class DownloadController {
	
	public DownloadController(){
		
	}
	
	public ArrayList<String> downloadPage(URL down_url) {

		String line;
		ArrayList<String> all_lines = new ArrayList<String>();

		try {

			InputStreamReader is = new InputStreamReader(down_url.openStream());
			BufferedReader br = new BufferedReader(is);

			while ((line = br.readLine()) != null) {
				all_lines.add(line);
			}

			br.close();

		} catch (IOException e) {
			System.out.println("error connecting to webside: " + down_url + "\nRetry..");
			e.printStackTrace();
			downloadPage(down_url);
		}

		return all_lines;

	}
	
}
