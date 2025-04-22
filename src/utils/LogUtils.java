package utils;

import java.io.File;
import java.util.Date;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.in.XMxmlParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;

public class LogUtils {

	private LogUtils() {
		//Private constructor to avoid unnecessary instantiation of the class
	}
	
	public static final String ARTIF_START = "_start_";
	public static final String ARTIF_END = "_end_";


	public static XLog convertToXlog(String logPath) {
		XLog xlog = null;
		File logFile = new File(logPath);

		if (logFile.getName().toLowerCase().endsWith(".mxml")){
			XMxmlParser parser = new XMxmlParser();
			if(parser.canParse(logFile)){
				try {
					xlog = parser.parse(logFile).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (logFile.getName().toLowerCase().endsWith(".xes")){
			XesXmlParser parser = new XesXmlParser();
			if(parser.canParse(logFile)){
				try {
					xlog = parser.parse(logFile).get(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return xlog;
	}
	
	
	public static XLog addArtificialStartEnd(XLog xLog) {
		XEvent artifStart = new XEventImpl(new XAttributeMapImpl());
		XConceptExtension.instance().assignName(artifStart, ARTIF_START);
		XLifecycleExtension.instance().assignStandardTransition(artifStart, StandardModel.COMPLETE);
		
		XEvent artifEnd = new XEventImpl(new XAttributeMapImpl());
		XConceptExtension.instance().assignName(artifEnd, ARTIF_END);
		XLifecycleExtension.instance().assignStandardTransition(artifEnd, StandardModel.COMPLETE);
		
		for (XTrace xTrace : xLog) {
			Date startDate = (Date) XTimeExtension.instance().extractTimestamp(xTrace.get(0)).clone();
			startDate.setTime(startDate.getTime() - 5000);
			XTimeExtension.instance().assignTimestamp(artifStart, startDate);
			xTrace.add(0, artifStart);
			
			Date endDate = (Date) XTimeExtension.instance().extractTimestamp(xTrace.get(xTrace.size()-1)).clone();
			endDate.setTime(endDate.getTime() + 5000);
			XTimeExtension.instance().assignTimestamp(artifEnd, endDate);
			xTrace.add(xTrace.size(), artifEnd);
		}
		
		return xLog;
	}

}
