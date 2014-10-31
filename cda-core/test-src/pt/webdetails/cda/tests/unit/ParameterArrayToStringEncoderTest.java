package pt.webdetails.cda.tests.unit;

import junit.framework.Assert;
import org.junit.Test;
import pt.webdetails.cda.dataaccess.Parameter;
import pt.webdetails.cda.utils.ParameterArrayToStringEncoder;

public class ParameterArrayToStringEncoderTest {

  @Test
  public void testStringParameterArrayEncoder() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ",", "'" );
    String result = encoder.encodeParameterArray( new String[] {"aa", "bb", "cc"}, Parameter.Type.STRING_ARRAY );

    Assert.assertEquals( "'aa','bb','cc'", result );

  }

  @Test
  public void testStringParameterArrayEncoderNullQuote() {
    ParameterArrayToStringEncoder encoder = new ParameterArrayToStringEncoder( ",", null );
    String result = encoder.encodeParameterArray( new String[] {"aa", "bb", "cc"}, Parameter.Type.STRING_ARRAY );

    Assert.assertEquals( "aa,bb,cc", result );

  }


}
