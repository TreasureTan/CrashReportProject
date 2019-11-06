package com.ysten.ystenreport.utils;

public class ReportTransUtil {
	private static final String SPLITE_STR = "&";
	private static final String EMPTY_FILL = "#";
	
	public final static String transReportTo(String[] srcReport){
		if(srcReport == null){
			return null;
		}
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < srcReport.length; i++){
			String report = srcReport[i];
			if(report == null){
				report = "";
			}
			if(i > 0){
				result.append(SPLITE_STR);
			}
			result.append(Base64.encodeToString(report.getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
			if(i == srcReport.length - 1 &&
					"".equals(report)){
				result.append(SPLITE_STR + EMPTY_FILL);
			}
		}
		byte[] compressedStr = CompressUtil.zipString2Byte(result.toString());
		return Base64.encodeToString(compressedStr, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
	}
	
	public final static String[] transFromReport(String report){
		if(report == null){
			return null;
		}
		
		String decodedReport = CompressUtil.unzipByte2String(Base64.decode(report, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
		String[] values = decodedReport.split(SPLITE_STR);
		String[] result = null;
		if(EMPTY_FILL.equals(values[values.length - 1])){
			result = new String[values.length - 1];
		}else{
			result = new String[values.length];
		}
		for(int i = 0;i < result.length; i++){
			result[i] = new String(Base64.decode(values[i], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP));
		}
		return result;
	}
}
