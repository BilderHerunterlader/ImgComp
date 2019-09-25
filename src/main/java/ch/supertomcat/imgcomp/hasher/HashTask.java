package ch.supertomcat.imgcomp.hasher;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.regex.Pattern;

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
	public static final String DEFAULT_FILENAME_PATTERN = ".*\\.(?:jpg|jpeg|gif|png|bak|bak1|bak2|bak3)$";

	/**
	 * Folder Filter
	 */
	private static final FileFilter FOLDER_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory()) {
				return true;
			}
			return false;
		}
	};

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
		if (rootPath.isEmpty()) {
			// TODO Make this linux compatible
			rootPath = ".\\";
		} else {
			// TODO Make this linux compatible
			if (!rootPath.endsWith("\\")) {
				rootPath += "\\";
			}
		}

		File rootFolder = new File(rootPath);
		if (!rootFolder.exists()) {
			logger.error("Folder does not exist: {}", rootPath);
			return;
		}

		// TODO CASE_INSENSITIVE flag really necessary?, could also be configured in pattern
		FileFilter fileFilter = new FileFilter() {
			/**
			 * File Pattern
			 */
			private Pattern filePattern = Pattern.compile(imageHashList.getFilenamePattern(), Pattern.CASE_INSENSITIVE);

			@Override
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					return filePattern.matcher(pathname.getName()).matches();
				}
				return false;
			}
		};

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
	private void generateImageHashes(File folder, FileFilter fileFilter, boolean recursive, List<Hash> hashes) {
		File[] files = folder.listFiles(fileFilter);
		if (files == null) {
			return;
		}

		progress.progressChanged(folder.getAbsolutePath() + " (" + files.length + "x)");
		progress.progressModeChanged(false);
		progress.progressChanged(0, files.length, 0);

		for (File file : files) {
			if (stop) {
				return;
			}

			String hash = ImageHashUtil.getImageHash(file);
			if (hash.length() > 0) {
				hashes.add(new Hash(file.getAbsolutePath(), hash));
			}
			progress.progressIncreased();
		}

		if (recursive) {
			File[] subFolders = folder.listFiles(FOLDER_FILTER);
			if (subFolders != null) {
				for (File subFolder : subFolders) {
					if (stop) {
						break;
					}
					generateImageHashes(subFolder, fileFilter, recursive, hashes);
				}
			}
		}
	}
}
