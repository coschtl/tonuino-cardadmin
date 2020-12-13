package at.dcosta.tonuino.cardadmin.ui;

import java.io.Serializable;

import at.dcosta.tonuino.cardadmin.Track;

public interface ValueResolver<T> extends Serializable {
	
	T getValue(Track track);
	void  setValue(T value, Track track);

}
