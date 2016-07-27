package com.github.gundy.semver4j;


import com.github.gundy.semver4j.model.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SemVerMaxSatisfyingTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
			{Arrays.asList("1.2.3", "1.2.4"), "1.2", "1.2.4"},
			{Arrays.asList("1.2.4", "1.2.3"), "1.2", "1.2.4"},
			{Arrays.asList("1.2.3", "1.2.4", "1.2.5", "1.2.6", "1.3.0"), "~1.2.3", "1.2.6"},
			{Arrays.asList("1.2.3", "1.2.4", "1.2.5", "1.2.6", "1.3.0"), "1.2.*", "1.2.6"},
			{Arrays.asList("0.0.0", "0.0.1", "0.10.24", "0.11.10", "0.10.25", "0.11.10-1", "0.10.25-1", "0.10.31"), "~0.10.x", "0.10.31"},
			{Arrays.asList("0.9.0-1.2.3", "1.0.0-1.2.3", "1.0.1-1.2.3", "1.0.2-1.2.3", "1.0.3-1.2.3",
				"1.0.4-1.2.3", "1.0.5-1.2.3", "1.0.6-1.2.3", "1.0.7-1.2.3", "1.0.8-1.2.3",
				"1.0.8", "1.0.9", "1.0.10", "1.0.11", "1.0.12"), "1.0.2-1.2.3", "1.0.2-1.2.3"},
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

	@Test
	public void testMaxSatisfyingOnVersion() {
		Set<Version> versions = new TreeSet<Version>();
		for (String strVersion : versionsToTest) {
			versions.add(Version.fromString(strVersion));
		}
		assertThat(Version.maxVersionSatisfying(versions, range), equalTo(Version.fromString(expectedValue)));
	}

}
