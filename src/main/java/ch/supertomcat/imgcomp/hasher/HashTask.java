package ch.supertomcat.imgcomp.hasher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.task.ImgCompTaskBase;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Hasher-Class which generates hashes for images and creates a hash-list
 */
public class HashTask extends ImgCompTaskBase {
	/**
	 * DEFAULT_FILENAME_PATTERN
	 */
	public static final String DEFAULT_FILENAME_PATTERN = "(?i).*\\.(?:jpg|jpeg|gif|png|bak|bak1|bak2|bak3)$";

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Lists of Image Hashes
	 */
	private List<ImageHashList> imageHashLists;

	/**
	 * Constructor
	 * 
	 * @param imageHashLists Lists of Image Hashes
	 * @param progress Progress Observer
	 */
	public HashTask(List<ImageHashList> imageHashLists, ProgressObserver progress) {
		super(progress);
		this.imageHashLists = imageHashLists;
	}

	@Override
	protected void startTask() {
		int i = 0;
		for (ImageHashList imageHashList : imageHashLists) {
			if (stop) {
				break;
			}
			generateHashes(imageHashList);
			// TODO Output Filename should be configurable in ImageHashList
			ImageHashUtil.writeHashList(imageHashList, "ImageHashes-" + i + ".txt");
			i++;
		}
		progress.progressCompleted();
	}

	/**
	 * Generate Image Hashes
	 * 
	 * @param imageHashList Image Hash List
	 */
	public void generateHashes(ImageHashList imageHashList) {
		String rootPath = imageHashList.getFolder();
		Path rootFolder = Paths.get(rootPath);

		if (!Files.exists(rootFolder)) {
			logger.error("Folder does not exist: {}", rootPath);
			return;
		}

		// TODO CASE_INSENSITIVE flag really necessary?, could also be configured in pattern
		final Pattern filePattern = Pattern.compile(imageHashList.getFilenamePattern(), Pattern.CASE_INSENSITIVE);
		Predicate<Path> fileFilter = x -> filePattern.matcher(x.getFileName().toString()).matches();

		generateImageHashes(rootFolder, fileFilter, imageHashList.isRecursive(), imageHashList.getHashes());
	}

	/**
	 * Generate Image Hashes
	 * 
	 * @param folder Folder
	 * @param fileFilter File Filter
	 * @param recursive True if recursive, false otherwise
	 * @param hashes List of Hashes
	 */
	private void generateImageHashes(Path folder, Predicate<Path> fileFilter, boolean recursive, List<Hash> hashes) {
		List<Path> files;
		try (@SuppressWarnings("resource")
		Stream<Path> stream = recursive ? Files.walk(folder) : Files.list(folder)) {
			files = stream.filter(Files::isRegularFile).filter(fileFilter).toList();
		} catch (IOException e) {
			logger.error("Could not list files: {}", folder, e);
			return;
		}

		progress.progressChanged(folder.toAbsolutePath() + " (" + files.size() + "x)");
		progress.progressModeChanged(false);
		progress.progressChanged(0, files.size(), 0);

		for (Path file : files) {
			if (stop) {
				return;
			}

			String hash = ImageHashUtil.getImageHash(file);
			if (!hash.isEmpty()) {
				hashes.add(new Hash(file.toAbsolutePath().toString(), hash));
			}
			progress.progressIncreased();
		}
	}
}
