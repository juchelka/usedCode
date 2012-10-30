package org.netbeans.modules.php.usedCode;

import java.util.HashMap;

public class CodeUsage extends HashMap<String,FileUsage> {
	
	public void add(FileUsage fileUsage)
	{
		super.put(fileUsage.file, fileUsage);
	}
}
