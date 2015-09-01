package com.dhcc.log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
/**
 * 日志类
 * @author lishengxi 
 */
public class Log  {
	private ILogWrite[] tf = new ILogWrite[LogProperties.LOG_COUNT];//LOGWrite数组
	private int logcount = LogProperties.LOG_COUNT;//指定的LOG数量
	private LogLayout logLayout = new LogLayout();
	private static LogConsole lc = new LogConsole();//控制台
	private String tlrLevel= null;//操作员日志级别 在tlrctls定义
	private Logger log = null;
	/**
	 * 初始LOG时,如果文件池里存在LOG则在文件池里获取,如果没有则新建.
	 * @param session logfile所需要的参数
	 */
	public Log(HttpSession session){
		//文件池KEY值
		String sessionUpbrno = session.getAttribute(LogProperties.LOG_FM_SESSION_UPBRNO)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_UPBRNO);
		String sessionDepartment = 	session.getAttribute(LogProperties.LOG_FM_SESSION_DEPARTMENT)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_DEPARTMENT);
		String sessionTlrno = 	session.getAttribute(LogProperties.LOG_FM_SESSION_TLRNO)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_TLRNO);
		//String key = sessionUpbrno+"_"+sessionDepartment+"_"+sessionTlrno;
		this.tlrLevel = (String)session.getAttribute("log_level");
		// 生成新的Logger  
	    log  = Logger.getLogger("");  
	    // 清空Appender。
	    log.removeAllAppenders();   
	    // 设定Logger的级别为DEBUG
	    log.setLevel(Level.DEBUG);
	    log.setAdditivity(true);  
	    // 生成新的Appender  
	    
	    RollingFileAppender appender = new RollingFileAppender();
	    PatternLayout layout = new PatternLayout();  
	    // log的输出形式  
	    String conversionPattern = "[%d] %p - %m%n";  
	    layout.setConversionPattern(conversionPattern);  
	    appender.setLayout(layout);  
	    // log输出路径 
	    Date date = new Date();
	    SimpleDateFormat formater =new SimpleDateFormat("yyyyMMdd");
	    String userHome = System.getProperty("user.home");  
		String basePath = userHome + "/cwht_dir/logs/"+sessionUpbrno+"/" +sessionDepartment +"/"+formater.format(date)+"/" ;
		File folder = new File(basePath);
		if(!folder.exists()){
			folder.mkdirs();
		}
		File file = new File(basePath + "/"+sessionTlrno + ".log");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	    appender.setFile(basePath +"/"+ sessionTlrno + ".log");  
	    // log的文字码  
	    appender.setEncoding("GB18030");  
	    // true:在已存在log文件后面追加 false:新log覆盖以前的log  
	    appender.setAppend(true);  
	    // 适用当前配置  
	    appender.activateOptions();
	    appender.setMaxFileSize("10MB");
		// 将新的Appender加到Logger中  
	    if(LogProperties.LOG_FM_TALKING.contains("FILE")){
		    log.addAppender(appender);
	    }
	    if(LogProperties.LOG_FM_TALKING.contains("CONSOLE")){
	    	ConsoleAppender console = new ConsoleAppender();
		    console.setLayout(layout);
		    console.setTarget("System.out");
		    log.addAppender(console); 
		}
		/*if(LogProperties.LOG_FM_TALKING.contains("FILE")){
			LogFile lf = new LogFile(sessionDepartment,sessionUpbrno,sessionTlrno);
			if(FilePool.get(key)==null){
				FilePool.push(key, lf);
			}
			else{
				lf = (LogFile) FilePool.get(key);
			}
			tf[--logcount] = lf;
			lf = null;
		}
	    if(LogProperties.LOG_FM_TALKING.contains("CONSOLE")) 
			tf[--logcount]  = lc;*/
	}
	/**
	 * 格式化输出信息.
	 * @param level 日志级别
	 * @return 返回格式化的日志信息
	 */
	private String logForMat(String level){
		return logLayout.runLayout(level);
	}
	/**
	 * 循环数组输出日志+异常输出
	 * @param level 级别
	 * @param mss 信息
	 * @param et 异常
	 */
	private void write(String level,String mss,Exception et){
		for(int i=0;i<LogProperties.LOG_COUNT;i++){
			if(tf[i]!=null){
				try {
					tf[i].write(logForMat(level)+mss,et);
					
				} catch (Exception e) {
					//e.printStackTrace();
				}	
			}		
		}
	}
	/**
	 * 循环数组输出日志
	 * @param level 级别
	 * @param mss 信息
	 */
	private void write(String level,String mss) {
		for(int i=0;i<LogProperties.LOG_COUNT;i++){
			if(tf[i]!=null){
				try {
					tf[i].write(logForMat(level)+mss);
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}		
		}
	}
	/**
	 * 不可恢复错误信息
	 * @param mss 信息
	 */
	public void fatal(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("FATAL"))
			write("FATAL",mss);	*/
		log.fatal(mss);
	}
	/**
	 * 不可恢复错误信息+异常信息
	 * @param mss 信息
	 */
	public void fatal(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("FATAL"))
			write("FATAL",mss,et);*/
		log.fatal(mss,et);
	}
	/**
	 * 普通信息
	 * @param mss 信息
	 */
	public void info(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("INFO"))
			write("INFO",mss);*/
		log.info(mss);
	}
	/**
	 * 普通信息+异常
	 * @param mss 信息
	 * @param et 异常
	 */
	public void info(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("INFO"))
			write("INFO",mss,et);*/
		log.info(mss,et);
	}
	/**
	 * 调试信息
	 * @param mss 信息
	 */
	public void debug(String mss){
		/*
		String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",mss);*/
		log.debug(mss);
	}
	/**
	 * 调试信息+异常
	 * @param mss 信息
	 * @param et 异常
	 */
	public void debug(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",mss,et);*/
		log.debug(mss,et);
	}
	/**
	 * 
	 * @功能描述：将需要的对象传递给debug函数，对于支持的对象会以json串的格式打印
	 * @param mss :要打印的对象
	 * @param objname：对象描述，
	 * @author zhangjiqing
	 * @date :2014-2-18 下午01:39:59
	 *
	 */
	public void debug(Object mss,String objname){
		String str = objname + ":" ;
		if(!(mss instanceof String)){
			if(mss instanceof ArrayList || mss instanceof Set) {
				str += JSONArray.fromObject(mss).toString();
			}else
				str += JSONObject.fromObject(mss).toString();
		}
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",str);*/
		log.debug(str);
	}
	
	/**
	 * 错误信息
	 * @param mss 信息
	 */
	public void error(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("ERROR"))
			write("ERROR",mss);*/
		log.error(mss);
	}
	/**
	 * 错误信息+异常
	 * @param mss 信息
	 * @param et 异常
	 */
	public void error(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("ERROR"))
			write("ERROR",mss,et);*/
		log.error(mss,et);
	}
	/**
	 * 
	 * @功能描述：将需要的对象传递给debug函数，对于支持的对象会以json串的格式打印
	 * @param mss :要打印的对象
	 * @param objname：对象描述，
	 * @author zhangjiqing
	 * @date :2014-2-18 下午01:39:59
	 *
	 */
	public void error(Object mss,String objname){
		String str = objname + ":" ;
		if(!(mss instanceof String)){
			if(mss instanceof ArrayList || mss instanceof Set) {
				str += JSONArray.fromObject(mss).toString();
			}else
				str += JSONObject.fromObject(mss).toString();
		}
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//如果操作员没有日志级别 按照配置文件定义的级别打印
		}
		if(lvl.contains("ERROR"))
			write("ERROR",str);*/
		log.error(str);
	}
	/**
	 * 功能描述：返回操作员对应的日志级别
	 * @return
	 * @author tianming
	 * @date 20140218
	 */
	public String getTlrLevel(){
		if(this.tlrLevel==null||"".equals(this.tlrLevel)){//如果当前操作员没有设置日志级别，返回null
			 return null;
		}else{
			 String lvl = null;
			 if("3".equals(this.tlrLevel)){
				 lvl = "ERROR,DEBUG,INFO";
			 }else if("2".equals(this.tlrLevel)){
				 lvl = "ERROR,DEBUG";
			 }else if("1".equals(this.tlrLevel)){
				 lvl = "ERROR";
			 }else {
				 lvl = "FATAL,ERROR,DEBUG,INFO";
			 }
			 return lvl;
		}
	}
}
