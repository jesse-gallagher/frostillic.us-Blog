package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import model.util.PostUtil;

@SuppressWarnings("nls")
public class TestPostUtil {
	@Test
	public void testToCn() {
		assertEquals("Foo Fooson", PostUtil.toCn("cn=Foo Fooson,o=SomeOrg"));
		assertEquals("Bar Barson", PostUtil.toCn("CN=Bar Barson,O=SomeOrg"));
		assertEquals("", PostUtil.toCn("o=SomeOrg"));
		assertEquals("", PostUtil.toCn("O=SomeOrg"));
		assertEquals("Baz Bazson", PostUtil.toCn("Baz Bazson"));
	}
}
