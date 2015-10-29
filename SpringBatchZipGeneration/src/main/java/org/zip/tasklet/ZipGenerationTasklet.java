package org.zip.tasklet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.DataSource;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

@Component
public class ZipGenerationTasklet implements Tasklet, InitializingBean,StepExecutionListener {

	/*private Resource directory;*/
	List<String> fileList =new ArrayList<String>();
    private  String OUTPUT_ZIP_FILE= "D:\\MyFile.zip";
    private String SOURCE_FOLDER ="D:\\NisargPics";
	
	private JdbcTemplate jdbcTemplate;
	int batchid;
	BigInteger batch_run_id;
	private long batchId=2;
	
	private SimpleJdbcCall simpleJdbcCall;
	@Autowired
	public void setDataSource(DataSource dataSource) {
	this.jdbcTemplate = new JdbcTemplate(dataSource);
	simpleJdbcCall = new SimpleJdbcCall(dataSource).withFunctionName("get_id");
	}
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		

		System.out.println("Proc call to get auto incr. value .....!");
		
		SqlParameterSource  mapParam = new MapSqlParameterSource().addValue("name_","zip_dtl");
		
		
		Map<String,Object> map =  simpleJdbcCall.execute(mapParam);
	
		System.out.println("Executing The Procedure  .....!");
		BigInteger id = (BigInteger)map.get("sequence_no_");
		
		// batch_run_id=jdbcTemplate.queryForObject("SELECT sequence_no+1 FROM sequence_generate_tab", BigInteger.class);
		
		String sql="INSERT INTO BATCHSTRN (BATCHRUNID,BATCHID,REQDATE,PROCSTARTDATE,STATUSCODE)"
				+ "VALUES (?, ?, ?, ?, 'S')";
		
		jdbcTemplate.update(sql,batch_run_id,batchId,getCurrentTimeStamp(),getCurrentTimeStamp());
		
		
		
		System.out.println("Execution Start .....!");
	
		generateFileList(new File(SOURCE_FOLDER));
		zipIt(OUTPUT_ZIP_FILE,id);
	 	      
		
		
		System.out.println("Execution End .....!");
		
		return RepeatStatus.FINISHED;

	}
/*
	public Resource getDirectory() {
		return directory;
	}

	public void setDirectory(Resource directory) {
		this.directory = directory;
	}
*/
	public void zipIt(String zipFile, BigInteger id){

	//System.out.println("id:::::::::->"+id);
	
		String sql = "INSERT INTO ZIP_DTL " +
				"(ZIP_ID,FILE_NAME, FILE_PATH) VALUES (?, ?, ?)";
	     byte[] buffer = new byte[1024];
	 
	     try{
	 
	    	FileOutputStream fos = new FileOutputStream(zipFile);
	    	ZipOutputStream zos = new ZipOutputStream(fos);
	 
	    	System.out.println("Output to Zip : " + zipFile);
	 
	    	int i =2;
	    	for(String file : this.fileList){
	    		
	    		System.out.println("File Added : " + file);
	    		
	    		//
	    		
	    		jdbcTemplate.update(sql,new Object[]{id,file,file});
	       
	    		i=i+1;
	        
	    	
	    		ZipEntry ze= new ZipEntry(file);
	        	zos.putNextEntry(ze);
	 
	        	FileInputStream in = 
	                       new FileInputStream(SOURCE_FOLDER + File.separator + file);
	 
	        	int len;
	        	while ((len = in.read(buffer)) > 0) {
	        		zos.write(buffer, 0, len);
	        	}
	 
	        	in.close();
	    	}
	 
	    	zos.closeEntry();
	    	//remember close it
	    	zos.close();
	 
	    	System.out.println("Done");
	    }catch(IOException ex){
	       ex.printStackTrace();   
	    }
	   }
	 
	    /**
	     * Traverse a directory and get all files,
	     * and add the file into fileList  
	     * @param node file or directory
	     */
	    public void generateFileList(File node){
	 
	    	//add file only
		if(node.isFile()){
			fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
		}
	 
		if(node.isDirectory()){
			String[] subNote = node.list();
			for(String filename : subNote){
				generateFileList(new File(node, filename));
			}
		}
	 
	    }
	 
	    /**
	     * Format the file path for zip
	     * @param file file path
	     * @return Formatted file path
	     */
	    private String generateZipEntry(String file){
	    	return file.substring(SOURCE_FOLDER.length()+1, file.length());
	    }

		@Override
		public void afterPropertiesSet() throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public ExitStatus afterStep(StepExecution stepExecution) {
			// TODO Auto-generated method stub
			
			stepExecution.getJobExecution().getExecutionContext()
			.put("BATCHRUNID", batch_run_id);
			
			return null;
		}

		@Override
		public void beforeStep(StepExecution arg0) {
			// TODO Auto-generated method stub
			
		}
		
		public static String getCurrentTimeStamp() {
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			return strDate;
		}
}