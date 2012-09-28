package ogdl;


import org.junit.*;
import static org.junit.Assert.*;
import ogdl.support.*;

public class TestCharacters {

	@Before
	public void setUp() throws Exception {
	}
	
	final static String op = "!%^&*-=+<>/?~|";

	@Test
	public void testOperator() throws Exception
	{
		for (int i=-1; i<1024; i++) {
			if (op.indexOf(i) == -1)
				assertTrue(!Characters.isOperator(i));
			else
				assertTrue(Characters.isOperator(i));
			
		}
	}
	
	@Test
	public void testWord() throws Exception
	{
		for (int i=-1; i<300; i++) {

			if (i>47 && i<58)
				assertTrue(Characters.isWord(i));
			else if (i>64 && i<91)
				assertTrue(Characters.isWord(i));
			else if (i>96 && i<123)
				assertTrue(Characters.isWord(i));
			else if (i==95)
				assertTrue(Characters.isWord(i));
			else if (i<128)
				assertTrue(!Characters.isWord(i));
			else
				assertTrue(Characters.isWord(i));
			
		}
		
		assertTrue(!Characters.isWord('"'));
	}
	
	@Test
	public void testString() throws Exception
	{
		String s="a.gb.@#|c ";
		int i;
		
		for (i=0; i<s.length(); i++)
			if (!Characters.isString(s.charAt(i)))
				break;
		
		assertEquals(i,9);
	}
	
	@Test
	public void testEnd() throws Exception
	{
		for (int i=-1; i<300; i++) {
			if (i>31)
				assertTrue(!Characters.isEnd(i));
			else if (i==9 || i==10 || i==13)
				assertTrue(!Characters.isEnd(i));
			else
				assertTrue(Characters.isEnd(i));
		}
	}
}
