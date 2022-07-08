import java.io.IOException;
import java.util.Map;


public class mainUI{
	
	public static void main(String[] args) throws IOException {

		String input_filename = "test.txt";
		int level=5;
		boolean reset = false;
		String output_filename = "encode_output.txt";
		String decode_output = "decode_output.txt";
		
		
		FileCompressorClass obj = new FileCompressorClass();
		boolean encodeStatus = obj.encode(input_filename, level, reset, output_filename);
		System.out.println("Encode status: " + encodeStatus);
		
		boolean decodeStatus = obj.decode(output_filename, decode_output);
		System.out.println("Decode status: " + decodeStatus);
		
		Map<Character, String> codeBook = obj.codebook();
		System.out.print("Codebook output: " + codeBook);
	}
}
