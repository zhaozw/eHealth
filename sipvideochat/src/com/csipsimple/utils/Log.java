/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.csipsimple.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.os.Environment;

public class Log {
	private static int logLevel = 1;
	
	/**
	 * Change current logging level
	 * @param level new log level 1 <= level <= 6 
	 */
	public static void setLogLevel(int level) {
		logLevel = level;
	}
	
	/**
	 * Get the current log level
	 * @return the log level
	 */
	public static int getLogLevel() {
	    return logLevel;
	}
	
	/**
	 * Log verbose
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 */
	public static void v(String tag, String msg) {
		if(logLevel >= 5) {
			android.util.Log.v(tag, msg);
		}
	}

	/**
	 * Log verbose
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 * @param tr Error to serialize in log
	 */
	public static void v(String tag, String msg, Throwable tr) {
		if(logLevel >= 5) {
			android.util.Log.v(tag, msg, tr);
		}
	}
	
	/**
	 * Log debug
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 */
	public static void d(String tag, String msg) {
		if(logLevel >= 4) {
			android.util.Log.d(tag, msg);
		}
	}

	/**
	 * Log debug
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 * @param tr Error to serialize in log
	 */
	public static void d(String tag, String msg, Throwable tr) {
		if(logLevel >= 4) {
			android.util.Log.d(tag, msg, tr);
		}
	}
	
	/**
	 * Log info
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 */
	public static void i(String tag, String msg) {
		if(logLevel >= 3) {
			android.util.Log.i(tag, msg);
		}
	}

	/**
	 * Log info
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 * @param tr Error to serialize in log
	 */
	static void i(String tag, String msg, Throwable tr) {
		if(logLevel >= 3) {
			android.util.Log.i(tag, msg, tr);
		}
	}

	/**
	 * Log warning
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 */
	public static void w(String tag, String msg) {
		if(logLevel >= 2) {
			android.util.Log.w(tag, msg);
		}
	}

	/**
	 * Log warning
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 * @param tr Error to serialize in log
	 */
	public static void w(String tag, String msg, Throwable tr) {
		if(logLevel >= 2) {
			android.util.Log.w(tag, msg, tr);
		}
	}

	/**
	 * Log error
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 */
	public static void e(String tag, String msg) {
		if(logLevel >= 1) {
			android.util.Log.e(tag, msg);
		}
	}

	/**
	 * Log error
	 * @param tag Tag for this log
	 * @param msg Msg for this log
	 * @param tr Error to serialize in log
	 */
	public static void e(String tag, String msg, Throwable tr) {
		if(logLevel >= 1) {
			android.util.Log.e(tag, msg, tr);
		}
	}

	public static void writeLogToFile(String trace) {
		try
		{
//			if (true) {
//				return;
//			}
			String filename = getErrorLogPath();
			new File(filename).getParentFile().mkdirs();
//		    String filename= parent+"error.txt";
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(trace+"\n");//appends the string to the file
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}

	public static String getErrorLogPath() {
		String filename = Environment.getExternalStorageDirectory().toString()+"/nelsons/"+"error.txt";
		File parent = new File(Environment.getExternalStorageDirectory().toString()+"/nelsons/");
		parent.mkdirs();
		return filename;
	}

	public static void writeLogToFile(Exception e) {
		try {
//			 if (true) {
//				 return;
//			 }
			String filename = getErrorLogPath();
			new File(filename).getParentFile().mkdirs();
//			String parent = Environment.getExternalStorageDirectory()
//					.toString() + "/nelsons/";
//			new File(parent).mkdirs();
//			String filename = parent + "error.txt";
			FileWriter fw = new FileWriter(filename, true); // the true will
															// append the new
															// data
			e.printStackTrace(new PrintWriter(fw));
			// fw.write(trace+"\n");//appends the string to the file
			fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}

}
