package dev.newline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.GeneratedImage;
import net.sourceforge.plantuml.SourceFileReader;
import net.sourceforge.plantuml.preproc.Defines;

/*
 * 

https://forum.plantuml.net/18126/multi-line-creole-tables-broken-activity-diagrams-since-2023
@startuml
:|Creole Table Line1|\n|Line2|;
@enduml

@startuml
' Adapted from https://forum.plantuml.net/18126/multi-line-creole-tables-broken-activity-diagrams-since-2023
:| Creole Table \\n multi-line1| a |\n| Line2| b |;
:
| Creole Table \n multi-line1| a |
| Line2| b |;
:
| Creole Table %newline() multi-line1| a |
| Line2| b |;
@enduml

 */
public class activity_creole_table {

	protected File getJavaFile() {
		final String name = getClass().getName();
		final File f = new File("src/test/java/" + name.replace('.', '/') + ".java");
		return f;
	}

	@Test
	public void testExecute() throws IOException, InterruptedException {
		final File file = getJavaFile();
		final FileFormatOption options = new FileFormatOption(FileFormat.PNG);

		final File outputDirectory = new File("outputdev").getAbsoluteFile();
		outputDirectory.mkdirs();

		final SourceFileReader reader = new SourceFileReader(Defines.createWithFileName(file), file, outputDirectory,
				Arrays.asList("!pragma layout smetana"), "UTF-8", options);
		final List<GeneratedImage> list = reader.getGeneratedImages();

		assertEquals(2, list.size());

	}


}
