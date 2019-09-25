package ch.supertomcat.imgcomp.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import ch.supertomcat.imgcomp.comparator.SearchMode;

/**
 * Panel for selecting search mode
 */
public class SearchModePanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/**
	 * Normal Search Radio Button
	 */
	private JRadioButton rbNormalSearch = new JRadioButton("Normal Search", true);

	/**
	 * Inclusive Search Radio Button
	 */
	private JRadioButton rbInclusiveSearch = new JRadioButton("Inclusive Search", false);

	/**
	 * Exclusive Search Radio Button
	 */
	private JRadioButton rbExclusiveSearch = new JRadioButton("Exclusive Search", false);

	/**
	 * List Inclusive Search Radio Button
	 */
	private JRadioButton rbListInclusiveSearch = new JRadioButton("List Inclusive Search", false);

	/**
	 * List Exclusive Search Radio Button
	 */
	private JRadioButton rbListExclusiveSearch = new JRadioButton("List Exclusive Search", false);

	/**
	 * Constructor
	 * 
	 * @param defaultSearchMode Default Search Mode
	 */
	public SearchModePanel(SearchMode defaultSearchMode) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder("Options"));
		add(rbNormalSearch);
		add(rbInclusiveSearch);
		add(rbExclusiveSearch);
		add(rbListInclusiveSearch);
		add(rbListExclusiveSearch);
		add(Box.createVerticalGlue());

		ButtonGroup bgOptions = new ButtonGroup();
		bgOptions.add(rbNormalSearch);
		bgOptions.add(rbInclusiveSearch);
		bgOptions.add(rbExclusiveSearch);
		bgOptions.add(rbListInclusiveSearch);
		bgOptions.add(rbListExclusiveSearch);

		selectSearchMode(defaultSearchMode);
	}

	/**
	 * Select Search Mode
	 * 
	 * @param searchMode Search Mode
	 */
	private void selectSearchMode(SearchMode searchMode) {
		switch (searchMode) {
			case INCLUSIVE_SEARCH:
				rbInclusiveSearch.setSelected(true);
				break;
			case EXCLUSIVE_SEARCH:
				rbExclusiveSearch.setSelected(true);
				break;
			case LIST_INCLUSIVE_SEARCH:
				rbListInclusiveSearch.setSelected(true);
				break;
			case LIST_EXCLUSIVE_SEARCH:
				rbListExclusiveSearch.setSelected(true);
				break;
			case NORMAL_SEARCH:
			default:
				rbNormalSearch.setSelected(true);
				break;
		}
	}

	/**
	 * @return Selected Search Mode
	 */
	public SearchMode getSelectedSearchMode() {
		if (rbNormalSearch.isSelected()) {
			return SearchMode.NORMAL_SEARCH;
		} else if (rbInclusiveSearch.isSelected()) {
			return SearchMode.INCLUSIVE_SEARCH;
		} else if (rbExclusiveSearch.isSelected()) {
			return SearchMode.EXCLUSIVE_SEARCH;
		} else if (rbListInclusiveSearch.isSelected()) {
			return SearchMode.LIST_INCLUSIVE_SEARCH;
		} else if (rbListExclusiveSearch.isSelected()) {
			return SearchMode.LIST_EXCLUSIVE_SEARCH;
		} else {
			return SearchMode.NORMAL_SEARCH;
		}
	}

	/**
	 * Enable Input
	 */
	public void enableInput() {
		rbNormalSearch.setEnabled(true);
		rbInclusiveSearch.setEnabled(true);
		rbExclusiveSearch.setEnabled(true);
		rbListInclusiveSearch.setEnabled(true);
		rbListExclusiveSearch.setEnabled(true);
	}

	/**
	 * Disable Input
	 */
	public void disableInput() {
		rbNormalSearch.setEnabled(false);
		rbInclusiveSearch.setEnabled(false);
		rbExclusiveSearch.setEnabled(false);
		rbListInclusiveSearch.setEnabled(false);
		rbListExclusiveSearch.setEnabled(false);
	}
}
