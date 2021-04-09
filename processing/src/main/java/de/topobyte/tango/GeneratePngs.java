package de.topobyte.tango;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import de.topobyte.melon.paths.PathUtil;
import de.topobyte.processutils.Processes;
import de.topobyte.system.utils.SystemPaths;

public class GeneratePngs
{

	final static Logger logger = LoggerFactory.getLogger(GeneratePngs.class);

	private static String PROP_REPO = "repo";
	private static String DIRNAME = "scalable";

	private static int[] sizes = new int[] { 16, 22, 32 };

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

		for (int size : sizes) {
			Path dir = repo.resolve(String.format("gen-%d", size));
			generate(scalable, size, dir);
		}
	}

	private static void generate(Path scalable, int size, Path dirSize)
			throws IOException
	{
		List<Path> dirs = PathUtil.list(scalable);
		Collections.sort(dirs);
		for (Path dir : dirs) {
			if (!Files.isDirectory(dir)) {
				continue;
			}
			generateFiles(dir, size, dirSize);
		}
	}

	private static void generateFiles(Path dir, int size, Path dirSize)
			throws IOException
	{
		Path relDirSize = repo.relativize(dirSize);

		List<Path> files = PathUtil.list(dir);
		Collections.sort(files);
		for (Path file : files) {
			if (!Files.isRegularFile(file)
					|| !file.getFileName().toString().endsWith(".svg")) {
				continue;
			}
			Path relative = repo.relativize(file);
			Path target = relDirSize
					.resolve(relative.getName(0).relativize(relative));
			String filename = target.getFileName().toString().replace(".svg",
					".png");
			target = target.resolveSibling(filename);

			if (Files.exists(repo.resolve(target))) {
				System.out.println("SKIP " + target);
				continue;
			}

			Path targetDir = repo.resolve(target.getParent());
			Files.createDirectories(targetDir);

			execute(String.format("inkscape -C -w %d -e %s %s", size, target,
					relative));
		}
	}

	private static void execute(String command)
	{
		System.out.println(command);

		List<String> args = Splitter.on(" ").splitToList(command);

		ProcessBuilder pb = new ProcessBuilder();
		pb.command(args);
		pb.directory(repo.toFile());
		Processes.trySimple(pb, "Error while converting image");
	}

}
