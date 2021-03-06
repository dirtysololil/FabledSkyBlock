package com.songoda.skyblock.challenge.challenge;

public class Peer<E, F> {
	private E key;
	private F value;

	public Peer(E key, F value) {
		this.key = key;
		this.value = value;
	}

	public E getKey() {
		return key;
	}

	public F getValue() {
		return value;
	}
}