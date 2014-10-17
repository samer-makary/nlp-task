package engine.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileLinesReader {

	public static List<String> readLines(File file)
			throws FileNotFoundException {
		if (file == null || !file.exists())
			return null;

		List<String> lines = new ArrayList<String>();
		Scanner scanner = new Scanner(new BufferedInputStream(
				new FileInputStream(file)));

		while (scanner.hasNextLine()) {
			lines.add(scanner.nextLine());
		}

		scanner.close();

		return lines;
	}

}
