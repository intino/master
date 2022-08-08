package io.intino.master.framework.filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

public class TriplesFileWriter {

	private final File stage;
	private final String sender;
	private BufferedWriter writer;

	public TriplesFileWriter(File stage, String sender) {
		this.stage = stage;
		this.sender = sender;
	}

	public void write(Triple triple) {
		try {
			writer().write(triple.toString());
			writer().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private BufferedWriter writer() throws IOException {
		if (writer == null) writer = new BufferedWriter(new FileWriter(file(), true));
		return writer;
	}

	private File file() {
		File file = new File(stage, now() + "/" + sender + ".triples");
		file.getParentFile().mkdirs();
		return file;
	}

	private String now() {
		return LocalDate.now().toString().replace("-", "");
	}


}
