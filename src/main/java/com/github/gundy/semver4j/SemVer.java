package com.github.gundy.semver4j;

import com.github.gundy.semver4j.model.Version;

import java.util.Collection;

/**
 *
 */
public final class SemVer {
	/**
	 * Check whether a specified version string parses as a valid semver version.
	 * @param version The version string to check (eg. "1.0.3-alpha.01+build")
	 * @return true if the version parses successfully, false otherwise.
     */
	public static boolean valid(String version) {
		try {
			Version.fromString(version);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Check whether a given version falls within a specified range.
	 * @param versionToTest The version to test (eg. "1.0.3")
	 * @param range A range, specified in node semver range format (eg. "&lt;1.0.2 || &gt;1.3")
     * @return true if the given version falls within the range, false otherwise.
     */
	public static boolean satisfies(String versionToTest, String range) {
		try {
			Version version = Version.fromString(versionToTest);
			return version.satisfies(range);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Returns the maximum version from a given collection of versions that satisfies a given range.
	 * @param versionsToTest The collection of versions to test against the range
	 * @param range A range, specified in node semver range format (eg. "&lt;1.0.2 || &gt;1.3")
	 * @return true if the given version falls within the range, false otherwise.
	 */
	public static String maxSatisfying(Collection<String> versionsToTest, String range) {
		return Version.maxSatisfying(versionsToTest, range);
	}
}
