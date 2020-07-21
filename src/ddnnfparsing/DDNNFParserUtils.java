package ddnnfparsing;

public class DDNNFParserUtils {
	
	public static final String AND = "A";
	
	public static final String OR = "O";
	
	public static final String LITERAL = "L";
	
	public static final String NEGATIVE = "-";
	
	public static final String NNF = "nnf";

	
	public static boolean isDecision(String[] split) {
		return !split[1].equals("0");
	}
	
	public static boolean isLiteral(String[] split) {
		return split[0].equals(LITERAL);
	}
	
	public static boolean isEntry(String[] split) {
		return split[0].equals(NNF);
	}
	
	public static boolean isAnd(String[] split) {
		return split[0].equals(AND);
	}
	
	public static boolean isOr(String[] split) {
		return split[0].equals(OR);
	}
	
	public static boolean isNegativeLiteral(String[] split) {
		return split[1].startsWith(NEGATIVE);
	}
	
	public static String getNegativeLiteralName(String[] split) {
		return split[1].substring(1, split[1].length());
	}
	
	public static int getNegativeLiteral(String[] split) {
		return Integer.valueOf(getNegativeLiteralName(split));
	}
	
	public static String getPositiveLiteralName(String[] split) {
		return split[1];
	}
	
	public static int getPositiveLiteral(String[] split) {
		return Integer.valueOf(getPositiveLiteralName(split));
	}
	
	public static int getLiteral(String[] split) {
		return Integer.valueOf(split[1]);
	}
	
	public static int[] getAndChildIndices(String[] split) {
		int[] literals = new int[split.length - 2];
		// Skip label and number of children
		for (int i = 2; i < split.length; i++) {
			literals[i - 2] = Integer.parseInt(split[i]);
		}
		return literals;
	}
	
	public static int[] getOrChildIndices(String[] split) {
		int[] literals = new int[split.length - 3];
		for (int i = 3; i < split.length; i++) {
			literals[i - 3] = Integer.parseInt(split[i]);
		}
		
		return literals;
	}
	
}
