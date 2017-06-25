package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigReader {
	@Autowired
	private Settings settings;
	@Autowired
	private ProxySettings proxySettings;
	
	public Settings getSettings() {
		return settings;
	}
	public ProxySettings getProxySettings() {
		return proxySettings;
	}
}
