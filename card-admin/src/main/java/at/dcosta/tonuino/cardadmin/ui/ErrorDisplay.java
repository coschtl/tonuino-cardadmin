package at.dcosta.tonuino.cardadmin.ui;

public interface ErrorDisplay {
	
	void showError(String summary, String detail);
	
	void showError(String summary, Throwable t);

}
