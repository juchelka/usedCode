// https://bitbucket.org/netbeans/main-silver 
// https://bitbucket.org/netbeans/main-silver/src/a8bb6947b7ee/php.project/src/org/netbeans/modules/php/project
// https://bitbucket.org/netbeans/main-silver/src/479f53195a06/maven.coverage/src/org/netbeans/modules/maven/coverage/MavenCoverageProvider.java

package org.netbeans.modules.php.usedCode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ui.codecoverage.PhpCoverageProvider;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

@ActionID(
    category = "File",
id = "org.netbeans.modules.php.usedCode.LoadUsage")
@ActionRegistration(
    displayName = "#CTL_LoadUsage")
@ActionReference(path = "Menu/Tools", position = 50)
@Messages("CTL_LoadUsage=Load usage")
public final class LoadUsage implements ActionListener {

	private final Project context;
	private UsedCode usedCode = null;
	private InputOutput io = null;

	public LoadUsage(Project context) {
		this.context = context;
		this.io = IOProvider.getDefault().getIO("Used code", true);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {

		usedCode = new UsedCode(this.getRoot());
		usedCode.output = this.io.getOut();
		usedCode.properties = this.getProjectProperties();

		try {
			usedCode.loadUsageFile(this.selectFile());
			this.getCoverageProvider().setEnabled(true);
			this.getCoverageProvider().setCoverage(usedCode.coverage());
		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
	}
	
	public FileObject getRoot()
	{
		Collection<? extends Sources> sources = this.context.getLookup().lookupAll(Sources.class);
		Iterator<? extends Sources> i = sources.iterator();
		while (i.hasNext())
		{
			Sources source = (Sources) i.next();
			SourceGroup[] groups = source.getSourceGroups("generic");
			for (SourceGroup g : groups)
			{
				return g.getRootFolder();
			}
		}
		return null;
	}

	public File selectFile()
	{
		JFileChooser jfc = new JFileChooser();
		File f = new File(System.getProperty("user.dir"));
		jfc.setCurrentDirectory(f);
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.showOpenDialog(null);
		return jfc.getSelectedFile();
	}
	
	public PhpCoverageProvider getCoverageProvider()
	{
		return this.context.getLookup().lookup(PhpCoverageProvider.class);
	}
	
	public PhpProjectProperties getProjectProperties()
	{
		return new PhpProjectProperties((PhpProject) this.context);
	}

}