package com.stintAnalyzer.ui;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Console
{
	private final JFrame frame;
	private final JTextPane pane;

	public Console()
	{
		// Create JFrame and use it to exit the program
		frame = new JFrame("iRacing Stint Analyzer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		WindowListener listener = new WindowAdapter()
		{
			public void windowClosing(WindowEvent event)
			{
				Frame frame = (Frame) event.getSource();
				System.out.println("Closing " + frame.getTitle());
				System.exit(0);
			}
		};
		Container container = frame.getContentPane();
		pane = new JTextPane();
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setForeground(attributeSet, Color.black);
		pane.setCharacterAttributes(attributeSet, true);
		pane.setText("Close this window to stop iRacing Stint Analyzer!");
		container.add(pane, BorderLayout.CENTER);
		frame.setSize(400, 80);
		frame.addWindowListener(listener);
	}

	public void showConsole()
	{
//		frame.setAlwaysOnTop (true);
		frame.setVisible(true);
	}

	public void setConsoleText(String consoleText)
	{
		pane.setText(consoleText);
	}
}
