package com.github.gundy.semver4j;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SemVerTest {
	@Test
	public void testIsValid() {
		assertThat(SemVer.valid("1.2.3"), equalTo(true));
		assertThat(SemVer.valid("a.b.c"), equalTo(false));
	}

	@Test
	public void testSatisfies() {
		assertThat(SemVer.satisfies("1.2.3", "1.2"), equalTo(true));
		assertThat(SemVer.satisfies("1.2.3", "1.2 || >=2.5.0 || 5.0.0 - 7.2.3"), equalTo(true));
		assertThat(SemVer.satisfies("6.3.1", "1.2 || >=8.5.0 || 5.0.0 - 7.2.3"), equalTo(true));
		assertThat(SemVer.satisfies("6.3.1", "1.2 || >=8.5.0 || 2.0.0 - 4.2.3"), equalTo(false));
		assertThat(SemVer.satisfies("1.2.3", "1.1 - 4.0"), equalTo(true));
		assertThat(SemVer.satisfies("1.2.3", "1.1 - 1.2.0"), equalTo(false));
	}
}
