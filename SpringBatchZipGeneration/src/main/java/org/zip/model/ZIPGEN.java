package org.zip.model;

import java.io.Serializable;

public class ZIPGEN implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int  ZIP_ID;
	private String FILE_NAME;
	public int getZIP_ID() {
		return ZIP_ID;
	}
	public void setZIP_ID(int zIP_ID) {
		ZIP_ID = zIP_ID;
	}
	public String getFILE_NAME() {
		return FILE_NAME;
	}
	public void setFILE_NAME(String fILE_NAME) {
		FILE_NAME = fILE_NAME;
	}
	public String getFILE_PATH() {
		return FILE_PATH;
	}
	public void setFILE_PATH(String fILE_PATH) {
		FILE_PATH = fILE_PATH;
	}
	private String FILE_PATH;
	
	
	
}
