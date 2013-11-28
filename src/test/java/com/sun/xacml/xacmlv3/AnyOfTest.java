/**
 * 
 */
package com.sun.xacml.xacmlv3;

import static org.junit.Assert.fail;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.AnyOf;
import oasis.names.tc.xacml._3_0.core.schema.wd_17.Request;

import org.junit.Test;

import com.sun.xacml.EvaluationCtx;
import com.thalesgroup.authzforce.pdp.core.test.utils.TestUtils;

/**
 * @author Romain Ferrari
 *
 */
public class AnyOfTest {
	
	private EvaluationCtx context;
	private AnyOf anyOf;
	
	public void createAnyOf() {
		anyOf = new AnyOf();
		
	}
	
	public void createContext() {
		Request request = new Request();
		context = TestUtils.createContext(request);
	}

	/**
	 * Test method for {@link com.sun.xacml.xacmlv3.AnyOf#match(com.sun.xacml.EvaluationCtx)}.
	 */
	@Test
	public final void testMatch() {
		fail("Not yet implemented"); // TODO
	}

}
