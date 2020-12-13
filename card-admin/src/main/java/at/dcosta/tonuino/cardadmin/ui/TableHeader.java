package at.dcosta.tonuino.cardadmin.ui;

import java.io.Serializable;

public class TableHeader implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private final String name;
	private final ValueResolver<?> valueResolver;
	
	public TableHeader(String name, ValueResolver<?> valueResolver) {
		this.name = name;
		this.valueResolver = valueResolver;
	}
	public String getName() {
		return name;
	}
	public ValueResolver<?> getValueResolver() {
		return valueResolver;
	}
	
	

}
