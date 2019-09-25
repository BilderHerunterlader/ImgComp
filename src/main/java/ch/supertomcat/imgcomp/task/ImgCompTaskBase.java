package ch.supertomcat.imgcomp.task;

import ch.supertomcat.supertomcatutils.gui.progress.ProgressObserver;

/**
 * Base Class for ImgComp Tasks
 */
public abstract class ImgCompTaskBase implements ImgCompTask {
	/**
	 * Running Flag
	 */
	protected boolean running = false;

	/**
	 * Stop Flag
	 */
	protected boolean stop = false;

	/**
	 * Progress or null
	 */
	protected final ProgressObserver progress;

	/**
	 * Constructor
	 * 
	 * @param progress Progress Observer
	 */
	public ImgCompTaskBase(ProgressObserver progress) {
		this.progress = progress;
	}

	@Override
	public void start() {
		if (running) {
			return;
		}
		startTask();
	}

	/**
	 * Start Task
	 */
	protected abstract void startTask();

	@Override
	public void stop() {
		stop = true;
	}
}
