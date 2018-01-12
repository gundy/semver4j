package com.github.gundy.semver4j.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class VersionTest {

	private static Version parse(String txt) {
		return Version.builder().fromString(txt);
	}

	private static void assertVersion(
		Version version,
		int major,
		int minor,
		int patch,
		List<Version.Identifier> preReleaseIdentifiers,
		List<Version.Identifier> buildIdentifiers
	) {
		assertThat(version.getMajor(), equalTo(major));
		assertThat(version.getMinor(), equalTo(minor));
		assertThat(version.getPatch(), equalTo(patch));
		assertThat(version.getPreReleaseIdentifiers(), equalTo(preReleaseIdentifiers));
		assertThat(version.getBuildIdentifiers(), equalTo(buildIdentifiers));
	}

	private static List<Version.Identifier> ids(String... ids) {
		List<Version.Identifier> results = new ArrayList<Version.Identifier>();
		for (String id : ids) {
			results.add(Version.Identifier.fromString(id));
		}
		return results;
	}

	@Test
	public void testIncrementMajor() {
		Version version = Version.builder().fromString("1.2.4");
		assertThat(version.incrementMajor().toString(), equalTo("2.0.0"));
	}

	@Test
	public void testIncrementMinor() {
		Version version = Version.builder().fromString("1.2.4");
		assertThat(version.incrementMinor().toString(), equalTo("1.3.0"));
	}

	@Test
	public void testIncrementPatch() {
		Version version = Version.builder().fromString("1.2.4");
		assertThat(version.incrementPatch().toString(), equalTo("1.2.5"));
	}

	@Test
	public void testVersionParsing() {
		Version version = Version.builder().fromString("1.0.3-beta4.blah+build1.build2");
		assertVersion(version, 1, 0, 3, ids("beta4", "blah"), ids("build1", "build2"));
		version = Version.builder().fromString("1.2");
		assertVersion(version, 1, 2, 0, ids(), ids());
	}

	@Test
	public void testParseValidSemverVersions() {
		Version version = parse("0.0.0");
		assertVersion(version, 0, 0, 0, ids(), ids());

		version = parse("1.0.0");
		assertVersion(version, 1, 0, 0, ids(), ids());

		version = parse("1.0.0-alpha");
		assertVersion(version, 1, 0, 0, ids("alpha"), ids());

		version = parse("1.0.0-alpha.1");
		assertVersion(version, 1, 0, 0, ids("alpha", "1"), ids());

		version = parse("1.0.0-0.3.7");
		assertVersion(version, 1, 0, 0, ids("0", "3", "7"), ids());

		version = parse("1.0.0-x.7.z.92");
		assertVersion(version, 1, 0, 0, ids("x", "7", "z", "92"), ids());

		version = parse("1.0.0-alpha.1+build.1024");
		assertVersion(version, 1, 0, 0, ids("alpha", "1"), ids("build", "1024"));
	}

	@Test
	public void testSemverOrdering1() {
		List<Version> unsorted = Arrays.asList(
			parse("2.1.0"),
			parse("1.0.0"),
			parse("2.1.1"),
			parse("2.0.0")
		);

		Collections.sort(unsorted, Version.forwardComparator());
		List<String> results = new ArrayList<String>();
		for (Version v : unsorted) {
			results.add(v.toString());
		}

		assertThat(results, equalTo(Arrays.asList(
			"1.0.0",
			"2.0.0",
			"2.1.0",
			"2.1.1"
		)));
	}

	@Test
	public void testSemverOrdering2() {
		List<Version> unsorted = Arrays.asList(
			parse("1.0.0-beta"),
			parse("1.0.0"),
			parse("1.0.0-beta.2"),
			parse("1.0.0-alpha"),
			parse("1.0.0-alpha.1"),
			parse("1.0.0-beta.11"),
			parse("1.0.0-rc.1"),
			parse("1.0.0-alpha.beta")
		);

		Collections.sort(unsorted, Version.forwardComparator());
		List<String> results = new ArrayList<String>();
		for (Version v : unsorted) {
			results.add(v.toString());
		}

		assertThat(results, equalTo(Arrays.asList(
			"1.0.0-alpha",
			"1.0.0-alpha.1",
			"1.0.0-alpha.beta",
			"1.0.0-beta",
			"1.0.0-beta.2",
			"1.0.0-beta.11",
			"1.0.0-rc.1",
			"1.0.0"
		)));
	}

	@Test
	public void testSemverOrdering3() {
		Version v130 = Version.builder().fromString("1.3.0");
		Version v130beta = Version.builder().fromString("1.3.0-beta");
		assertThat(v130.compareTo(v130beta) > 0, equalTo(true));
		assertThat(v130beta.compareTo(v130) < 0, equalTo(true));
	}
}
