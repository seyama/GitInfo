package com.example.demo;

import org.springframework.stereotype.Component;

@Component
public class Repository {
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
