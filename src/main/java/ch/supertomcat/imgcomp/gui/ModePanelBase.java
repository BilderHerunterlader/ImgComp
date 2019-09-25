package ch.supertomcat.imgcomp.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.supertomcat.imgcomp.comparator.Duplicate;
import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.imgcomp.gui.renderer.FileCellRenderer;
import ch.supertomcat.imgcomp.task.ImgCompTask;
import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;

/**
 * Base class for Mode Panel
 */
public abstract class ModePanelBase extends JPanel implements IProgressObserver {
	private static final long serialVersionUID = 1L;

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * List Model
	 */
	protected final DefaultListModel<File> listModel = new DefaultListModel<>();

	/**
	 * List
	 */
	protected final JList<File> list = new JList<>(listModel);

	/**
	 * Panel containing the list
	 */
	protected final JPanel pnlList = new JPanel(new BorderLayout());

	/**
	 * Buttons Panel
	 */
	protected final JPanel pnlButtons = new JPanel();

	/**
	 * Search Button
	 */
	protected final JButton btnRun;

	/**
	 * Add Button
	 */
	protected final JButton btnAdd = new JButton("+");

	/**
	 * Remove Button
	 */
	protected final JButton btnRemove = new JButton("-");

	/**
	 * Clear Button
	 */
	protected final JButton btnClear = new JButton("Clear");

	/**
	 * Search Mode Panel
	 */
	protected final SearchModePanel pnlSearchMode;

	/**
	 * Output Text Area
	 */
	private JTextArea txtOutput = new JTextArea(20, 120);

	/**
	 * ProgressBar
	 */
	private JProgressBar pg = new JProgressBar();

	/**
	 * Stop Flag
	 */
	protected boolean stop = false;

	/**
	 * Running Task or null
	 */
	protected ImgCompTask runningTask = null;

	/**
	 * Running Task Sync Object
	 */
	protected final Object runnungTaskSyncObject = new Object();

	/**
	 * Constructor
	 * 
	 * @param runButtonTitle Title for Run Button
	 * @param listBorderTitle Title for List Border
	 * @param defaultSearchMode Default Search Mode
	 */
	public ModePanelBase(String runButtonTitle, String listBorderTitle, SearchMode defaultSearchMode) {
		super(new BorderLayout());
		this.btnRun = new JButton(runButtonTitle);

		pnlSearchMode = new SearchModePanel(defaultSearchMode);

		list.setCellRenderer(new FileCellRenderer(true));
		list.setVisibleRowCount(-1);
		pnlList.add(new JScrollPane(list), BorderLayout.CENTER);
		pnlList.setBorder(BorderFactory.createTitledBorder(listBorderTitle));

		JPanel pnlButtons = new JPanel();
		pnlButtons.add(btnRun);
		pnlButtons.add(btnAdd);
		pnlButtons.add(btnRemove);
		pnlButtons.add(btnClear);

		btnRun.addActionListener(e -> {
			if (runningTask == null) {
				clearResult();
				run();
			}
		});
		btnAdd.addActionListener(e -> {
			File file = selectFileToAdd();
			if (file != null) {
				listModel.addElement(file);
			}
		});
		btnRemove.addActionListener(e -> {
			int indices[] = list.getSelectedIndices();
			for (int i = indices.length - 1; i >= 0; i--) {
				listModel.remove(indices[i]);
			}
		});
		btnClear.addActionListener(e -> listModel.removeAllElements());

		pnlList.add(pnlButtons, BorderLayout.SOUTH);

		JPanel pnlInput = new JPanel(new GridLayout(1, 2));
		pnlInput.add(pnlSearchMode);
		pnlInput.add(pnlList);

		txtOutput.setEditable(false);

		pg.setStringPainted(true);

		add(pnlInput, BorderLayout.NORTH);
		add(new JScrollPane(txtOutput), BorderLayout.CENTER);
		add(pg, BorderLayout.SOUTH);

		DropTargetListener dtl = new DropTargetListener() {
			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
				checkDragAccepted(dtde);
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				checkDragAccepted(dtde);
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
				checkDragAccepted(dtde);
			}

			@Override
			public void dragExit(DropTargetEvent dtde) {
				// Nothing to do
			}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.rejectDrop();
					return;
				}

				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

				try {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

					List<File> selectedFiles = selectFilesToDrop(files);
					for (File file : selectedFiles) {
						listModel.addElement(file);
					}

					dtde.dropComplete(true);
				} catch (UnsupportedFlavorException | IOException e) {
					logger.error("Could not drop files", e);
					dtde.dropComplete(false);
				}
			}

			private void checkDragAccepted(DropTargetDragEvent dtde) {
				if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
				} else {
					dtde.rejectDrag();
				}
			}
		};
		new DropTarget(list, dtl);
	}

	/**
	 * Select file to add, when add button is pressed
	 * 
	 * @return File to add
	 */
	protected abstract File selectFileToAdd();

	/**
	 * Select files to add to list, when files are dropped
	 * 
	 * @param droppedFiles Files which were originally dropped
	 * @return Files to add to list
	 */
	protected abstract List<File> selectFilesToDrop(List<File> droppedFiles);

	/**
	 * Run Method, which is called, when Run Button is pressed
	 */
	protected abstract void run();

	/**
	 * Display Result
	 * 
	 * @param duplicates Duplicates
	 */
	protected void displayResult(List<Duplicate> duplicates) {
		StringBuilder sb = new StringBuilder();

		for (Duplicate duplicate : duplicates) {
			sb.append("Duplicates Found (" + duplicate.getHash() + "):\n");
			for (int c = 0; c < duplicate.getFiles().size(); c++) {
				sb.append("\t" + duplicate.getFiles().get(c) + "\n");
			}
		}

		txtOutput.setText(sb.toString());
	}

	/**
	 * Clear Result
	 */
	protected void clearResult() {
		txtOutput.setText("");
	}

	/**
	 * Stop Running Task
	 */
	public void stopRunningTask() {
		synchronized (runnungTaskSyncObject) {
			stop = true;
			if (runningTask != null) {
				runningTask.stop();
			}
		}
	}

	/**
	 * Enable Input
	 */
	public void enableInput() {
		btnRun.setEnabled(true);
		btnAdd.setEnabled(true);
		btnRemove.setEnabled(true);
		btnClear.setEnabled(true);
		list.setEnabled(false);
		pnlSearchMode.enableInput();
	}

	/**
	 * Disable Input
	 */
	public void disableInput() {
		btnRun.setEnabled(false);
		btnAdd.setEnabled(false);
		btnRemove.setEnabled(false);
		btnClear.setEnabled(false);
		list.setEnabled(false);
		pnlSearchMode.disableInput();
	}

	/**
	 * Add Input File
	 * 
	 * @param file File
	 */
	public void addInputFile(File file) {
		listModel.addElement(file);
	}

	@Override
	public void progressIncreased() {
		if (EventQueue.isDispatchThread()) {
			pg.setValue(pg.getValue() + 1);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setValue(pg.getValue() + 1);
				}
			});
		}
	}

	@Override
	public void progressChanged(final int val) {
		if (EventQueue.isDispatchThread()) {
			pg.setValue(val);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setValue(val);
				}
			});
		}
	}

	@Override
	public void progressChanged(final int min, final int max, final int val) {
		if (EventQueue.isDispatchThread()) {
			pg.setMinimum(min);
			pg.setMaximum(max);
			pg.setValue(val);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setMinimum(min);
					pg.setMaximum(max);
					pg.setValue(val);
				}
			});
		}
	}

	@Override
	public void progressChanged(final String text) {
		if (EventQueue.isDispatchThread()) {
			pg.setString(text);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setString(text);
				}
			});
		}
	}

	@Override
	public void progressChanged(final boolean visible) {
		if (EventQueue.isDispatchThread()) {
			pg.setVisible(visible);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setVisible(visible);
				}
			});
		}
	}

	@Override
	public void progressModeChanged(final boolean indeterminate) {
		if (EventQueue.isDispatchThread()) {
			pg.setIndeterminate(indeterminate);
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setIndeterminate(indeterminate);
				}
			});
		}
	}

	@Override
	public void progressCompleted() {
		if (EventQueue.isDispatchThread()) {
			pg.setValue(pg.getMaximum());
			pg.setString("Done");
		} else {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					pg.setValue(pg.getMaximum());
					pg.setString("Done");
				}
			});
		}
	}
}
