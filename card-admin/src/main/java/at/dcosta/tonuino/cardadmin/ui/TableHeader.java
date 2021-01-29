package at.dcosta.tonuino.cardadmin.ui;

import java.io.Serializable;

public class TableHeader implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	private final ValueAdapter<?> valueAdapter;
	
	public TableHeader(String name, ValueAdapter<?> valueAdapter) {
		this.name = name;
		this.valueAdapter = valueAdapter;
	}
	public String getName() {
		return name;
	}
	
	public ValueAdapter<?> getValueAdapter() {
		return valueAdapter;
	}
	
}
