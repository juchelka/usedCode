package org.netbeans.modules.php.usedCode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileMetricsVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.FileVO;
import org.netbeans.modules.php.project.ui.codecoverage.CoverageVO.LineVO;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileObject;
import org.openide.windows.OutputWriter;

public class UsedCode {
	
	public FileObject root;
	public OutputWriter output = null;
	public CodeUsage usage = null;
	public PhpProjectProperties properties = null;
	
	public UsedCode(FileObject root)
	{
		this.root = root;
	}
	
	public void loadUsageFile(File usageFile) throws IOException
	{
		String json = "";
		FileReader input = new FileReader(usageFile);
		BufferedReader bufRead = new BufferedReader(input);
		String line;
		int count = 0;
		line = bufRead.readLine();
		while (line != null){
			json += line;
			line = bufRead.readLine();
		}
		bufRead.close();
		
		this.usage = this.parseJson(json);
	}
	
	private CodeUsage parseJson(String s)
	{
		JSONArray array = (JSONArray) JSONValue.parse(s);

		usage = new CodeUsage();
		
		for (Object ob : array)
		{
			JSONObject job = (JSONObject) ob;
			
			FileUsage fu = new FileUsage();
			fu.file = job.get("file").toString();
			
			JSONArray lines = (JSONArray) job.get("lines");
			fu.lines = new int[lines.size()];
			int i = 0;
			for (Object line : lines)
			{
				fu.lines[i] = Integer.parseInt(line.toString());
				i++;
			}
			usage.add(fu);
		}
		
		return usage;
	}
	
	public CoverageVO coverage()
	{
		HashMap<String,String> mapping = this.getPathMapping();
		CoverageVO cv = new CoverageVO();		
		for (FileUsage f : usage.values())
		{
			String usedPath = f.file;
			if (mapping != null)
			{
				for(Object remotePath : mapping.keySet())
				{
					if (f.file.startsWith(remotePath.toString()))
					{
						String localPath = mapping.get((String) remotePath);
						usedPath = f.file.replaceFirst(Pattern.quote(remotePath.toString()), localPath.replaceAll(Pattern.quote("\\"), "\\\\\\\\"));
						if (System.getProperty("file.separator").equals("\\"))
						{
							usedPath = usedPath.replace('/', '\\');
						}
						log("Mapping used: " + usedPath);
					}
				}
			}
			
			FileVO fv = new FileVO(usedPath);
			int max = 0;
			for (int line : f.lines)
			{
				if (line > max) 
				{
					max = line;
				}
				fv.addLine(new LineVO(line, "stmt", 1));
				log(f.file + ": " + line);
			}
			fv.setMetrics(new FileMetricsVO(max, 0, 0, 0, 0, 0, 0, 0, 0));
			cv.addFile(fv);
		}

		return cv;
	}
	
	public HashMap<String,String> getPathMapping() {
		ConfigManager.Configuration conf = properties.getConfigManager().currentConfiguration();
		
		if (conf.getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE) == null)
		{
			return null;
		}
		
		String separator = Pattern.quote(PhpProjectProperties.DEBUG_PATH_MAPPING_SEPARATOR);

		String localMapping = conf.getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_LOCAL);
		String[] lm;
		if (localMapping == null)
		{
			lm = new String[0];
		} else {
			lm = localMapping.split(separator);
		}

		HashMap<String,String> mapping = new HashMap<String,String>();
		int i = 0;
		for (String rem : conf.getValue(PhpProjectProperties.DEBUG_PATH_MAPPING_REMOTE).split(separator))
		{
			String path;
			String fileSeparator = System.getProperty("file.separator");
			if (i+1 > lm.length)
			{
				path = this.root.getPath();
			} else {
				path = this.root.getPath() + fileSeparator + lm[i];
			}

			if (fileSeparator.equals("\\"))
			{
				path = path.replace('/', '\\');
			}
			mapping.put(rem, path);

			i++;
		}

		for (Object o : mapping.keySet())
		{
			log("MAP " + o.toString() + " -> " + mapping.get(o));
		}

		return mapping;
	}
	
	public void log(String s)
	{
		if (this.output != null)
		{
			this.output.println(s);
		}
	}
	
}