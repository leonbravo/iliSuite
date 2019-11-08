package ai.iliSuite.view.wizard;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public abstract class BaseWizard {
	protected List<StepViewController> steps;
	protected int index = -1;
	
	protected EventHandler<ActionEvent> finishHandler;
	protected EventHandler<ActionEvent> cancelHandler;
	protected EventHandler<ActionEvent> backHandler;

	public BaseWizard() {
		steps = new ArrayList<StepViewController>();
	}

	public BaseWizard(List<StepViewController> children) {
		children = new ArrayList<>(children);
	}

	abstract protected void drawPage(StepViewController item);
	abstract protected void loadedPage();

	public void goForward() {
		StepViewController actualItem = null;

		if (!steps.isEmpty() && index >= 0 && index < steps.size()) {
			actualItem = steps.get(index);
		}
		StepArgs args = new StepArgs();
		if(actualItem != null) {
			actualItem.goForward(args);
		}

		if(!args.isCancel() && index < steps.size()) {
			index++;
			if(index < steps.size()) {
				StepViewController nextItem = steps.get(index);
				drawPage(nextItem);
				loadedPage();
			} else {
				index = steps.size()-1;
				if (finishHandler != null) {
					finishHandler.handle(new ActionEvent());
				}
			}
		}
	}

	// XXX Excepetion is too generic
	public void init() throws Exception {
		if(steps.isEmpty()) {
			throw new Exception("The wizard does not have pages.");
		}
		index = 0;
		StepViewController actualItem = steps.get(index);
		drawPage(actualItem);
	}

	public void goBack() {
		
		if(index >= 0 && index < steps.size()) {
			StepViewController actualItem = steps.get(index);
			StepArgs args = new StepArgs();
			actualItem.goBack(args);
			if(!args.isCancel()) {
				index--;
				if(index>=0) {
					actualItem = steps.get(index);
					drawPage(actualItem);
					loadedPage();
				} else {
					index = 0;
					if(backHandler != null) {
						backHandler.handle(new ActionEvent());
					}
				}
			}
		}
	}

	public void cancel() {
		StepViewController actualItem = steps.get(index);
		StepArgs args = new StepArgs();
		actualItem.cancel(args);
		if(!args.isCancel()) {
			if(cancelHandler != null) {
				cancelHandler.handle(new ActionEvent());
			}
		}
	}

	public void add(StepViewController item) {
		steps.add(item);
	}

	public void remove(StepViewController item) {
		steps.remove(item);
	}
	
	public void add(List<StepViewController> items) {
		steps.addAll(items);
	}

	public void setOnFinish(EventHandler<ActionEvent> handler) {
		this.finishHandler = handler;
	}
	
	public void setOnCancel(EventHandler<ActionEvent> handler) {
		this.cancelHandler = handler;
	}
	
	public void setOnBack(EventHandler<ActionEvent> handler) {
		this.backHandler = handler;
	}
}
