package ch.supertomcat.imgcomp.gui;

import java.awt.EventQueue;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ch.supertomcat.imgcomp.comparator.HashComparatorTask;
import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.imgcomp.hasher.HashTask;
import ch.supertomcat.imgcomp.hasher.ImageHashList;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;
import ch.supertomcat.supertomcatutils.io.FileUtil;

/**
 * Hash Mode Panel
 */
public class HashModePanel extends ModePanelBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param defaultSearchMode Default Search Mode
	 */
	public HashModePanel(SearchMode defaultSearchMode) {
		super("Search", "Directories", defaultSearchMode);
	}

	@Override
	protected File selectFileToAdd() {
		return FileDialogUtil.showFolderOpenDialog(this, "", null);
	}

	@Override
	protected List<Path> selectFilesToDrop(List<File> droppedFiles) {
		return droppedFiles.stream().map(File::toPath).filter(Files::isDirectory).toList();
	}

	@Override
	protected void run() {
		final ProgressObserver progress = new ProgressObserver();

		// TODO Should be configurable
		String filePattern = HashTask.DEFAULT_FILENAME_PATTERN;

		final List<ImageHashList> imageHashLists = new ArrayList<>();
		for (int i = 0; i < listModel.size(); i++) {
			String folder = listModel.get(i).toAbsolutePath().toString();
			if (!folder.endsWith("\\") && !folder.endsWith("/")) {
				folder += FileUtil.FILE_SEPERATOR;
			}
			ImageHashList imageHashList = new ImageHashList(folder, filePattern, true);
			imageHashLists.add(imageHashList);
		}

		final SearchMode searchMode = pnlSearchMode.getSelectedSearchMode();

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					progress.addProgressListener(HashModePanel.this);

					// Hash
					HashTask hashTask = new HashTask(imageHashLists, progress);
					synchronized (runnungTaskSyncObject) {
						runningTask = hashTask;
						if (stop) {
							runningTask = null;
							return;
						}
					}
					hashTask.start();

					// Compare
					HashComparatorTask compareTask = new HashComparatorTask(imageHashLists, progress, searchMode, false, true, false, false);
					synchronized (runnungTaskSyncObject) {
						runningTask = compareTask;
						if (stop) {
							runningTask = null;
							return;
						}
					}
					compareTask.start();

					synchronized (runnungTaskSyncObject) {
						runningTask = null;
						if (stop) {
							runningTask = null;
							return;
						}
					}

					EventQueue.invokeLater(new Runnable() {
						@Override
						public void run() {
							displayResult(compareTask.getDuplicates());
						}
					});
				} finally {
					progress.removeProgressListener(HashModePanel.this);
				}
			}
		});
		thread.setName("Hash-Thread-" + thread.threadId());
		thread.start();
	}
}
