package ch.supertomcat.imgcomp.gui;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ch.supertomcat.imgcomp.comparator.HashComparatorTask;
import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.imgcomp.hasher.ImageHashList;
import ch.supertomcat.imgcomp.hasher.ImageHashUtil;
import ch.supertomcat.supertomcatutils.gui.dialog.FileDialogUtil;
import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Comp Mode Panel
 */
public class CompModePanel extends ModePanelBase {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param defaultSearchMode Default Search Mode
	 */
	public CompModePanel(SearchMode defaultSearchMode) {
		super("Compare", "ImageHashList Files", defaultSearchMode);
	}

	@Override
	protected File selectFileToAdd() {
		return FileDialogUtil.showFileOpenDialog(this, "", null);
	}

	@Override
	protected List<File> selectFilesToDrop(List<File> droppedFiles) {
		return droppedFiles.stream().filter(File::isFile).collect(Collectors.toList());
	}

	@Override
	protected void run() {
		final ProgressObserver progress = new ProgressObserver();

		final List<ImageHashList> imageHashLists = new ArrayList<>();
		for (int i = 0; i < listModel.size(); i++) {
			String imageHashListFile = listModel.get(i).getAbsolutePath();
			ImageHashList imageHashList = ImageHashUtil.readHashList(imageHashListFile);
			imageHashLists.add(imageHashList);
		}

		final SearchMode searchMode = pnlSearchMode.getSelectedSearchMode();

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					progress.addProgressListener(CompModePanel.this);

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
					progress.removeProgressListener(CompModePanel.this);
				}
			}
		});
		thread.setName("Comp-Thread-" + thread.getId());
		thread.start();
	}
}
