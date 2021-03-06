package com.skytonia.SkyCore.util;

import java.util.regex.Pattern;

/**
 * Created by Chris Brown (OhBlihv) on 3/31/2017.
 */
public class TextUtil
{
	
	public enum MinecraftFontInfo
	{
		
		A('A', 5),
		a('a', 5),
		B('B', 5),
		b('b', 5),
		C('C', 5),
		c('c', 5),
		D('D', 5),
		d('d', 5),
		E('E', 5),
		e('e', 5),
		F('F', 5),
		f('f', 4),
		G('G', 5),
		g('g', 5),
		H('H', 5),
		h('h', 5),
		I('I', 3),
		i('i', 1),
		J('J', 5),
		j('j', 5),
		K('K', 5),
		k('k', 4),
		L('L', 5),
		l('l', 1),
		M('M', 5),
		m('m', 5),
		N('N', 5),
		n('n', 5),
		O('O', 5),
		o('o', 5),
		P('P', 5),
		p('p', 5),
		Q('Q', 5),
		q('q', 5),
		R('R', 5),
		r('r', 5),
		S('S', 5),
		s('s', 5),
		T('T', 5),
		t('t', 4),
		U('U', 5),
		u('u', 5),
		V('V', 5),
		v('v', 5),
		W('W', 5),
		w('w', 5),
		X('X', 5),
		x('x', 5),
		Y('Y', 5),
		y('y', 5),
		Z('Z', 5),
		z('z', 5),
		NUM_1('1', 5),
		NUM_2('2', 5),
		NUM_3('3', 5),
		NUM_4('4', 5),
		NUM_5('5', 5),
		NUM_6('6', 5),
		NUM_7('7', 5),
		NUM_8('8', 5),
		NUM_9('9', 5),
		NUM_0('0', 5),
		EXCLAMATION_POINT('!', 1),
		AT_SYMBOL('@', 6),
		NUM_SIGN('#', 5),
		DOLLAR_SIGN('$', 5),
		PERCENT('%', 5),
		UP_ARROW('^', 5),
		AMPERSAND('&', 5),
		ASTERISK('*', 5),
		LEFT_PARENTHESIS('(', 4),
		RIGHT_PERENTHESIS(')', 4),
		MINUS('-', 5),
		UNDERSCORE('_', 5),
		PLUS_SIGN('+', 5),
		EQUALS_SIGN('=', 5),
		LEFT_CURL_BRACE('{', 4),
		RIGHT_CURL_BRACE('}', 4),
		LEFT_BRACKET('[', 3),
		RIGHT_BRACKET(']', 3),
		COLON(':', 1),
		SEMI_COLON(';', 1),
		DOUBLE_QUOTE('"', 3),
		SINGLE_QUOTE('\'', 1),
		LEFT_ARROW('<', 4),
		RIGHT_ARROW('>', 4),
		QUESTION_MARK('?', 5),
		SLASH('/', 5),
		BACK_SLASH('\\', 5),
		LINE('|', 1),
		TILDE('~', 5),
		TICK('`', 2),
		PERIOD('.', 1),
		COMMA(',', 1),
		SPACE(' ', 3),
		DEFAULT('a', 4),
		THING('▬', 5);
		
		private char character;
		private int length;
		
		MinecraftFontInfo(char character, int length)
		{
			this.character = character;
			this.length = length;
		}
		
		public char getCharacter()
		{
			return this.character;
		}
		
		public int getLength()
		{
			return this.length;
		}
		
		public int getBoldLength()
		{
			return this == SPACE ? this.getLength() : this.length + 1;
		}
		
		public static MinecraftFontInfo getDefaultFontInfo(char c)
		{
			for(MinecraftFontInfo fontInfo : values())
			{
				if(fontInfo.getCharacter() == c)
				{
					return fontInfo;
				}
			}
			
			return DEFAULT;
		}
		
	}

	public static int getLineLength(String line)
	{
		int lineLength = 0;
		for(char character : line.toCharArray())
		{
			lineLength += MinecraftFontInfo.getDefaultFontInfo(character).getLength();
		}

		return lineLength;
	}
	
	private static final int CENTER_PX = 154;
	
	//Does not check for BOLD
	private static final Pattern COLOUR_CODE_PATTERN = Pattern.compile("[&|§][a-km-rA-KM-R0-9]");
	
	public static String center(final String message)
	{
		//messaging = ChatColor.translateAlternateColorCodes('&', messaging);
		String strippedMessage = COLOUR_CODE_PATTERN.matcher(message).replaceAll("");
		
		int messagePxSize = 0;
		boolean previousCode = false;
		boolean isBold = false;
		char[] var4 = strippedMessage.toCharArray();
		int toCompensate = var4.length;
		
		int spaceLength;
		for(spaceLength = 0; spaceLength < toCompensate; ++spaceLength)
		{
			char c = var4[spaceLength];
			if(c == 167)
			{
				previousCode = true;
			}
			else if(!previousCode)
			{
				MinecraftFontInfo dFI = MinecraftFontInfo.getDefaultFontInfo(c);
				messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
				++messagePxSize;
			}
			else
			{
				previousCode = false;
				isBold = c == 108 || c == 76;
			}
		}
		
		int halvedMessageSize = messagePxSize / 2;
		toCompensate = 154 - halvedMessageSize;
		spaceLength = MinecraftFontInfo.SPACE.getLength() + 1;
		int compensated = 0;
		
		StringBuilder sb;
		for(sb = new StringBuilder(); compensated < toCompensate; compensated += spaceLength)
		{
			sb.append(" ");
		}
		
		return sb.toString() + message;
	}
	
}
