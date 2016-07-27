package com.github.gundy.semver4j.visitor;

import com.github.gundy.semver4j.generated.grammar.NodeSemverExpressionBaseVisitor;
import com.github.gundy.semver4j.generated.grammar.NodeSemverExpressionParser;
import com.github.gundy.semver4j.model.Version;

public final class SemVerRangeExpressionVisitor extends NodeSemverExpressionBaseVisitor<Boolean> {
	private final Version fullySpecifiedComparisonVersion;

	public SemVerRangeExpressionVisitor(Version fullySpecifiedComparisonVersion) {
		this.fullySpecifiedComparisonVersion = fullySpecifiedComparisonVersion;
	}

	@Override
	public Boolean visitHyphenatedRangeOfFullySpecifiedVersions(NodeSemverExpressionParser.HyphenatedRangeOfFullySpecifiedVersionsContext ctx) {
		Version left = Version.fromString(ctx.left.getText());
		Version right = Version.fromString(ctx.right.getText());

		boolean verGtLeft = fullySpecifiedComparisonVersion.compareTo(left)>=0;

		boolean result;
		if (isEmpty(ctx.right.patch)  && isEmpty(ctx.right.minor)) {
			right = right.incrementMajor();
			result = verGtLeft && fullySpecifiedComparisonVersion.compareTo(right)<0;
		} else if (isEmpty(ctx.right.patch)) {
			right = right.incrementMinor();
			result = verGtLeft && fullySpecifiedComparisonVersion.compareTo(right)<0;
		} else {
			result = fullySpecifiedComparisonVersion.compareTo(left)>=0
				&& fullySpecifiedComparisonVersion.compareTo(right)<=0;
		}

		if (fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size()>0) {
			if (
				left.getPreReleaseIdentifiers().size()>0 && majorMinorPatchMatch(left, fullySpecifiedComparisonVersion)
					|| right.getPreReleaseIdentifiers().size()>0 && majorMinorPatchMatch(right, fullySpecifiedComparisonVersion)
				) {
				return result;
			} else {
				return Boolean.FALSE;
			}
		} else {
			return result;
		}
	}


	@Override
	public Boolean visitEmptyRange(NodeSemverExpressionParser.EmptyRangeContext ctx) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitOperator(NodeSemverExpressionParser.OperatorContext ctx) {
		String operator = ctx.unaryOperator().getText();
		String wc = ctx.partialWildcardSemver().getText().replace(".x", "").replace(".X", "").replace(".*", "");
		Version partiallySpecifiedVersion = Version.fromString(wc);
		boolean result;
		if (">".equals(operator)) {
			if (ctx.partialWildcardSemver().patch == null || ctx.partialWildcardSemver().minor == null) {
				if (ctx.partialWildcardSemver().patch == null) {
					partiallySpecifiedVersion = partiallySpecifiedVersion.incrementMinor();
				}
				if (ctx.partialWildcardSemver().minor == null) {
					partiallySpecifiedVersion = partiallySpecifiedVersion.incrementMajor();
				}
				result = fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) >= 0;
			} else {
				result=fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) > 0;
			}
		} else if (">=".equals(operator)) {
			result=fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) >= 0
				|| matchesWildcard(ctx.partialWildcardSemver());
		} else if ("<".equals(operator)) {
			result=fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) < 0;
		} else if ("<=".equals(operator)) {
			result=fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) <= 0
				|| matchesWildcard(ctx.partialWildcardSemver());
		} else if ("=".equals(operator)) {
			result=fullySpecifiedComparisonVersion.compareTo(partiallySpecifiedVersion) == 0
				|| matchesWildcard(ctx.partialWildcardSemver());
		} else {
			return false;
		}
		/* comparison versions with pre-release tags only match exact versions */
		if (fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size()>0) {
			result = result
				&& partiallySpecifiedVersion.getMajor() == fullySpecifiedComparisonVersion.getMajor()
				&& partiallySpecifiedVersion.getMinor() == fullySpecifiedComparisonVersion.getMinor()
				&& partiallySpecifiedVersion.getPatch() == fullySpecifiedComparisonVersion.getPatch()
				&& partiallySpecifiedVersion.getPreReleaseIdentifiers().size() > 0;
		}
		return result;
	}

	private boolean matchesWildcard(NodeSemverExpressionParser.PartialWildcardSemverContext partialWildcardSemverContext) {
		if (fullySpecifiedComparisonVersion.toString().trim().equals(partialWildcardSemverContext.getText().trim())) {
			return true;
		}
		if (fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size()>0) {
			return false;
		}
		/* simple wildcard matches all */
		if (partialWildcardSemverContext.getText().trim().matches("^[*xX]$")) {
			return Boolean.TRUE;
		}
		boolean result = true;
		if (needsMatching(partialWildcardSemverContext.major)) {
			result = result && Integer.valueOf(partialWildcardSemverContext.major.getText()) == fullySpecifiedComparisonVersion.getMajor();
		}
		if (needsMatching(partialWildcardSemverContext.minor)) {
			result = result && Integer.valueOf(partialWildcardSemverContext.minor.getText()) == fullySpecifiedComparisonVersion.getMinor();
		}
		if (needsMatching(partialWildcardSemverContext.patch)) {
			result = result && Integer.valueOf(partialWildcardSemverContext.patch.getText()) == fullySpecifiedComparisonVersion.getPatch();
		}
		return result;
	}

	/* 	| (GT EQ | LT EQ) ASTERISK                 # wildcardOperator */
	@Override
	public Boolean visitWildcardOperator(NodeSemverExpressionParser.WildcardOperatorContext ctx) {
		return fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size()==0;
	}

	/* compare to a particular partially specified version */
	@Override
	public Boolean visitFullySpecifiedSemver(NodeSemverExpressionParser.FullySpecifiedSemverContext ctx) {
		Version version = Version.fromString(ctx.getText());
		if (isEmpty(ctx.fullSemver().patch) && isEmpty(ctx.fullSemver().minor)) {
			return version.getMajor() == fullySpecifiedComparisonVersion.getMajor()
				&& fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size() == 0;
		} else if (isEmpty(ctx.fullSemver().patch)) {
			return version.getMajor() == fullySpecifiedComparisonVersion.getMajor()
				&& version.getMinor() == fullySpecifiedComparisonVersion.getMinor()
				&& fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size() == 0;
		} else {
			return fullySpecifiedComparisonVersion.compareTo(version) == 0;
		}
	}

	@Override
	public Boolean visitWildcardRange(NodeSemverExpressionParser.WildcardRangeContext ctx) {
		return matchesWildcard(ctx.partialWildcardSemver());
	}

	private boolean needsMatching(NodeSemverExpressionParser.IntegerContext ctx) {
		String text = ctx != null ? ctx.getText() : null;
		return text != null && !"".equals(text);
	}

	private boolean isEmpty(NodeSemverExpressionParser.IntegerContext ctx) {
		return ctx == null || ctx.getText() == null || "".equals(ctx.getText());
	}

	@Override
	public Boolean visitTildeRange(NodeSemverExpressionParser.TildeRangeContext ctx) {
		Version left = Version.fromString(ctx.fullSemver().getText());
		Version right;
		if (isEmpty(ctx.fullSemver().minor) && isEmpty(ctx.fullSemver().patch)) {
			right = left.incrementMajor();
		} else if (isEmpty(ctx.fullSemver().patch)) {
			right = left.incrementMinor();
		} else { /* all three specified */
			right = left.incrementMinor();
		}

		// allow pre-release ID's to be matched only if major/minor/patch versions match
		if (majorMinorPatchMatch(left, fullySpecifiedComparisonVersion) && left.getPreReleaseIdentifiers().size() > 0) {
			return compareLeftInclusiveRightExclusive(left, right);
		}

		// if comparison version has pre-release ID's, fail (major/minor/patch don't match)
		if (fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size() > 0) {
			return Boolean.FALSE;
		}

		return compareLeftInclusiveRightExclusive(left, right);

	}

	/**
	 * Check that comparison version is &gt;= left argument and &lt; right argument.
	 * @param left
	 * @param right
     * @return
     */
	private boolean compareLeftInclusiveRightExclusive(Version left, Version right) {
		return fullySpecifiedComparisonVersion.compareTo(left) >= 0
			&& fullySpecifiedComparisonVersion.compareTo(right) < 0;
	}

	@Override
	public Boolean visitCaretRange(NodeSemverExpressionParser.CaretRangeContext ctx) {
		Version left = Version.fromString(ctx.fullSemver().getText());
		Version right;
		if (left.getMajor()==0 && left.getMinor()==0) {
			right = left.incrementPatch();
		} else if (left.getMajor() == 0) {
			right = left.incrementMinor();
		} else {
			right = left.incrementMajor();
		}

		// allow pre-release ID's to be matched only if major/minor/patch versions match
		if (majorMinorPatchMatch(left, fullySpecifiedComparisonVersion) && left.getPreReleaseIdentifiers().size() > 0) {
			return compareLeftInclusiveRightExclusive(left, right);
		}

		// if comparison version has pre-release ID's, fail (major/minor/patch don't match)
		if (fullySpecifiedComparisonVersion.getPreReleaseIdentifiers().size() > 0) {
			return Boolean.FALSE;
		}

		return compareLeftInclusiveRightExclusive(left, right);
	}

	private static boolean majorMinorPatchMatch(Version left, Version right) {
		return left.getMajor() == right.getMajor()
			&& left.getMinor() == right.getMinor()
			&& left.getPatch() == right.getPatch();
	}

	@Override
	public Boolean visitLogicalAndOfSimpleExpressions(NodeSemverExpressionParser.LogicalAndOfSimpleExpressionsContext ctx) {
		for (NodeSemverExpressionParser.SimpleContext simpleContext : ctx.simple()) {
			if (!visit(simpleContext)) { /* short circuit evaluation */
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitLogicalOrOfMultipleRanges(NodeSemverExpressionParser.LogicalOrOfMultipleRangesContext ctx) {
		for (NodeSemverExpressionParser.BasicRangeContext brc : ctx.basicRange()) {
			if (visitBasicRange(brc)) { /* short circuit evaluation */
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

}
