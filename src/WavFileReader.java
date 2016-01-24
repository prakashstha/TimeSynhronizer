

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WavFileReader {
	private String wavFileName;
	private int syncTimeMs;
	//private int noOfSamplesToRemove;
	private int bufferSize = 8000;
	short channels;
	short BPP;
	int sampleRate;
	
	
//	public WavFileReader(String fileName, int syncTimeMs){
//		this.wavFileName = fileName;
//		this.syncTimeMs = syncTimeMs;
//		noOfSamplesToRemove = (int) ((double)(syncTimeMs * Constants.SAMPLE_RATE)/1000 * 2);
//	}
	
	public WavFileReader(short channels, short BPP, int sampleRate){
		this.channels = channels;
		this.BPP = BPP;
		this.sampleRate = sampleRate;
	}

	/**
	 * 
	 * @param inFileName complete .wav audio file path of which audio part is to be removed
	 * @param outFileName audio file after removing audio portion between "startMilliSeconds" and "endMilliSeconds"
	 * @param startMilliSeconds start time in milliseconds to start the removal of audio samples
	 * @param endMilliSeconds end time in milliseconds to end the removal of audio samples.
	 * @return
	 */
	public List<Short> subAudio(String inFileName, String outFileName, int startMilliSeconds, int endMilliSeconds){
		//System.out.println("Start Milis: " + startMilliSeconds + "\n end milis: " + endMilliSeconds);
		//System.out.println("Sample rate: " + sampleRate + "\nchannels: " + channels);
		int startNoOfSamples = (int)((double)(startMilliSeconds * sampleRate)/1000 * channels);
		int endNoOfSamples = (int)((double)(endMilliSeconds * sampleRate)/1000 * channels);
		
		//System.out.println("start: " + startNoOfSamples + "\nEnd: " + endNoOfSamples );
		String tempFile = "abc.raw";
		File f = new File(tempFile);
		DataOutputStream dos = null;
		List<Short> audioData = new ArrayList<Short>();
		DataInputStream dis = null;
		int i = 0;
		short val;
		
		try {
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inFileName)));
			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			
			while(dis.available()>0){
				i++;
				val =  dis.readShort();
				if(endMilliSeconds > 0){ 
					if(i >= startNoOfSamples && i <= endNoOfSamples){
						continue;
					}
				}else if(endMilliSeconds <= 0){
					if(i >= startNoOfSamples){
						break;
					}
				}
				audioData.add(val);
				dos.writeShort(val);
				//System.out.println(audioData.get(i++));
			}
			dos.close();
			dis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		copyWaveFile(tempFile, outFileName);
		return audioData;
	}
	
	
	public List<Short> readFile(String audioFilePath){
		
		List<Short> audioData = new ArrayList<Short>();
		DataInputStream dis = null;
		int i = 0;
		short val;
		try {
			
			dis = new DataInputStream(new BufferedInputStream(new FileInputStream(audioFilePath)));
			
			while(dis.available()>0){
				i++;
				val =  dis.readShort();
				audioData.add(val);
			}
			dis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return audioData;
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
		 * Get the name of file given complete path of that file
		 * @param completePath complete path of the file
		 * @return only name of file only excluding file path
		 */
		public static String getFileNameOnly(String completePath){
			String str[] = completePath.split("/");
			return str[str.length-1];
			
		}
		/**
		 * convert the original file format to specified format if possible
		 * @param fromFile original file path
		 * @param toFile path of the file to be created after conversion
		 * @param toSampleRate sample rate of output audio file
		 * @param toChannels channels - mono or stereo of output audio file
		 * @param toFramSize frame size (- 2 for mono and 4 for stereo -) ,number of bytes per frame, of output audio file
		 * @return
		 * @throws UnsupportedAudioFileException
		 * @throws IOException
		 */
		public static void convertAudioFormat(String fromFile, String toFile, float toSampleRate, int toChannels, int toFramSize){
			System.out.print("File ==> " + getFileNameOnly(fromFile) + " === converting to ===");
			File file = new File(fromFile);//new File("C:\\Users\\prakashs\\Desktop\\web_audio_data.wav");
		    File output = new File(toFile);//new File("C:\\Users\\prakashs\\Desktop\\new.wav");

		    AudioInputStream ais;
		    AudioInputStream eightKhzInputStream = null;
		    try {
				ais = AudioSystem.getAudioInputStream(file);
				AudioFormat sourceFormat = ais.getFormat();
			    //System.out.println(ais.getFormat().getSampleRate());
			    AudioFileFormat sourceFileFormat = AudioSystem.getAudioFileFormat(file);
//			    System.out.println("Channel :" + sourceFormat.getChannels() + 
//		        		"sample size in bits" + sourceFormat.getSampleSizeInBits()+
//		        		"frame size" + sourceFormat.getFrameSize() + 
//		        		"frame rate" + sourceFormat.getFrameRate());
//		    
			    if (ais.getFormat().getSampleRate() == 44100f) {
			        AudioFileFormat.Type targetFileType = sourceFileFormat.getType();
			        AudioFormat targetFormat = new AudioFormat(
			                sourceFormat.getEncoding(),
			                toSampleRate,
			                sourceFormat.getSampleSizeInBits(),
			                toChannels,
			                toFramSize,
			                toSampleRate,
			                sourceFormat.isBigEndian());
			        if (!AudioSystem.isFileTypeSupported(targetFileType) || ! AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
			              throw new IllegalStateException("Conversion not supported!");
			        }
			        eightKhzInputStream = AudioSystem.getAudioInputStream(targetFormat, ais);
			        int nWrittenBytes = 0;

			      	nWrittenBytes = AudioSystem.write(eightKhzInputStream, targetFileType, output);
					//System.out.println("nWrittenBytes: " + nWrittenBytes);
			        
			    }
			} catch (UnsupportedAudioFileException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    System.out.println(" ===> " + getFileNameOnly(toFile));
				
		}
		
		
		
	
	public static void main(String[] args){
//		String inFilePath = "./dataset/syncTest/chrome/mac/1452900915404_test1_browser_audio.wav";
//		String outFilePath = "./dataset/syncTest/chrome/mac/1452900915404_test1_browser_audio_sync.wav";
	    WavFileReader wavReader = new WavFileReader(Short.parseShort("2"), Short.parseShort("16"), 44100);
//		List<Short> audioData = wavReader.subAudio(inFilePath, outFilePath, 5000, 0);
//	
	    String from = "/Users/Prakashs/Desktop/browser_audio.wav";
		String to = "/Users/Prakashs/Desktop/browser_audio_mono.wav";
		wavReader.convertAudioFormat(from, to, 22050f, 1, 2);
		//System.out.println("Audio Data: " + audioData.size());
	}
	
	public static boolean renameFile(String oldFile, String newFile){
		//rename file
		File file = new File(oldFile);
		File file2 = new File(newFile);
		return file.renameTo(file2);
		
	}
	public static boolean deleteFile(String file){
		//delete file
		System.out.println("\ndeleted file:==> " + file);
		File f = new File(file);
		return f.delete();
				

	}
	public static boolean copyFile(File source, File dest){
		
		try {
			if(dest.exists()){
				deleteFile(dest.getPath());
			}
			Files.copy(source.toPath(), dest.toPath());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
