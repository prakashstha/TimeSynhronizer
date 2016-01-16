

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


public class BrowserMobileTimeSynchronizer {

	
	int bufferSize = 8000;
	short channels = 2;
	short BPP = 16;
	int sampleRate = 44100;
	/**
	 * 
	 * @param bPath browser file path
	 * @param mPath mobile file path
	 */
	public BrowserMobileTimeSynchronizer(String bPath, String mPath) {
		// TODO Auto-generated constructor stub
		this.browserPath = bPath;
		this.phonePath = mPath;
	}
	
	/**
	 * 
	 * @param mTimeInfoFilePath the path of audio time info file
	 * @return time stamp at which phone starts recording audio
	 */
	private long getPhoneAudioTimeStart(String mTimeInfoFilePath) {
		// TODO Auto-generated method stub
		BufferedReader reader = getReader(mTimeInfoFilePath);
		long phoneAudioStartTime = -1;
		String line = "";
		try {
				line = reader.readLine();
				String tokens[] = line.split(",");
				phoneAudioStartTime = Long.parseLong(tokens[1]);
				if(phoneAudioStartTime == -1) 
					phoneAudioStartTime = 0;
				reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(phoneAudioStartTime == -1){
			throw new IllegalStateException("Could not read phone time info...");
		}
		
		return phoneAudioStartTime;
		
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
	 * @return time-stamp at which browser starts recording audio
	 */
	public long getBrowserAudioTimeStart(String bTimeInfoFilePath){
		BufferedReader reader = getReader(bTimeInfoFilePath);
		long browserAudioStartTime = -1;
		String line = "";
		try {
				line = reader.readLine();
				String tokens[] = line.split(",");
				browserAudioStartTime = Long.parseLong(tokens[1]);
				if(browserAudioStartTime == -1) 
					browserAudioStartTime = 0;
				reader.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(browserAudioStartTime == -1){
			throw new IllegalStateException("Could not read phone time info...");
		}
		return browserAudioStartTime;
	}
	
	/**
	 * 
	 * @return time-offset between the phone and web-server
	 */
	public int getPhoneServerTimeOffset(String mTimeSyncFilePath){
		int timeOffset = -1;
		BufferedReader reader = getReader(mTimeSyncFilePath);

		//System.out.println("From file: " + mTimeSyncFilePath);
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
	 * @return time-offset between the browser and web-server
	 */
	public int getBrowserServerTimeOffset(String bTimeSyncFilePath){
		int timeOffset = -1;
		BufferedReader reader = getReader(bTimeSyncFilePath);

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
						String mTimeSyncFilePath
						){
		//long phoneAudioStartTime = getPhoneTimeSyncStartTime(mTimeInfoFilePath);
		long phoneAudioStartTime = getPhoneAudioTimeStart(mTimeInfoFilePath);
		long browserAudioStartTime = getBrowserAudioTimeStart(bTimeInfoFilePath);
		int phoneServerTimeOffset = getPhoneServerTimeOffset(mTimeSyncFilePath);
		int browserServerTimeOffset = getBrowserServerTimeOffset(bTimeSyncFilePath);
		System.out.println("phone audio start time: "+phoneAudioStartTime);
		System.out.println("phone time offset: "+phoneServerTimeOffset);
		System.out.println("browser audio start time: "+browserAudioStartTime);
		System.out.println("browser time offset: "+browserServerTimeOffset);
	
		long syncPhoneAudioStartTime = phoneAudioStartTime - phoneServerTimeOffset;
		long syncBrowserAudioStartTime = browserAudioStartTime - browserServerTimeOffset;
		int syncTimeMs = (int)(syncPhoneAudioStartTime - syncBrowserAudioStartTime);
		System.out.println("Sync phone start time: " + syncPhoneAudioStartTime);
		System.out.println("Sync browser start time:" +  syncBrowserAudioStartTime);
		System.out.println("Time to be sync: " + syncTimeMs);
	    String syncFilePath = bAudioFilePath;
	    if(syncTimeMs<0){
	    	syncFilePath = mAudioFilePath;
	    }
//	   WavFileReader wavReader = new WavFileReader(syncFilePath, syncTimeMs);
//	   wavReader.readFile();
//	   String outFile = syncFilePath.substring(0, syncFilePath.indexOf(".wav")) + "_sync.wav";
//	    
//	   copyWaveFile("abc.raw", outFile);
		
		
	   
		
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
                    sampleRate, channels, BPP);

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
	        System.out.println("Byte Rate: " + bytesRate);
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

		
		String inFilePath = "./dataset/syncTest/mac/1452891021503_test1_browser_audio.wav";
	    String outFilePath = "./dataset/syncTest/mac/1452891021503_test1_browser_audio_copy.wav";
	    String bPath = "hello", mPath = "Hello";
	   BrowserMobileTimeSynchronizer b = new BrowserMobileTimeSynchronizer(bPath, mPath);
	   b.copyWaveFile(inFilePath, outFilePath);
	    
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
			long phoneAudioStartTime = getPhoneAudioTimeStart(mAudioTimeInfoFilePathList.get(i));
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
