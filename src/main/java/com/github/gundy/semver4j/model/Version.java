package com.github.gundy.semver4j.model;

import com.github.gundy.semver4j.generated.grammar.*;
import com.github.gundy.semver4j.visitor.SemVerRangeExpressionVisitor;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

	private static final ForwardComparator FORWARD_COMPARATOR = new ForwardComparator();
	private static final ReverseComparator REVERSE_COMPARATOR = new ReverseComparator();

	private final int major;
	private final int minor;
	private final int patch;
	private final List<Identifier> preReleaseIdentifiers;
	private final List<Identifier> buildIdentifiers;

	private Version(int major, int minor, int patch, List<Identifier> preReleaseIdentifiers, List<Identifier> buildIdentifiers) {
		if (major < 0) throw new IllegalArgumentException("Major version must be >= 0");
		this.major = major;

		if (minor < 0) throw new IllegalArgumentException("Minor version must be >= 0");
		this.minor = minor;

		if (patch < 0) throw new IllegalArgumentException("Patch version must be >= 0");
		this.patch = patch;

		if (preReleaseIdentifiers == null) {
			throw new IllegalArgumentException("Pre-release identifier list must not be null");
		}
		this.preReleaseIdentifiers = preReleaseIdentifiers;
		if (buildIdentifiers == null) {
			throw new IllegalArgumentException("Build identifier list must not be null");
		}
		this.buildIdentifiers = buildIdentifiers;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getPatch() {
		return patch;
	}

	public List<Identifier> getPreReleaseIdentifiers() {
		return preReleaseIdentifiers;
	}

	public List<Identifier> getBuildIdentifiers() {
		return buildIdentifiers;
	}

	public Version incrementMajor() {
		return new Version(major+1,0,0, Collections.<Identifier>emptyList(), Collections.<Identifier>emptyList());
	}

	public Version incrementMinor() {
		return new Version(major,minor+1,0, Collections.<Identifier>emptyList(), Collections.<Identifier>emptyList());
	}

	public Version incrementPatch() {
		return new Version(major,minor,patch+1, Collections.<Identifier>emptyList(), Collections.<Identifier>emptyList());
	}

	public boolean satisfies(String expression) {
		ParseTree tree = getParseTreeForRange(expression);

		SemVerRangeExpressionVisitor visitor = new SemVerRangeExpressionVisitor(this);
		return visitor.visit(tree);

	}

	public static Version maxVersionSatisfying(Collection<Version> versions, String range) {
		/* compile expression once; use on all versions */
		ParseTree tree = getParseTreeForRange(range);

		ArrayList<Version> matchingVersions = new ArrayList<Version>();
		for (Version version : versions) {
			if (new SemVerRangeExpressionVisitor(version).visit(tree)) {
				matchingVersions.add(version);
			}
		}
		Collections.sort(matchingVersions, Version.reverseComparator());
		if (matchingVersions.size()>0) {
			return matchingVersions.get(0);
		} else {
			return null;
		}
	}

	private static ParseTree getParseTreeForRange(String range) {
		ANTLRInputStream reader = new ANTLRInputStream(range);
		NodeSemverExpressionLexer lexer = new NodeSemverExpressionLexer(reader);
		lexer.removeErrorListeners();
		TokenStream tokens = new CommonTokenStream(lexer);
		NodeSemverExpressionParser parser = new NodeSemverExpressionParser(tokens);
		parser.removeErrorListeners();
		return parser.rangeSet();
	}

	/**
	 * Returns the maximum version from a given collection of versions that satisfies a given range.
	 * @param versionsToTest The collection of versions to test against the range
	 * @param range A range, specified in node semver range format (eg. "&lt;1.0.2 || &gt;1.3")
	 * @return true if the given version falls within the range, false otherwise.
	 */
	public static String maxSatisfying(Collection<String> versionsToTest, String range) {
		/* compile expression once; use on all versions */
		ParseTree tree = getParseTreeForRange(range);

		ArrayList<Version> matchingVersions = new ArrayList<Version>();
		for (String strVersion : versionsToTest) {
			Version version = Version.builder().fromString(strVersion);
			if (new SemVerRangeExpressionVisitor(version).visit(tree)) {
				matchingVersions.add(version);
			}
		}
		Collections.sort(matchingVersions, Version.reverseComparator());
		if (matchingVersions.size()>0) {
			return matchingVersions.get(0).toString();
		} else {
			return null;
		}
	}

	// convenience method
	public static Version fromString(String version) {
		return builder().fromString(version);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private int major=0;
		private int minor=0;
		private int patch=0;
		private final List<Identifier> preReleaseIdentifiers = new ArrayList<Identifier>();
		private final List<Identifier> buildIdentifiers = new ArrayList<Identifier>();

		public Builder major(int major) {
			this.major = major;
			return this;
		}
		public Builder minor(int minor) {
			this.minor = minor;
			return this;
		}
		public Builder patch(int patch) {
			this.patch = patch;
			return this;
		}
		public Builder preReleaseIdentifiers(List<Identifier> identifiers) {
			preReleaseIdentifiers.addAll(identifiers);
			return this;
		}
		public Builder buildIdentifiers(List<Identifier> identifiers) {
			buildIdentifiers.addAll(identifiers);
			return this;
		}
		public Version fromString(String version) {
			ANTLRInputStream reader = new ANTLRInputStream(version);
			NodeSemverVersionLexer lexer = new NodeSemverVersionLexer(reader);
			lexer.removeErrorListeners();
			TokenStream tokens = new CommonTokenStream(lexer);
			NodeSemverVersionParser parser = new NodeSemverVersionParser(tokens);
			parser.removeErrorListeners();
			ParseTree tree = parser.fullySpecifiedVersion();
			SemverVersionVisitor visitor = new SemverVersionVisitor();
			return visitor.visit(tree);
		}
		public Version build() {
			return new Version(
				major,
				minor,
				patch,
				Collections.unmodifiableList(preReleaseIdentifiers),
				Collections.unmodifiableList(buildIdentifiers)
			);
		}
		private static class SemverVersionVisitor extends NodeSemverVersionBaseVisitor<Version> {
			@Override
			public Version visitFullySpecifiedVersion(NodeSemverVersionParser.FullySpecifiedVersionContext ctx) {
				Version.Builder builder= Version.builder()
					.major(parseOptionalInteger(ctx.major))
					.minor(parseOptionalInteger(ctx.minor))
					.patch(parseOptionalInteger(ctx.patch));

				if (ctx.preReleaseIdentifiers != null && ctx.preReleaseIdentifiers.identifier() != null) {
					builder.preReleaseIdentifiers(convertIdentifiers(ctx.preReleaseIdentifiers.identifier()));
				}
				if (ctx.buildIdentifiers != null && ctx.buildIdentifiers.identifier() != null) {
					builder.buildIdentifiers(convertIdentifiers(ctx.buildIdentifiers.identifier()));
				}
				return builder.build();
			}

			private int parseOptionalInteger(NodeSemverVersionParser.IntegerContext val) {
				if (val == null || val.getText() == null || "".equalsIgnoreCase(val.getText())) {
					return 0;
				} else {
					return Integer.parseInt(val.getText());
				}
			}

			private List<Identifier> convertIdentifiers(List<NodeSemverVersionParser.IdentifierContext> contexts) {
				List<Identifier> results = new ArrayList<Identifier>();
				for (NodeSemverVersionParser.IdentifierContext ctx : contexts) {
					results.add(Identifier.fromString(ctx.getText()));
				}
				return results;
			}
		}

	}

	@Override
	public String toString() {
		String preRelease = join(".", preReleaseIdentifiers);
		String build = join(".", buildIdentifiers);
		return "" + major + "." + minor + "." + patch +
			(preReleaseIdentifiers.size()>0 ? "-"+preRelease : "") +
			(buildIdentifiers.size()>0 ? "+"+build : "");
	}

	private static String join(String joiner, Collection<?> objects) {
		Iterator i = objects.iterator();
		StringBuilder sb = new StringBuilder();
		while (i.hasNext()) {
			sb.append(i.next().toString());
			if (i.hasNext()) {
				sb.append(joiner);
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Version that = (Version) o;

		if (major != that.major) return false;
		if (minor != that.minor) return false;
		if (patch != that.patch) return false;
		if (!preReleaseIdentifiers.equals(that.preReleaseIdentifiers)) return false;
		return buildIdentifiers.equals(that.buildIdentifiers);

	}

	@Override
	public int hashCode() {
		int result = major;
		result = 31 * result + minor;
		result = 31 * result + patch;
		result = 31 * result + preReleaseIdentifiers.hashCode();
		result = 31 * result + buildIdentifiers.hashCode();
		return result;
	}

	private static class ForwardComparator implements Comparator<Version> {

		@Override
		public int compare(Version a, Version b) {
			if (a.major < b.major) {
				return -1;
			} else if (a.major > b.major) {
				return 1;
			} else if (a.minor < b.minor) {
				return -1;
			} else if (a.minor > b.minor) {
				return 1;
			} else if (a.patch < b.patch) {
				return -1;
			} else if (a.patch > b.patch) {
				return 1;
			} else {
				return comparePreReleaseVersions(a,b);
			}
		}

		private int comparePreReleaseVersions(Version a, Version b) {
			if (a.preReleaseIdentifiers.equals(b.preReleaseIdentifiers)) {
				return 0;
			}
			if (a.preReleaseIdentifiers.size() == 0 && b.preReleaseIdentifiers.size() != 0) {
				return 1;
			} else if (b.preReleaseIdentifiers.size() == 0 && a.preReleaseIdentifiers.size() != 0) {
				return -1;
			}
			int length = a.preReleaseIdentifiers.size() < b.preReleaseIdentifiers.size()
				? a.preReleaseIdentifiers.size()
				: b.preReleaseIdentifiers.size();

			for (int i = 0; i < length; i++) {
				Identifier left = a.preReleaseIdentifiers.get(i);
				Identifier right = b.preReleaseIdentifiers.get(i);
				int result = left.compareTo(right);
				if (result != 0) {
					return result;
				}
			}

			if (a.preReleaseIdentifiers.size() > b.preReleaseIdentifiers.size()) {
				return 1;
			} else if (a.preReleaseIdentifiers.size() < b.preReleaseIdentifiers.size()) {
				return -1;
			} else {
				return 0;
			}
		}

	}

	private static final class ReverseComparator extends ForwardComparator {
		@Override
		public int compare(Version a, Version b) {
			return -super.compare(a, b);
		}
	}

	@Override
	public int compareTo(Version o) {
		return FORWARD_COMPARATOR.compare(this, o);
	}

	public static final Comparator<Version> forwardComparator() {
		return FORWARD_COMPARATOR;
	}

	public static final Comparator<Version> reverseComparator() {
		return REVERSE_COMPARATOR;
	}


	public static final class Identifier implements Comparable<Identifier> {
        private static final Pattern PATTERN_NUMERIC = Pattern.compile("^[0-9]+$");
        private static final Pattern PATTERN_VALID = Pattern.compile("[\\-A-Za-z0-9]+");
        private final String identifier;
        private final boolean isNumeric;

        private Identifier(String identifier) {
			if (!PATTERN_VALID.matcher(identifier).matches()) {
				throw new IllegalArgumentException("Identifier must match [-A-Za-z0-9]+");
			}
            this.identifier = identifier;
            this.isNumeric = PATTERN_NUMERIC.matcher(identifier).matches();
        }

        public static Identifier fromString(String identifier) {
            return new Identifier(identifier);
        }

        public String getIdentifier() {
            return identifier;
        }

        public boolean isNumeric() {
            return isNumeric;
        }

        @Override
        public String toString() {
            return identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Identifier that = (Identifier) o;

            return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

        }

        @Override
        public int hashCode() {
            return identifier != null ? identifier.hashCode() : 0;
        }

        @Override
        public int compareTo(Identifier o) {
            if (isNumeric() && !o.isNumeric()) {
                return -1;
            } else if (!isNumeric() && o.isNumeric()) {
                return 1;
            } else if (isNumeric()) {
                return Long.valueOf(getIdentifier()).compareTo(Long.valueOf(o.getIdentifier()));
            } else {
                return getIdentifier().compareTo(o.getIdentifier());
            }
        }
    }
}
