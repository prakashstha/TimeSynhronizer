

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;


public class BrowserMobileWearSynchronizer {

	/**
	 * 
	 * @param bPath browser file path
	 * @param mPath mobile file path
	 */
	public BrowserMobileWearSynchronizer(String bPath, String mPath) {
		// TODO Auto-generated constructor stub
		this.browserPath = bPath;
		this.phonePath = mPath;
	}
	
	/**
	 * 
	 * @param mTimeInfoFilePath the path of audio time info file
	 * @return time stamp at which "ofDevice" starts recording audio
	 */
	private long getAudioStartTime(String mTimeInfoFilePath, String ofDevice) {
		// TODO Auto-generated method stub
		BufferedReader reader = getReader(mTimeInfoFilePath);
		long audioStartTime = -1;
		String line = "";
		try {
				if(ofDevice.equalsIgnoreCase("phone") || ofDevice.equalsIgnoreCase("wear")){
					while((line = reader.readLine())!=null){
						//System.out.println(line);
						String tokens[] = line.split(",");
						if(tokens[0].startsWith(ofDevice)){
							audioStartTime = Long.parseLong(tokens[1]);
							break;
						}
						
					}
				}else if(ofDevice.equalsIgnoreCase("browser")){
					//System.out.println("inside browser");
					line = reader.readLine();
					String tokens[] = line.split(",");
					audioStartTime = Long.parseLong(tokens[1]);
				}
				if(audioStartTime == -1) 
					audioStartTime = 0;
				reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(audioStartTime == -1){
			throw new IllegalStateException("Could not read phone time info...");
		}
		
		return audioStartTime;
		
	}
	
	/**
	 * 
	 * @return time-stamp at which phone start time syncing process
	 */
	public long getPhoneTimeSyncStartTime(String mTimeSyncFilePath){
		BufferedReader reader = getReader(mTimeSyncFilePath);
		long mTimeSyncStartTime = -1;
		String line = "";
		try {
				line = reader.readLine();
				String tokens[] = line.split(",");
				mTimeSyncStartTime = Long.parseLong(tokens[0]);
				if(mTimeSyncStartTime == -1) 
					mTimeSyncStartTime = 0;
				reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(mTimeSyncStartTime == -1){
			throw new IllegalStateException("Could not read phone time info...");
		}
		
		return mTimeSyncStartTime;
	}
	
	
	/**
	 * 
	 * @return time-offset among the devices 
	 * e.g.,a) browser and web server or b) phone and web server or c) wearable and phone
	 */
	public int getTimeOffset(String timeSyncFilePath){
		int timeOffset = -1;
		BufferedReader reader = getReader(timeSyncFilePath);

		//System.out.println("From file: " + bTimeSyncFilePath);
		String line = "";
		try {
			while((line = reader.readLine())!=null){
				String tokens[] = line.split(",");
				if(tokens.length>0){
					timeOffset = (int)Double.parseDouble(tokens[4]);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timeOffset;
	}

	
	/**
	 * 
	 * @param filePath complete file path
	 * @return reader for corresponding file type
	 */
	public BufferedReader getReader(String filePath){
		String endsWith = "";
		String dirPath = "";
		FileReader reader = null;
		BufferedReader bfrReader = null;
	    try {
			reader = new FileReader(filePath);
			bfrReader = new BufferedReader(reader);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   
		return bfrReader;
	}

	/**
	 * Method to synchronize the browser and phone audio recordings
	 * Remove the first samples from longer audio files
	 * @param bAudioFilePath complete browser audio file path
	 * @param bTimeInfoFilePath complete browser time into file path
	 * @param bTimeSyncFilePath complete browser time sync file path
	 * @param mAudioFilePath complete phone audio file path
	 * @param mTimeInfoFilePath complete phone time into file path
	 * @param mTimeSyncFilePath complete phone time sync file path
	 */
	public void synchronize(String bAudioFilePath,
						String bTimeInfoFilePath,
						String bTimeSyncFilePath,
						String mAudioFilePath,
						String mTimeInfoFilePath,
						String mTimeSyncFilePath,
						String wAudioFilePath,
						String wTimeSyncFilePath
						){
		//long phoneAudioStartTime = getPhoneTimeSyncStartTime(mTimeInfoFilePath);
		long phoneAudioStartTime = getAudioStartTime(mTimeInfoFilePath, "phone"); //(mTimeInfoFilePath);
		long wearAudioStartTime = getAudioStartTime(mTimeInfoFilePath, "wear");
		long browserAudioStartTime = getAudioStartTime(bTimeInfoFilePath, "browser");
		int phoneServerTimeOffset = getTimeOffset(mTimeSyncFilePath);
		int browserServerTimeOffset = getTimeOffset(bTimeSyncFilePath);
		int phoneWearTimeOffset = getTimeOffset(wTimeSyncFilePath);
		
		
		System.out.println("browser audio start time: "+browserAudioStartTime);
		System.out.println("phone audio start time: "+phoneAudioStartTime);
		System.out.println("wear audio start time: " + wearAudioStartTime);
		System.out.println("browser server time offset: "+browserServerTimeOffset);
		System.out.println("phone server time offset: "+ phoneServerTimeOffset);
		System.out.println("phone wear time offset: " + phoneWearTimeOffset);
		 
		//syncing with phone audio
		long syncPhoneWearStartTime = phoneAudioStartTime - phoneWearTimeOffset;
		int syncPhoneTimeMs = (int)(wearAudioStartTime - syncPhoneWearStartTime);
		System.out.println("Sync phone wear start time: " + syncPhoneWearStartTime);
		System.out.println("Sync wear  start time: " + wearAudioStartTime);
		System.out.println("Phone time to be sync: " + syncPhoneTimeMs);
		
		long syncPhoneServerStartTime = phoneAudioStartTime - phoneServerTimeOffset;
		long syncBrowserServerStartTime = browserAudioStartTime - browserServerTimeOffset;
		int syncServerTimeMs = (int)(syncPhoneServerStartTime - syncBrowserServerStartTime);
		System.out.println("Sync phone server start time: " + syncPhoneServerStartTime);
		System.out.println("Sync browser server start time:" +  syncBrowserServerStartTime);
		System.out.println("Time to be sync: " + syncServerTimeMs);
		System.out.println("combined time to be synced: " + (syncServerTimeMs + syncPhoneTimeMs));
		
	    
		
		
//		long syncPhoneAudioStartTime = phoneAudioStartTime - phoneServerTimeOffset;
//		long syncBrowserAudioStartTime = browserAudioStartTime - browserServerTimeOffset;
//	    int syncTimeMs = (int)(syncPhoneAudioStartTime - syncBrowserAudioStartTime);
//		System.out.println("Sync phone start time: " + syncPhoneAudioStartTime);
//		System.out.println("Sync browser start time:" +  syncBrowserAudioStartTime);
//		System.out.println("Time to be sync: " + syncTimeMs);
	    
		
		Short channels = 2;
		Short BPP = 16;
		int sampleRate = 44100;
		
		//synching phone audio with wear recordings
		String toBeSyncFilePath = mAudioFilePath;
	    if(syncPhoneTimeMs<0){
	    	//syncFilePath = mAudioFilePath;
	    	System.err.println("Synch Phone time < 0");
	    	System.exit(0);
	    }
	    
	    
	   WavFileReader wavReader = new WavFileReader(channels, BPP, sampleRate);
	   String mOutFilePath = toBeSyncFilePath.substring(0, toBeSyncFilePath.indexOf(".wav")) + "_sync.wav";
	   List<Short> phoneAudioList = wavReader.subAudio(toBeSyncFilePath, mOutFilePath, 0, syncPhoneTimeMs);
	  
	   toBeSyncFilePath = bAudioFilePath;
	    if(syncServerTimeMs < 0 || syncPhoneTimeMs < 0){
	    	//syncFilePath = mAudioFilePath;
	    	System.err.println("Synch server or phone time < 0");
	    	System.exit(0);
	    }
	    
	   String bOutFilePath = toBeSyncFilePath.substring(0, toBeSyncFilePath.indexOf(".wav")) + "_sync.wav";
	   List<Short> browserAudioList = wavReader.subAudio(toBeSyncFilePath, bOutFilePath, 0, (syncServerTimeMs + syncPhoneTimeMs));
	   
	   browserAudioList = wavReader.readFile(bOutFilePath);
	   phoneAudioList = wavReader.readFile(mOutFilePath);
	   List<Short> wearAudioList = wavReader.readFile(wAudioFilePath);
	   
	   //all length are in milliseconds
	   int bAudioLen = (browserAudioList.size() * 1000)/(channels*sampleRate);
	   int mAudioLen = (phoneAudioList.size()*1000)/(channels * sampleRate);
	   int wAudioLen = (wearAudioList.size() * 1000)/(1 * 22050);
	   int shortestLen = (bAudioLen < mAudioLen)?((bAudioLen<wAudioLen)?bAudioLen:wAudioLen):((mAudioLen<wAudioLen)?mAudioLen:wAudioLen);

	   System.out.println("Browser Length: " + bAudioLen + 
			   "\nPhone Length: " + mAudioLen + 
			   "\nWear Length: " + wAudioLen);
	   System.out.println("Shortest len : " + shortestLen);
	   
	   if(shortestLen != bAudioLen){
		   String outFile = bOutFilePath.substring(0, bOutFilePath.indexOf(".wav")) + "_l.wav";
		   wavReader.subAudio(bOutFilePath, outFile, shortestLen, 0);
	   }
	   if(shortestLen != mAudioLen){
		   String outFile = mOutFilePath.substring(0, bOutFilePath.indexOf(".wav")) + "_l.wav";
		   wavReader.subAudio(mOutFilePath, outFile, shortestLen, 0);
	   }
	   //channels and sample rate is different for wear audio
	   if(shortestLen != wAudioLen){
		   channels = 1;
		   sampleRate = 22050;
		   wavReader = new WavFileReader(Short.parseShort("1"), BPP, 22050);
		   String outFile = wAudioFilePath.substring(0, wAudioFilePath.indexOf(".wav")) + "_l.wav";
		   wavReader.subAudio(wAudioFilePath, outFile, shortestLen, 0);
	   }
	 
	 }
	
	
	
	/**
	 * method that list all the files
	 */
	public void listFilePaths(){
		bAudioFilePathList = new ArrayList<String>();
		bAudioTimeInfoFilePathList = new ArrayList<String>();
		bAudioTimeSyncFilePathList = new ArrayList<String>();
		mAudioFilePathList = new ArrayList<String>();
		mAudioTimeInfoFilePathList = new ArrayList<String>();
		mAudioTimeSyncFilePathList = new ArrayList<String>();
		
	    String bAudioFilePath, bAudioTimeSyncFilePath, bAudioTimeInfoFilePath;
		String mAudioFilePath, mAudioTimeSyncFilePath, mAudioTimeInfoFilePath;
	    File mbDir = new File(phonePath);
	    File bDir = new File(browserPath);
	    List<String> mListOfFileNames = new ArrayList();
	    List<String> bListOfFileNames = new ArrayList();
	    
	    String currentCSVFileName="";
	    String initialFileName = "";
	    int lengthOfTime = 13;
	    
	    if(bDir.isDirectory()){
	    	File[] bArrOfFiles = bDir.listFiles();
	    	for(File f: bArrOfFiles){
	    		bListOfFileNames.add(f.getName());
	    	}
	    }else{
	    	System.err.println("bDir is not dir:...");
	    }
	    	
	    
	    if(mbDir.isDirectory()){
	    	File[] mArrOfFiles = mbDir.listFiles();
	    	// create list of file names
	    	for(File f: mArrOfFiles){
	    		mListOfFileNames.add(f.getName());
	    	}
	    	
	    	for(File f:mArrOfFiles){
	    		currentCSVFileName = f.getName();
	    		/**
	    		 * selecting phone time sync file
	    		 */
	    		if(currentCSVFileName.endsWith("mobileTimeSync.csv")){
	    			
	    			//get the start time string of filename from the file
	    			initialFileName = currentCSVFileName.substring(0, lengthOfTime);
	    			//System.out.println("currentFile: " + currentCSVFileName + "\t initials: " + initialFileName);
	    			int endOfMid = currentCSVFileName.indexOf("mobileTimeSync.csv");
	    			
	    			// browser and mobile middle string
	    			String mMiddleString = currentCSVFileName.substring(lengthOfTime, endOfMid);
	    			String bMiddleString = currentCSVFileName.substring(lengthOfTime, endOfMid) + "_";
	    			
	    			// complete browser file paths
	    			bAudioFilePath  = browserPath + initialFileName + bMiddleString + Constants.BROWSER_AUDIO_FILE;
	    			bAudioTimeInfoFilePath =  browserPath + initialFileName + bMiddleString + Constants.BROWSER_AUDIO_TIME_FILE;
	    			bAudioTimeSyncFilePath = browserPath + initialFileName + bMiddleString + Constants.BROWSER_SERVER_TIME_SYNC_FILE;
	    			
	    			
	    			
	    			// complete mobile file paths
	    			mAudioFilePath  = phonePath + initialFileName + mMiddleString + Constants.PHONE_AUDIO_FILE;
	    			mAudioTimeInfoFilePath =  phonePath + initialFileName + mMiddleString + Constants.AUDIO_TIME_FILE;
	    			mAudioTimeSyncFilePath = phonePath + initialFileName + mMiddleString + Constants.PHONE_SERVER_TIME_SYNC_FILE;
	    			
	    			
	    			
	    			bAudioFilePathList.add(bAudioFilePath);
	    			bAudioTimeInfoFilePathList.add(bAudioTimeInfoFilePath);
	    			bAudioTimeSyncFilePathList.add(bAudioTimeSyncFilePath);
	    			mAudioFilePathList.add(mAudioFilePath);
	    			mAudioTimeInfoFilePathList.add(mAudioTimeInfoFilePath);
	    			mAudioTimeSyncFilePathList.add(mAudioTimeSyncFilePath);
	    			
	    		}
	    	}
	    }else{
	    	System.err.println("mDir is not dir:...");
	    }
	}
	/**
	 * @param browsserPath path of browser recordings
	 * @param phonePath path of phone recordings
	 */
	private String browserPath, phonePath;
    private static List<String> bAudioFilePathList, bAudioTimeSyncFilePathList, bAudioTimeInfoFilePathList;
    private static List<String> mAudioFilePathList, mAudioTimeSyncFilePathList, mAudioTimeInfoFilePathList;
	
	
	public static void main(String[] args){

		String brDirPath = "./dataset/syncTest/chrome/mac/";
		String mbDirPath = "./dataset/syncTest/chrome/phone/";
		String fileInitials = "1452900986653_test3";//"1452900950421_test2"; //"1452900915404_test1";
		String bAudioFilePath = brDirPath + fileInitials + "_browser_audio.wav";
		String bTimeInfoFilePath = brDirPath + fileInitials + "_browser_audio_time.csv";
		String bTimeSyncFilePath = brDirPath + fileInitials + "_browser_time_sync.csv";
		String mAudioFilePath = mbDirPath + fileInitials + File.separator + "phone_audio.wav";
		String mTimeInfoFilePath = mbDirPath + fileInitials + File.separator + "audio_time.csv";
		String mTimeSyncFilePath = mbDirPath + fileInitials + File.separator + "server_time_sync.csv"; 
		String wTimeSyncFilePath = mbDirPath + fileInitials + File.separator + "wear_time_sync.csv"; 
		String wAudioFilePath = mbDirPath + fileInitials + File.separator + "wear_audio.wav";
		BrowserMobileWearSynchronizer b = new BrowserMobileWearSynchronizer(brDirPath, mbDirPath);
		
		b.synchronize(bAudioFilePath, bTimeInfoFilePath, bTimeSyncFilePath, mAudioFilePath, mTimeInfoFilePath, mTimeSyncFilePath,wAudioFilePath, wTimeSyncFilePath);
		
		
		
//		/**
//		 * if it phone audio time info file is missing then do the following steps:
//		 * 1. Change the audio time info file in Constants.java file
//		 * 2. In synchronize() method used getSyncStartTime() as audio start time instead of getAudioStartTime()
//		 * 3. Find the average time diff between time sync start time and audio start time
//		 */
//		
////		String brDirPath = "C:/Users/prakashs/Desktop/time_sync_audio/mac/";
////	    String mbDirPath = "C:/Users/prakashs/Desktop/time_sync_audio/phone/";
//	    /*BrowserMobileTimeSynchronizer synchronizerAvg = new BrowserMobileTimeSynchronizer(brDirPath, mbDirPath);
//	    synchronizerAvg.listFilePaths();
//		int avgDiff = synchronizerAvg.avgTimeDiffBtwTimeSyncStartAndAudioStart();
//	    
////		String brDirPath = "D:/Dropbox/Project/2FA/audio analysis/audiofeatures/dataset/mac/";
////	    String mbDirPath = "D:/Dropbox/Project/2FA/audio analysis/audiofeatures/dataset/phone/";
//*/	    
//	    
//	    String brDirPath = "D:/babins_test_file/mac/";
//	    String mbDirPath = "D:/babins_test_file/phone/";
//	    BrowserMobileTimeSynchronizer synchronizer = new BrowserMobileTimeSynchronizer(brDirPath, mbDirPath);
//		synchronizer.listFilePaths();
//		//System.out.println(bAudioFilePathList.size());
//		//bAudioFilePathList.size()
//		for(int i = 0;i<5; i++){
//		
//			System.out.println("Browser Audio File Path : " + bAudioFilePathList.get(i));
//			System.out.println("Browser Time Info Path : " + bAudioTimeInfoFilePathList.get(i));
//			System.out.println("Browser Time Sync File Path : " + bAudioTimeSyncFilePathList.get(i));
//			System.out.println("Phone Audio File Path : " + mAudioFilePathList.get(i));
//			System.out.println("Phone Time Info Path : " + mAudioTimeInfoFilePathList.get(i) );
//			System.out.println("Phone Time Sync File Path : " + mAudioTimeSyncFilePathList.get(i));	
//			
////			synchronizer.synchronize(bAudioFilePathList.get(i), bAudioTimeInfoFilePathList.get(i), bAudioTimeSyncFilePathList.get(i), 
////					mAudioFilePathList.get(i), mAudioTimeInfoFilePathList.get(i), mAudioTimeSyncFilePathList.get(i));
//		
//			System.out.println("\n***** ========   ****\n");
//			if(i>5)
//				break;
//		}//System.out.println("Browser Start time" + synchronizer.getBrowserAudioTimeStart());
//		
	}
	
	public int avgTimeDiffBtwTimeSyncStartAndAudioStart(){
		int avg = 0;
		long sum = 0;
		for(int i = 0;i<mAudioTimeInfoFilePathList.size();i++){
//			System.out.println("Phone Time Info Path : " + mAudioTimeInfoFilePathList.get(i) );
//			System.out.println("Phone Time Sync File Path : " + mAudioTimeSyncFilePathList.get(i));	
			long phoneAudioStartTime = getAudioStartTime(mAudioTimeInfoFilePathList.get(i), "phone");
			long phoneTimeSyncStartTime = getPhoneTimeSyncStartTime(mAudioTimeSyncFilePathList.get(i));
//			System.out.println("Phone audio start time: " + phoneAudioStartTime);
//			System.out.println("Time Sync Start time: " +  phoneTimeSyncStartTime);
//			System.out.println("Difference: " +  (phoneTimeSyncStartTime - phoneAudioStartTime)  + "\n***** ========   ****\n");
			sum+=(phoneTimeSyncStartTime - phoneAudioStartTime);
		}
		avg = (int) sum/mAudioTimeInfoFilePathList.size();
		System.out.println("Avg: " + avg);
		return avg;
	}
	
	

	public static FileType getFileType(String fileName){
		if(fileName.endsWith(Constants.BROWSER_AUDIO_FILE)){
			return FileType.BROWSER_AUDIO;
		}else if(fileName.endsWith(Constants.BROWSER_AUDIO_TIME_FILE))
			return FileType.BROWSER_TIME_INFO;
		else if(fileName.endsWith(Constants.BROWSER_SERVER_TIME_SYNC_FILE)){
			return FileType.BROWSER_SERVER_TIME_SYNC;
		}else if(fileName.endsWith(Constants.PHONE_AUDIO_FILE)){
			return FileType.PHONE_AUDIO;
		}else if(fileName.endsWith(Constants.AUDIO_TIME_FILE)){
			return FileType.AUDIO_TIME_INFO;
		}else if(fileName.endsWith(Constants.WEAR_AUDIO_FILE)){
			return FileType.WEAR_AUDIO;
		}else if(fileName.endsWith(Constants.PHONE_WEAR_TIME_SYNC_FILE)){
			return FileType.PHONE_WEAR_TIME_SYNC;
		}else if(fileName.endsWith(Constants.PHONE_SERVER_TIME_SYNC_FILE)){
			return FileType.PHONE_SERVER_TIME_SYNC;
		}
		
		return null;
		
	}

}
