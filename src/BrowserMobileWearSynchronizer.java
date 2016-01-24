

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.musicg.wave.WaveFileManager;


public class BrowserMobileWearSynchronizer {

	
	int bufferSize = 8000;
	short channels = 2;
	short BPP = 16;
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
	 * @return time stamp at which phone starts recording audio
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
		
		
//		System.out.println("browser audio start time: "+browserAudioStartTime);
//		System.out.println("phone audio start time: "+phoneAudioStartTime);
//		System.out.println("wear audio start time: " + wearAudioStartTime);
//		System.out.println("browser server time offset: "+browserServerTimeOffset);
//		System.out.println("phone server time offset: "+ phoneServerTimeOffset);
//		System.out.println("phone wear time offset: " + phoneWearTimeOffset);
		 
//		//synching browser audio start time
//		long syncBrowserAudioStartTime = browserAudioStartTime - browserServerTimeOffset;
//		long syncPhoneAudioStartTime = phoneAudioStartTime - phoneServerTimeOffset;
//		//wear audio start time in terms of phone clock = wearAudioStartTime + phoneWearTimeOffset;
//		long syncWearAudioStartTime = (wearAudioStartTime + phoneWearTimeOffset) - phoneServerTimeOffset;
//		
//		System.out.println("Browser start time: " + syncBrowserAudioStartTime + 
//				"\nPhone start time: " + syncPhoneAudioStartTime +
//				"\nWear start time: " + syncWearAudioStartTime);
//		System.out.println("\n\nWear time to be sync: " + (syncBrowserAudioStartTime - syncWearAudioStartTime) + 
//				"\nPhone time to be sync: " + (syncBrowserAudioStartTime - syncPhoneAudioStartTime));
//		
		
		
		/**
		 * Consideration: phone recording starts first, then wear recording and then
		 * browser audio recording
		 * Client/Browser application hit start button to trigger recording in phone and watch 
		 * Phone starts recording and trigger wear app to start recording
		 * In Client/Browser, when user enter password, it plays audio in background and also starts recordings
		 */
		//syncing wear with phone audio
		long syncPhoneWearStartTime = phoneAudioStartTime - phoneWearTimeOffset;
		//syncing phone with respect to wear audio
		int syncPhoneTimeMs = (int)(wearAudioStartTime - syncPhoneWearStartTime);
//		System.out.println("Sync phone wear start time: " + syncPhoneWearStartTime);
//		System.out.println("Sync wear  start time: " + wearAudioStartTime);
		
		//syncing phone and wear with browser audio
		long syncPhoneServerStartTime = phoneAudioStartTime - phoneServerTimeOffset;
		long syncBrowserServerStartTime = browserAudioStartTime - browserServerTimeOffset;
		int syncPhoneBrowserTimeMs = (int)(syncBrowserServerStartTime - syncPhoneServerStartTime);
		int syncWearTimeMs = syncPhoneBrowserTimeMs;
		//syncing phone with respect to phone as well as browser.
		syncPhoneTimeMs = syncPhoneTimeMs + syncPhoneBrowserTimeMs;
//		System.out.println("Sync phone server start time: " + syncPhoneServerStartTime);
//		System.out.println("Sync browser server start time:" +  syncBrowserServerStartTime);
		
		//System.out.println("Phone audio to be sync for wear and phone synchronization: " + syncPhoneTimeMs);
		System.out.println("Wear time lab to be sync: " + syncWearTimeMs);
		System.out.println("Phone time lag to be sync: " + syncPhoneTimeMs);
	    
		
		
//		long syncPhoneAudioStartTime = phoneAudioStartTime - phoneServerTimeOffset;
//		long syncBrowserAudioStartTime = browserAudioStartTime - browserServerTimeOffset;
//	    int syncTimeMs = (int)(syncPhoneAudioStartTime - syncBrowserAudioStartTime);
//		System.out.println("Sync phone start time: " + syncPhoneAudioStartTime);
//		System.out.println("Sync browser start time:" +  syncBrowserAudioStartTime);
//		System.out.println("Browser time to be sync: " + syncTimeMs);
	    
		
		Short channels = 1;
		Short BPP = 16;
		int sampleRate = 22050;
		
		
		if(syncPhoneTimeMs<0){
	    	//syncFilePath = mAudioFilePath;
	    	System.err.println("Synch Phone time < 0");
	    	System.exit(0);
	    }
	    if(syncWearTimeMs<0){
	    	System.err.println("Sync phone wear time < 0");
	    	System.exit(0);
	    }
	    
	  /* synching phone audio*/
	   String toBeSyncFilePath = mAudioFilePath;
	   WavFileReader wavReader = new WavFileReader(channels, BPP, sampleRate);
	   String mOutFilePath = toBeSyncFilePath.substring(0, toBeSyncFilePath.indexOf(".wav")) + "_sync.wav";
	   List<Short> phoneAudioList = wavReader.subAudio(toBeSyncFilePath, mOutFilePath, 0, syncPhoneTimeMs);
	   List<Short> browserAudioList = wavReader.readFile(bAudioFilePath);
	   
	   /* synching wear audio*/
	   toBeSyncFilePath = wAudioFilePath;
	   String wOutFilePath = toBeSyncFilePath.substring(0, toBeSyncFilePath.indexOf(".wav")) + "_sync.wav";
	   List<Short> wearAudioList = wavReader.subAudio(toBeSyncFilePath, wOutFilePath, 0, syncWearTimeMs);
	   
	   //all length are in milliseconds
	   int bAudioLen = (browserAudioList.size() * 1000)/(channels*sampleRate);
	   int mAudioLen = (phoneAudioList.size()*1000)/(channels * sampleRate);
	   int wAudioLen = (wearAudioList.size() * 1000)/(1 * 22050);
	   int shortestLen = (bAudioLen < mAudioLen)?((bAudioLen<wAudioLen)?bAudioLen:wAudioLen):((mAudioLen<wAudioLen)?mAudioLen:wAudioLen);

//	   System.out.println("Browser Length: " + bAudioLen + 
//			   "\nPhone Length: " + mAudioLen + 
//			   "\nWear Length: " + wAudioLen);
//	   System.out.println("Shortest len : " + shortestLen);
	
	   String mOutFile = mOutFilePath.substring(0, mOutFilePath.indexOf(".wav")) + "_l.wav";
	   wavReader.subAudio(mOutFilePath, mOutFile, shortestLen, 0);
	   WavFileReader.deleteFile(mOutFilePath);
	   WavFileReader.renameFile(mOutFile, mOutFilePath);
	   
	   String bOutFile = bAudioFilePath.substring(0, bAudioFilePath.indexOf(".wav")) + "_sync.wav";
	   wavReader.subAudio(bAudioFilePath, bOutFile, shortestLen, 0);
//	   WavFileReader.deleteFile(bAudioFilePath);
//	   WavFileReader.renameFile(bOutFile, bAudioFilePath);
	   
//	   wavReader = new WavFileReader(new Short("1"), BPP, 22050);
	   String wOutFile = wOutFilePath.substring(0, wOutFilePath.indexOf(".wav")) + "_l.wav";
	   wavReader.subAudio(wOutFilePath, wOutFile, shortestLen, 0);
	   WavFileReader.deleteFile(wOutFilePath);
	   WavFileReader.renameFile(wOutFile, wOutFilePath);
	   
	   
	   
	   
	 }
	
	 public String removeSyncFromWavFileName(String filename){
		 String str[] = filename.split("_");
		 String outFileName = "";
		 for(int i = 0;i < (str.length - 1); i++){
			 if(i == 0)
				 outFileName += str[i];
			 else
				 outFileName += "_" + str[i];
		 }
		 outFileName += ".wav";
		 //System.out.println("\noutfile anem: " + outFileName);
		 return outFileName;
	 }
	
	/**
	 * This method creates .wav file from .raw file
	 * @param inFilename complete path of raw audio file
	 * @param outFilename complete path of .wav audio file to be created
	 */
	private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        int intTotalAudioLen = 0;

        int totalDataLen = intTotalAudioLen + 44;
        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);

            totalAudioLen = in.getChannel().size();
            if((totalAudioLen + 44) >Integer.MAX_VALUE){
                throw new RuntimeException("file size is greate that expected");
            }
            intTotalAudioLen = (int)totalAudioLen;
            totalDataLen = intTotalAudioLen + 44;

           
            writeWaveFileHeader(out, intTotalAudioLen, totalDataLen,
                    Constants.SAMPLE_RATE, channels, BPP);

            while(in.read(data) != -1){
            	out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
      //  statusText.setText("Finished recordings");
    }
	    
	/**
	 * write header file in .wav file
	 * @param out outputstream of .wav file
	 * @param totalAudioLen total audio length in bytes excluding header
	 * @param totalDataLen  total data lenght in bytes including header
	 * @param sampleRate samplerate of audio recordings
	 * @param noOfChannels number of audio channels
	 * @param BPP 
	 * @throws IOException
	 */
		public void writeWaveFileHeader(FileOutputStream out,
	                                    int totalAudioLen,
	                                    int totalDataLen,
	                                    int sampleRate,
	                                    short noOfChannels,
	                                    short BPP) throws IOException {

	        String riffHeader = "RIFF";
	        String waveHeader = "WAVE";
	        String fmtHeader = "fmt ";
	        String data = "data";

	        //short BPP = 16; //bit per sample
	        //short numOfChannels = 2;//2 for stereo
	        //int totalDataLen = 999999;
	        //int totalAudioLen = 99966699;
	        //int sampleRate =44100;

	        int lengthOfFormat = 16;
	        short typeOfFormat = 1; //1 for PCM
	        int bytesRate = sampleRate * BPP * noOfChannels/8;
	        //System.out.println("Byte Rate: " + bytesRate);
	        short totalBytesPerSample =  (short) ((short)(BPP * noOfChannels)/8);

	        int allocSize = 44; //default header size
	        /**
	         * riffHeader.getBytes().length + waveHeader.getBytes().length + fmtHeader.getBytes().length + data.getBytes().length + INT_SIZE*5 + SHORT_SIZE*4;
	         */
	        ByteBuffer headerBuffer = ByteBuffer.allocate(allocSize);

	        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

			 /* RIFF (4 bytes) */
	        headerBuffer.put(riffHeader.getBytes());
			 /* File Size (4 bytes) */
	        headerBuffer.putInt(totalDataLen);
			 /* WAVE (4 byte) */
	        headerBuffer.put(waveHeader.getBytes());
			 /* fmt (4 bytes) */
	        headerBuffer.put(fmtHeader.getBytes());
			 /* Length of format data as listed above (4 bytes) */
	        headerBuffer.putInt(lengthOfFormat);
			 /* Type of format (1 for PCM) 2 bytes */
	        headerBuffer.putShort(typeOfFormat);
			 /*Number of channels (2 bytes)*/
	        headerBuffer.putShort(noOfChannels);
			 /*Sample Rate (4 bytes)*/
	        headerBuffer.putInt(sampleRate);
			 /*number of bytes in 1 seconds (4 bytes)*/
	        headerBuffer.putInt(bytesRate);
			 /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
	        headerBuffer.putShort(totalBytesPerSample);
			 /*Bits per sample (2 bytes)*/
	        headerBuffer.putShort(BPP);
			 /*data (4 bytes)*/
	        headerBuffer.put(data.getBytes());
			 /*File size (4 bytes)*/
	        headerBuffer.putInt(totalAudioLen);

	        out.write(headerBuffer.array());

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
	    			bAudioFilePath  = browserPath + initialFileName + bMiddleString + Constants.BROWSER_AUDIO_FILE_NAME;
	    			bAudioTimeInfoFilePath =  browserPath + initialFileName + bMiddleString + Constants.BROWSER_AUDIO_TIME_FILE;
	    			bAudioTimeSyncFilePath = browserPath + initialFileName + bMiddleString + Constants.BROWSER_TIME_SYNC_FILE_NAME;
	    			
	    			
	    			
	    			// complete mobile file paths
	    			mAudioFilePath  = phonePath + initialFileName + mMiddleString + Constants.PHONE_AUDIO_FILE_NAME;
	    			mAudioTimeInfoFilePath =  phonePath + initialFileName + mMiddleString + Constants.PHONE_AUDIO_TIME_FILE;
	    			mAudioTimeSyncFilePath = phonePath + initialFileName + mMiddleString + Constants.PHONE_TIME_SYNC_FILE;
	    			
	    			
	    			
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
		String fileInitials = "1453645308313_roomTest_amazing";//"1452900950421_test2"; //"1452900915404_test1";
		
		String bAudioFilePath = brDirPath + fileInitials + "_browser_audio.wav";
		String bTimeInfoFilePath = brDirPath + fileInitials + "_browser_audio_time.csv";
		String bTimeSyncFilePath = brDirPath + fileInitials + "_browser_time_sync.csv";
		String mAudioFilePath = mbDirPath + fileInitials + File.separator + "phone_audio.wav";
		String mTimeInfoFilePath = mbDirPath + fileInitials + File.separator + "audio_time.csv";
		String mTimeSyncFilePath = mbDirPath + fileInitials + File.separator + "server_time_sync.csv"; 
		String wTimeSyncFilePath = mbDirPath + fileInitials + File.separator + "wear_time_sync.csv"; 
		String wAudioFilePath = mbDirPath + fileInitials + File.separator + "wear_audio.wav";
		BrowserMobileWearSynchronizer b = new BrowserMobileWearSynchronizer(brDirPath, mbDirPath);
		
		
		String dirPath = "./dataset/syncTest/chrome/synched";
		File synchedFolder = new File(dirPath);
		if(!synchedFolder.exists()){
			synchedFolder.mkdirs();
		}
		File newFilePath = new File(dirPath + File.separator + fileInitials + "_browser_audio.wav");
		WavFileReader.copyFile(new File(bAudioFilePath), newFilePath);
		bAudioFilePath = newFilePath.getPath();
		
		newFilePath = new File(dirPath + File.separator + fileInitials + "_phone_audio.wav");
		WavFileReader.copyFile(new File(mAudioFilePath), newFilePath);
		mAudioFilePath = newFilePath.getPath();
		
		newFilePath = new File(dirPath + File.separator + fileInitials + "_wear_audio.wav");
		WavFileReader.copyFile(new File(wAudioFilePath), newFilePath);
		wAudioFilePath = newFilePath.getPath();
		
//		System.out.println("Phone audio: " + mAudioFilePath + 
//				"\n wear audio: " + wAudioFilePath+
//				"\nbrowser audio: " + bAudioFilePath );
		//converstion to lower sampling rate 22050 with mono channels
		String toFile = bAudioFilePath.substring(0, bAudioFilePath.indexOf(".wav")) + "_smpl.wav";
		WavFileReader.convertAudioFormat(bAudioFilePath, toFile, 22050, 1, 2);
		
		
		toFile = mAudioFilePath.substring(0, mAudioFilePath.indexOf(".wav")) + "_smpl.wav";
		WavFileReader.convertAudioFormat(mAudioFilePath, toFile, 22050, 1, 2);
		
		b.synchronize(bAudioFilePath, bTimeInfoFilePath, bTimeSyncFilePath, mAudioFilePath, mTimeInfoFilePath, mTimeSyncFilePath,wAudioFilePath, wTimeSyncFilePath);
		
		//removing unnessary files from synched folder
		if(synchedFolder.isDirectory()){
			System.out.println("\nRemoving unnessary files from synched directory...");
			System.out.println("*******************************************************");
			File[] fileList = synchedFolder.listFiles();
			for(File f: fileList){
				if(!f.getName().contains("sync")){
					WavFileReader.deleteFile(f.getPath());
				}
			}
		}
		
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
		//System.out.println("Avg: " + avg);
		return avg;
	}
	
	

	public static FileType getFileType(String fileName){
		if(fileName.endsWith(Constants.BROWSER_AUDIO_TIME_FILE))
			return FileType.BROWSER_TIME_INFO;
		else if(fileName.endsWith(Constants.BROWSER_TIME_SYNC_FILE_NAME)){
			return FileType.BROWSER_TIME_SYNC;
		}else if(fileName.endsWith(Constants.BROWSER_AUDIO_FILE_NAME)){
			return FileType.BROWSER_AUDIO;
		}else if(fileName.endsWith(Constants.PHONE_AUDIO_TIME_FILE)){
			return FileType.PHONE_TIME_INFO;
		}else if(fileName.endsWith(Constants.PHONE_TIME_SYNC_FILE)){
			return FileType.PHONE_TIME_SYNC;
		}else if(fileName.endsWith(Constants.PHONE_AUDIO_FILE_NAME)){
			return FileType.PHONE_AUDIO;
		}
		return null;
		
	}

}
