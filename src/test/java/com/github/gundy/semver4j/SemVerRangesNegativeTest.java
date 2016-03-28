package com.github.gundy.semver4j;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class SemVerRangesNegativeTest {

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
			{"1.0.0 - 2.0.0", "2.2.3"},
			{"1.2.3+asdf - 2.4.3+asdf", "1.2.3-pre.2"},
			{"1.2.3+asdf - 2.4.3+asdf", "2.4.3-alpha"},
			{"^1.2.3+build", "2.0.0"},
			{"^1.2.3+build", "1.2.0"},
			{"^1.2.3", "1.2.3-pre"},
			{"^1.2", "1.2.0-pre"},
			{">1.2", "1.3.0-beta"},
			{"<=1.2.3", "1.2.3-beta"},
			{"^1.2.3", "1.2.3-beta"},
			{"=0.7.x", "0.7.0-asdf"},
			{"0.7.x", "0.7.0-asdf"},
			{">=0.7.x", "0.7.0-asdf"},
			{"1", "1.0.0beta"},
			{"<1", "1.0.0beta"},
			{"< 1", "1.0.0beta"},
			{"1.0.0", "1.0.1"},
			{">=1.0.0", "0.0.0"},
			{">=1.0.0", "0.0.1"},
			{">=1.0.0", "0.1.0"},
			{">1.0.0", "0.0.1"},
			{">1.0.0", "0.1.0"},
			{"<=2.0.0", "3.0.0"},
			{"<=2.0.0", "2.9999.9999"},
			{"<=2.0.0", "2.2.9"},
			{"<2.0.0", "2.9999.9999"},
			{"<2.0.0", "2.2.9"},
			{">=0.1.97", "v0.1.93"},
			{">=0.1.97", "0.1.93"},
			{"0.1.20 || 1.2.4", "1.2.3"},
			{">=0.2.3 || <0.0.1", "0.0.3"},
			{">=0.2.3 || <0.0.1", "0.2.2"},
			{"2.x.x", "1.1.3"},
			{"2.x.x", "3.1.3"},
			{"1.2.x", "1.3.3"},
			{"1.2.x || 2.x", "3.1.3"},
			{"1.2.x || 2.x", "1.1.3"},
			{"2.*.*", "1.1.3"},
			{"2.*.*", "3.1.3"},
			{"1.2.*", "1.3.3"},
			{"1.2.* || 2.*", "3.1.3"},
			{"1.2.* || 2.*", "1.1.3"},
			{"2", "1.1.2"},
			{"<\t2.0.0", "3.2.9"},
			{"2.3", "2.4.1"},
			{"~2.4", "2.5.0"}, // >=2.4.0 <2.5.0
			{"~2.4", "2.3.9"},
			{"~>3.2.1", "3.3.2"}, // >=3.2.1 <3.3.0
			{"~>3.2.1", "3.2.0"}, // >=3.2.1 <3.3.0
			{"~1", "0.2.3"}, // >=1.0.0 <2.0.0
			{"~>1", "2.2.3"},
			{"~1.0", "1.1.0"}, // >=1.0.0 <1.1.0
			{"<1", "1.0.0"},
			{">=1.2", "1.1.1"},
			{"1","2.0.0beta"},
			{"~v0.5.4-beta", "0.5.4-alpha"},
			{"=0.7.x", "0.8.2"},
			{">=0.7.x", "0.6.2"},
			{"<0.7.x", "0.7.2"},
			{"<1.2.3", "1.2.3-beta"},
			{"=1.2.3", "1.2.3-beta"},
			{">1.2", "1.2.8"},
			{"^1.2.3", "2.0.0-alpha"},
			{"^1.2.3", "1.2.2"},
			{"^1.2", "1.1.9"},
			{"*", "v1.2.3-foo"},
			//invalid ranges never satisfied!
			{"blerg", "1.2.3"},
			{"^1.2.3", "2.0.0-pre"}
		});
	}
	
	private final String range;
	private final String versionToTest;

	public SemVerRangesNegativeTest(String range, String versionToTest) {
		this.range = range;
		this.versionToTest = versionToTest;
	}

	@Test
	public void testRangeNegative() {
		assertThat("Range "+range+" must NOT be satisfied by version "+versionToTest, !SemVer.satisfies(versionToTest, range), equalTo(true));
	}

}
