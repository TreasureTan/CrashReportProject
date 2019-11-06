package com.ysten.ystenreport.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressUtil {
	
	
	public static final String zipString2String(String str) {
		return new String(zipByte2Byte(str.getBytes()));
	}
	
	public static final byte[] zipString2Byte(String str) {
		return zipByte2Byte(str.getBytes());
	}
	
	public static final String zipByte2String(byte[] srcByte) {
		return new String(zipByte2Byte(srcByte));
	}
	
	
	/**
	* ???zip???????
	* @param str ?????????
	* @return ?????????????????
	*/
	public static final byte[] zipByte2Byte(byte[] str) {
		if (str == null)
			return null;
		byte[] compressed;
		ByteArrayOutputStream out = null;
		ZipOutputStream zout = null;
		try {
			out = new ByteArrayOutputStream();
			zout = new ZipOutputStream(out);
			zout.putNextEntry(new ZipEntry("0"));
			zout.write(str);
			zout.closeEntry();
			compressed = out.toByteArray();
			
		} catch (IOException e) {
			compressed = null;
		} finally {
			if (zout != null) {
				try {
					zout.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return compressed;
	}
	
	public static final String unzipString2String(String compressedStr) {
		return new String(unzipByte2Byte(compressedStr.getBytes()));
	}
	
	public static final String unzipByte2String(byte[] compressedByte) {
		return new String(unzipByte2Byte(compressedByte));
	}

	/**
	 * ???zip???н????
	 * 
	 * @param compressed
	 *            ?????????
	 * @return ???????????
	 */
	public static final byte[] unzipByte2Byte(byte[] compressed) {
		if (compressed == null) {
			return null;
		}

		ByteArrayOutputStream out = null;
		ByteArrayInputStream in = null;
		ZipInputStream zin = null;
		byte[] decompressed = null;
		try {
			out = new ByteArrayOutputStream();
			in = new ByteArrayInputStream(compressed);
			zin = new ZipInputStream(in);
			zin.getNextEntry();
			byte[] buffer = new byte[1024];
			int offset = -1;
			while ((offset = zin.read(buffer)) != -1) {
				out.write(buffer, 0, offset);
			}
			decompressed = out.toByteArray();
		} catch (IOException e) {
			decompressed = null;
		} finally {
			if (zin != null) {
				try {
					zin.close();
				} catch (IOException e) {
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return decompressed;
	}
	
    public static boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;
        File sourceFile = new File(sourcePath);
        File destFile = new File(toLocation);
        if(!destFile.exists()){
        	try {
				destFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
        }
        FileOutputStream dest = null;
        ZipOutputStream out = null;
        try {
            BufferedInputStream origin;
            dest = new FileOutputStream(toLocation);
            out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                out.flush();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
			try {
				if (out != null) {
					out.close();
				}
				if (dest != null) {
					dest.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return false;
    }

/*
 *
 * Zips a subfolder
 *
 */

    private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}  