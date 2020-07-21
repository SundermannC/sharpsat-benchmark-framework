package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import resultpackages.BinaryResult;
import utils.BenchmarkUtils.Status;

public class BinaryRunner {
	
	
	public final static String TIMEOUT_REACHED = "TIMEOUT!!";
	public final static String MONITORING_PREFIX = "/usr/bin/time -v ";
	public final static String SCRIPT_PREFIX = "/bin/sh";
	public final static String MEMORY_ULIMIT_PREFIX = "ulimit -v ";
	
		
	long currentlyRunningPid;
	
	long timeout;
	
	String user;
	
	public BinaryRunner(long timeout, String user) {
		this.currentlyRunningPid = -1;
		this.timeout = timeout;
		this.user = user;
	}
	
	public BinaryRunner(long timeout) {
		this.currentlyRunningPid = -1;
		this.timeout = timeout;
	}
	
	
	public void killCurrentProcess() {
		if (currentlyRunningPid >= 0) {
			final Runtime rt = Runtime.getRuntime();
			try {
				rt.exec(getKillCommand(currentlyRunningPid));
				currentlyRunningPid = -1;
			} catch (IOException e) {
				System.out.println("Failed to kill " + currentlyRunningPid);
			}
		}
	}
	
	public BinaryResult runBinary(String[] command) throws InterruptedException {
		final Runtime rt = Runtime.getRuntime();
		try {
			final Process ps = rt.exec(command);
			currentlyRunningPid = getPidOfProcess(ps);
			if(!ps.waitFor(timeout, TimeUnit.MINUTES)) {
			    return new BinaryResult("", Status.TIMEOUT);
			}

			String val = "";
		    String line;
		    
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		    while ((line = in.readLine()) != null) {
		        val +=line + "\n";
		    }
		    BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		    while ((line = err.readLine()) != null) {
		        val +=line + "\n";
		    }
			in.close();
			return new BinaryResult(val, Status.SOLVED);
		} catch (final IOException e) {
			return null;
		}
	}
	
	public BinaryResult runBinary(String command, long timeout) throws InterruptedException {
		final Runtime rt = Runtime.getRuntime();
		try {
			final Process ps = rt.exec(command);
			currentlyRunningPid = getPidOfProcess(ps);
			if(!ps.waitFor(timeout, TimeUnit.MINUTES)) {
			    return new BinaryResult("", Status.TIMEOUT);
			}

			String val = "";
		    String line;
		    
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		    while ((line = in.readLine()) != null) {
		        val +=line + "\n";
		    }
		    BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		    while ((line = err.readLine()) != null) {
		        val +=line + "\n";
		    }
			in.close();
			return new BinaryResult(val, Status.SOLVED);
		} catch (final IOException e) {
			return null;
		}
	}
	
	
	public static BinaryResult runBinaryStatic(String command, long timeout) {
		final Runtime rt = Runtime.getRuntime();
		try {
			final Process ps = rt.exec(command);
			long pid = getPidOfProcess(ps);
			try {
				if(!ps.waitFor(timeout, TimeUnit.MINUTES)) {
					final Process killPs = rt.exec(getKillCommand(pid));
					killPs.waitFor(10, TimeUnit.SECONDS);
				    return new BinaryResult("", Status.TIMEOUT);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			String val = "";
		    String line;
		    
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
		    while ((line = in.readLine()) != null) {
		        val +=line + "\n";
		    }
		    BufferedReader err = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
		    while ((line = err.readLine()) != null) {
		        val +=line + "\n";
		    }
			in.close();
			return new BinaryResult(val, Status.SOLVED);
		} catch (final IOException e) {
			return null;
		}
	}
	
	public static String getUlimitString(int memoryLimit) {
		int kbMemoryLimit = memoryLimit * 1000;
		return MEMORY_ULIMIT_PREFIX + kbMemoryLimit + "; ";
	}
	
	public static long getPidOfProcess(Process ps) {
		 long pid = -1;

		    try {
		      if (ps.getClass().getName().equals("java.lang.UNIXProcess")) {
		        Field f = ps.getClass().getDeclaredField("pid");
		        f.setAccessible(true);
		        pid = f.getLong(ps);
		        f.setAccessible(false);
		      }
		    } catch (Exception e) {
		      pid = -1;
		    }
		    return pid;
	}
	
	private static String getKillCommand(long pid) {
		return "rkill " + pid;
	}
	
	public static void killProcessesByUserAndName(String user, String binaryName) {
		final Runtime rt = Runtime.getRuntime();
		Process ps;
		try {
			ps = rt.exec("killall -u " + user + " " + binaryName);
			ps.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
	
	public void killProcessesByUserAndName(String binaryName) {
		killProcessesByUserAndName(user, binaryName);
	}
	
}
