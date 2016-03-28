package com.github.gundy.semver4j;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SemVerMaxSatisfyingTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
			{Arrays.asList("1.2.3", "1.2.4"), "1.2", "1.2.4"},
			{Arrays.asList("1.2.4", "1.2.3"), "1.2", "1.2.4"},
			{Arrays.asList("1.2.3", "1.2.4", "1.2.5", "1.2.6"), "~1.2.3", "1.2.6"},
	});
	}

	private final List<String> versionsToTest;
	private final String range;
	private final String expectedValue;

	public SemVerMaxSatisfyingTest(List<String> versionsToTest, String range, String expectedValue) {
		this.range = range;
		this.versionsToTest = versionsToTest;
		this.expectedValue = expectedValue;
	}

	@Test
	public void testMaxSatisfying() {
		assertThat(SemVer.maxSatisfying(versionsToTest, range), equalTo(expectedValue));
	}
}
