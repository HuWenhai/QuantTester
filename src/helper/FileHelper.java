package helper;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileHelper {
	public final static List<Path> listSourceFiles(Path dir, String wildcard) {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, wildcard)) {
			for (Path entry : stream) {
				result.add(entry);
			}
		} catch (DirectoryIteratorException | IOException ex) {
			// I/O error encounted during the iteration, the cause is an
			// IOException
			ex.printStackTrace();
		}
		return result;
	}
}
