package dev.newline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

/*
 * 
:Here is the line executed:
a  \n fprintf("hello\n", %s);


https://forum.plantuml.net/11343/activity-diagram-multi-line-and-n-management
@startuml
:1 %n() fprintf( hello%n() , %s);
@enduml

 */
public class activity_mono_multi_line2 {

	protected File getJavaFile() {
		final String name = getClass().getName();
		final File f = new File("test/" + name.replace('.', '/') + ".java");
		return f;
	}

	@Test
	public void testExecute() throws IOException, InterruptedException {
		final File file = getJavaFile();
		final FileFormatOption options = new FileFormatOption(FileFormat.PNG);

		final File outputDirectory = new File("outputdev").getAbsoluteFile();
		outputDirectory.mkdirs();

		final SourceFileReader reader = new SourceFileReader(Defines.createWithFileName(file), file, outputDirectory,
				Collections.<String>emptyList(), "UTF-8", options);
		final List<GeneratedImage> list = reader.getGeneratedImages();

		assertEquals(1, list.size());

	}


}
