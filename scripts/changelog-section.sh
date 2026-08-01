#!/bin/sh
#
# Prints one version's section of CHANGELOG.md, so that the GitLab release notes are the changelog
# rather than a second description that drifts away from it. Exits non-zero when the section is
# missing or empty, which is what makes it usable as a CI gate: a version cannot be released while
# nobody has said what is in it.
#
# Usage:
#   scripts/changelog-section.sh              # the version in gradle.properties
#   scripts/changelog-section.sh 0.2.0        # a specific version
#   scripts/changelog-section.sh 0.2.0 OTHER_CHANGELOG.md

set -eu

repo_root=$(CDPATH='' cd -- "$(dirname -- "$0")/.." && pwd)

version="${1:-}"
if [ -z "$version" ]; then
    version=$(sed -n 's/^VERSION_NAME=//p' "$repo_root/gradle.properties")
fi
if [ -z "$version" ]; then
    echo "changelog-section: no version given and VERSION_NAME is not set in gradle.properties" >&2
    exit 2
fi

changelog="${2:-$repo_root/CHANGELOG.md}"
if [ ! -f "$changelog" ]; then
    echo "changelog-section: $changelog not found" >&2
    exit 2
fi

# Blank lines are held back and only emitted once something follows them, which trims the padding
# around the heading without collapsing the blank lines that separate subsections.
section=$(
    awk -v version="$version" '
        $0 ~ "^## \\[" version "\\]" { in_section = 1; next }
        !in_section { next }
        /^## \[/ { exit }
        # The link reference definitions collected at the bottom of the file belong to the changelog,
        # not to the release notes, and the last section would otherwise swallow them.
        /^\[[^]]+\]:[[:space:]]*https?:/ { next }
        /^[[:space:]]*$/ { if (seen_content) held++; next }
        {
            while (held > 0) { print ""; held-- }
            seen_content = 1
            print
        }
    ' "$changelog"
)

if [ -z "$section" ]; then
    echo "changelog-section: CHANGELOG.md has no entries under '## [$version]'." >&2
    echo "Add a section for the version being released before tagging it." >&2
    exit 1
fi

printf '%s\n' "$section"
