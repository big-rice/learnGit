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
 * ��־��
 * @author lishengxi 
 */
public class Log  {
	private ILogWrite[] tf = new ILogWrite[LogProperties.LOG_COUNT];//LOGWrite����
	private int logcount = LogProperties.LOG_COUNT;//ָ����LOG����
	private LogLayout logLayout = new LogLayout();
	private static LogConsole lc = new LogConsole();//����̨
	private String tlrLevel= null;//����Ա��־���� ��tlrctls����
	private Logger log = null;
	/**
	 * ��ʼLOGʱ,����ļ��������LOG�����ļ������ȡ,���û�����½�.
	 * @param session logfile����Ҫ�Ĳ���
	 */
	public Log(HttpSession session){
		//�ļ���KEYֵ
		String sessionUpbrno = session.getAttribute(LogProperties.LOG_FM_SESSION_UPBRNO)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_UPBRNO);
		String sessionDepartment = 	session.getAttribute(LogProperties.LOG_FM_SESSION_DEPARTMENT)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_DEPARTMENT);
		String sessionTlrno = 	session.getAttribute(LogProperties.LOG_FM_SESSION_TLRNO)==null?"Other":(String)session.getAttribute(LogProperties.LOG_FM_SESSION_TLRNO);
		//String key = sessionUpbrno+"_"+sessionDepartment+"_"+sessionTlrno;
		this.tlrLevel = (String)session.getAttribute("log_level");
		// �����µ�Logger  
	    log  = Logger.getLogger("");  
	    // ���Appender��
	    log.removeAllAppenders();   
	    // �趨Logger�ļ���ΪDEBUG
	    log.setLevel(Level.DEBUG);
	    log.setAdditivity(true);  
	    // �����µ�Appender  
	    
	    RollingFileAppender appender = new RollingFileAppender();
	    PatternLayout layout = new PatternLayout();  
	    // log�������ʽ  
	    String conversionPattern = "[%d] %p - %m%n";  
	    layout.setConversionPattern(conversionPattern);  
	    appender.setLayout(layout);  
	    // log���·�� 
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
	    // log��������  
	    appender.setEncoding("GB18030");  
	    // true:���Ѵ���log�ļ�����׷�� false:��log������ǰ��log  
	    appender.setAppend(true);  
	    // ���õ�ǰ����  
	    appender.activateOptions();
	    appender.setMaxFileSize("10MB");
		// ���µ�Appender�ӵ�Logger��  
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
	 * ��ʽ�������Ϣ.
	 * @param level ��־����
	 * @return ���ظ�ʽ������־��Ϣ
	 */
	private String logForMat(String level){
		return logLayout.runLayout(level);
	}
	/**
	 * ѭ�����������־+�쳣���
	 * @param level ����
	 * @param mss ��Ϣ
	 * @param et �쳣
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
	 * ѭ�����������־
	 * @param level ����
	 * @param mss ��Ϣ
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
	 * ���ɻָ�������Ϣ
	 * @param mss ��Ϣ
	 */
	public void fatal(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("FATAL"))
			write("FATAL",mss);	*/
		log.fatal(mss);
	}
	/**
	 * ���ɻָ�������Ϣ+�쳣��Ϣ
	 * @param mss ��Ϣ
	 */
	public void fatal(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("FATAL"))
			write("FATAL",mss,et);*/
		log.fatal(mss,et);
	}
	/**
	 * ��ͨ��Ϣ
	 * @param mss ��Ϣ
	 */
	public void info(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("INFO"))
			write("INFO",mss);*/
		log.info(mss);
	}
	/**
	 * ��ͨ��Ϣ+�쳣
	 * @param mss ��Ϣ
	 * @param et �쳣
	 */
	public void info(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("INFO"))
			write("INFO",mss,et);*/
		log.info(mss,et);
	}
	/**
	 * ������Ϣ
	 * @param mss ��Ϣ
	 */
	public void debug(String mss){
		/*
		String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",mss);*/
		log.debug(mss);
	}
	/**
	 * ������Ϣ+�쳣
	 * @param mss ��Ϣ
	 * @param et �쳣
	 */
	public void debug(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",mss,et);*/
		log.debug(mss,et);
	}
	/**
	 * 
	 * @��������������Ҫ�Ķ��󴫵ݸ�debug����������֧�ֵĶ������json���ĸ�ʽ��ӡ
	 * @param mss :Ҫ��ӡ�Ķ���
	 * @param objname������������
	 * @author zhangjiqing
	 * @date :2014-2-18 ����01:39:59
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
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("DEBUG"))
			write("DEBUG",str);*/
		log.debug(str);
	}
	
	/**
	 * ������Ϣ
	 * @param mss ��Ϣ
	 */
	public void error(String mss){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("ERROR"))
			write("ERROR",mss);*/
		log.error(mss);
	}
	/**
	 * ������Ϣ+�쳣
	 * @param mss ��Ϣ
	 * @param et �쳣
	 */
	public void error(String mss,Exception et){
		/*String lvl = getTlrLevel();
		if(lvl==null){
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("ERROR"))
			write("ERROR",mss,et);*/
		log.error(mss,et);
	}
	/**
	 * 
	 * @��������������Ҫ�Ķ��󴫵ݸ�debug����������֧�ֵĶ������json���ĸ�ʽ��ӡ
	 * @param mss :Ҫ��ӡ�Ķ���
	 * @param objname������������
	 * @author zhangjiqing
	 * @date :2014-2-18 ����01:39:59
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
			lvl = LogProperties.LOG_FM_LEVEL;//�������Աû����־���� ���������ļ�����ļ����ӡ
		}
		if(lvl.contains("ERROR"))
			write("ERROR",str);*/
		log.error(str);
	}
	/**
	 * �������������ز���Ա��Ӧ����־����
	 * @return
	 * @author tianming
	 * @date 20140218
	 */
	public String getTlrLevel(){
		if(this.tlrLevel==null||"".equals(this.tlrLevel)){//�����ǰ����Աû��������־���𣬷���null
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
