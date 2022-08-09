import io.intino.magritte.builder.utils.FileSystemUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static io.intino.master.builder.MastercRunner.main;

@Ignore
public class TaracRunnerTest {
	private String home;

	@Before
	public void setUp() {
		home = new File("test-res").getAbsolutePath() + File.separator;
	}

	private static String temp(String filepath) {
		try {
			File file = new File(filepath);
			String home = System.getProperty("user.home");
			String text = Files.readString(file.toPath()).replace("$WORKSPACE", home + File.separator + "workspace").replace("$HOME", home);
			Path temporalFile = Files.createTempFile(file.getName(), ".txt");
			Files.writeString(temporalFile, text, StandardOpenOption.TRUNCATE_EXISTING);
			temporalFile.toFile().deleteOnExit();
			return temporalFile.toFile().getAbsolutePath();
		} catch (IOException e) {
			return null;
		}
	}


	@Test
	public void cinepolisM1() {
		FileSystemUtils.removeDir("/Users/oroncal/workspace/master/test/gen/io/intino/test/model");
		main(new String[]{temp(home + "cinepolis.txt")});
	}
}
