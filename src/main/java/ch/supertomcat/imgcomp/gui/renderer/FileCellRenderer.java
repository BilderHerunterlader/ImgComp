package ch.supertomcat.imgcomp.gui.renderer;

import java.awt.Component;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.filechooser.FileSystemView;

/**
 * Renderer for File
 */
public class FileCellRenderer extends DefaultListCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Full Path Display
	 */
	private boolean fullPathDisplay = false;

	/**
	 * Constructor
	 * 
	 * @param fullPathDisplay Full Path Display
	 */
	public FileCellRenderer(boolean fullPathDisplay) {
		this.fullPathDisplay = fullPathDisplay;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		if (value instanceof File) {
			File file = (File)value;
			if (fullPathDisplay) {
				setText(file.getAbsolutePath());
			} else {
				setText(file.getName());
			}
			setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			setEnabled(list.isEnabled());
			setFont(list.getFont());
			setOpaque(true);
		}
		return this;
	}
}
