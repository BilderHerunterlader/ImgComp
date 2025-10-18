package ch.supertomcat.imgcomp.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import ch.supertomcat.imgcomp.comparator.SearchMode;
import ch.supertomcat.supertomcatutils.application.ApplicationMain;
import ch.supertomcat.supertomcatutils.application.ApplicationProperties;
import ch.supertomcat.supertomcatutils.gui.PositionUtil;
import ch.supertomcat.supertomcatutils.gui.progress.IProgressObserver;

/**
 * Main-Window
 */
public class MainWindow extends JFrame implements IProgressObserver {
	private static final long serialVersionUID = 1L;

	/**
	 * Hash Mode Panel
	 */
	private HashModePanel pnlHashMode;

	/**
	 * Comp Mode Panel
	 */
	private CompModePanel pnlCompMode;

	/**
	 * Tabs
	 */
	private JTabbedPane tabs = new JTabbedPane();

	/**
	 * Status Label
	 */
	private JLabel lblStatus = new JLabel("Ready");

	/**
	 * Constructor
	 * 
	 * @param guiMode GUI Mode
	 * @param defaultSearchMode Default Search Mode
	 */
	public MainWindow(GuiMode guiMode, SearchMode defaultSearchMode) {
		super(ApplicationProperties.getProperty(ApplicationMain.APPLICATION_NAME) + " (" + ApplicationProperties.getProperty(ApplicationMain.APPLICATION_VERSION) + ")");
		if (guiMode == GuiMode.NORMAL) {
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		} else {
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}

		setLayout(new BorderLayout());

		pnlHashMode = new HashModePanel(defaultSearchMode);
		pnlCompMode = new CompModePanel(defaultSearchMode);

		switch (guiMode) {
			case CLI_HASH:
				pnlHashMode.disableInput();
				add(pnlHashMode, BorderLayout.CENTER);
				break;
			case CLI_COMP:
				pnlCompMode.disableInput();
				add(pnlCompMode, BorderLayout.CENTER);
				break;
			case NORMAL:
			default:
				tabs.add("Hash Mode", pnlHashMode);
				tabs.add("Compare Mode", pnlCompMode);

				add(tabs, BorderLayout.CENTER);

				addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						pnlHashMode.stopRunningTask();
						pnlCompMode.stopRunningTask();
					}
				});
				break;
		}

		add(lblStatus, BorderLayout.SOUTH);

		pack();
		PositionUtil.setPositionMiddleScreen(this, null);
	}

	/**
	 * Add Input File
	 * 
	 * @param file File
	 */
	public void addInputFile(File file) {
		pnlHashMode.addInputFile(file);
		pnlCompMode.addInputFile(file);
	}

	@Override
	public void progressIncreased() {
		pnlHashMode.progressIncreased();
		pnlCompMode.progressIncreased();
	}

	@Override
	public void progressChanged(final int val) {
		pnlHashMode.progressChanged(val);
		pnlCompMode.progressChanged(val);
	}

	@Override
	public void progressChanged(final int min, final int max, final int val) {
		pnlHashMode.progressChanged(min, max, val);
		pnlCompMode.progressChanged(min, max, val);
	}

	@Override
	public void progressChanged(final String text) {
		pnlHashMode.progressChanged(text);
		pnlCompMode.progressChanged(text);
	}

	@Override
	public void progressChanged(final boolean visible) {
		pnlHashMode.progressChanged(visible);
		pnlCompMode.progressChanged(visible);
	}

	@Override
	public void progressModeChanged(final boolean indeterminate) {
		pnlHashMode.progressModeChanged(indeterminate);
		pnlCompMode.progressModeChanged(indeterminate);
	}

	@Override
	public void progressCompleted() {
		pnlHashMode.progressCompleted();
		pnlCompMode.progressCompleted();
	}
}
