package com.stintAnalyzer.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.stintAnalyzer.dto.live.LiveData;
import com.stintAnalyzer.dto.session.Session;

import java.io.File;

public class Parser
{
	public static Session parseSessionFile(File file)
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		Session session;

		try
		{
			session = mapper.readValue(file, Session.class);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("Something went wrong parsing the session file!");
			return null;
		}

		return session;
	}

	public static LiveData parseLiveDataFile(File file)
	{
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		LiveData liveData = null;

		int tries = 1;
		int maxRetryAttempts = 3;
		do {
			try
			{
				liveData = mapper.readValue(file, LiveData.class);
				break;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Something went wrong parsing the session file!");
				System.out.println("Retry attempt " + tries + " of " + maxRetryAttempts);
				tries++;
			}
		}
		while (tries <= maxRetryAttempts);

		return liveData;
	}
}
