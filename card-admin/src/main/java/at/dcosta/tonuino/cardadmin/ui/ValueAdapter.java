package at.dcosta.tonuino.cardadmin.ui;

import at.dcosta.tonuino.cardadmin.Track;

public interface ValueAdapter<T> extends ValueResolver<T> {
	
	void  setValue(T value, Track track);

}
