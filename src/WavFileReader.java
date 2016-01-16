

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
import java.util.ArrayList;
import java.util.List;

public class WavFileReader {
	private String wavFileName;
	private int syncTimeMs;
	private int bufferSize = 8000;
	private short channels;
	private short BPP;
	private int sampleRate;
	
	/**
	 * 
	 * @param channels channels in audio
	 * @param BPP bits per point
	 * @param sampleRate sample rate of audio
	 */
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
	 * 			if endMillisSeconds is <= zero, then this method will remove "startMilliSeconds" audio from end of the audio
	 * @return
	 */
	public List<Short> subAudio(String inFileName, String outFileName, int startMilliSeconds, int endMilliSeconds){
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
				audioData.add(val);
				/*check if audio has to be removed from the selected portion or from the end of audio file*/
				if(endMilliSeconds > 0){ 
					if(i >= startNoOfSamples && i <= endNoOfSamples){
						continue;
					}
				}else if(endMilliSeconds <= 0){  //remove audio from end of of the file
					if(i >= startNoOfSamples){
						break;
					}
				}
				dos.writeShort(val);
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
		//create .wav file from .raw file generated
		copyWaveFile(tempFile, outFileName);
		return audioData;
	}
	
	/**
	 * this method read .wav file and return content of the file in the form of list of short integers
	 * @param audioFilePath complete path of audio file to be read
	 * @return list of short integers fetched from the audio file
	 */
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

	
	public static void main(String[] args){
		String inFilePath = "./dataset/syncTest/chrome/mac/1452900915404_test1_browser_audio.wav";
		String outFilePath = "./dataset/syncTest/chrome/mac/1452900915404_test1_browser_audio_sync.wav";
	    WavFileReader wavReader = new WavFileReader(Short.parseShort("2"), Short.parseShort("16"), 44100);
		List<Short> audioData = wavReader.subAudio(inFilePath, outFilePath, 5000, 0);
		//System.out.println("Audio Data: " + audioData.size());
	}

}
