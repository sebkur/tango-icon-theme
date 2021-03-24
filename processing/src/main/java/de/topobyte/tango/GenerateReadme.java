package de.topobyte.tango;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import de.topobyte.melon.paths.PathUtil;
import de.topobyte.system.utils.SystemPaths;

public class GenerateReadme
{

	private static String PROP_REPO = "repo";
	private static String DIRNAME = "scalable";

	private static Path repo;

	public static void main(String[] args) throws IOException
	{
		repo = null;
		String propRepo = System.getProperty(PROP_REPO);
		if (propRepo == null) {
			repo = SystemPaths.CWD.getParent();
		} else {
			repo = Paths.get(propRepo);
		}

		Path scalable = repo.resolve(DIRNAME);

		List<Path> dirs = PathUtil.list(scalable);
		Collections.sort(dirs);
		boolean first = true;
		for (Path dir : dirs) {
			if (!Files.isDirectory(dir)) {
				continue;
			}
			print(dir, first);
			first = false;
		}
	}

	private static void print(Path dir, boolean first) throws IOException
	{
		if (!first) {
			System.out.println();
		}
		System.out.println("## " + dir.getFileName());
		List<Path> files = PathUtil.list(dir);
		Collections.sort(files);
		for (Path file : files) {
			if (!Files.isRegularFile(file)
					|| !file.getFileName().toString().endsWith(".svg")) {
				continue;
			}
			Path relative = repo.relativize(file);
			System.out.println(String.format("![](%s)", relative));
		}
	}

}
