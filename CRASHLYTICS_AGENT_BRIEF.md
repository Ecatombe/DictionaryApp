# Implementation Brief: Crashlytics Triage Agent

## Context
Crashlytics issues currently require manual review to spot severity trends and triage into Jira. This agent automates that loop: scan → cluster by severity → propose a fix → hand off a human-reviewed draft into the Jira backlog. Firebase MCP is not yet on the company's approved MCP list, so the agent uses Crashlytics' BigQuery export instead — a supported, queryable path that needs no new MCP approval.

## Goal
A scheduled agent that, once daily, pulls new/changed Crashlytics issues, clusters them by severity, proposes a code-level fix (diff, not applied) for each cluster, and stages the result as a human-approved draft ticket ready to push into the existing Jira backlog pipeline.

## Relevant sources
- **Crashlytics → BigQuery export** (Firebase project's linked BigQuery dataset, tables `crashlytics_<app_id>.crashlytics_issues`/`_events`) — primary data source; must confirm export is enabled for the relevant Firebase project(s).
- App source repo(s) referenced by stack traces — needed to map a crash's stack frames to file/line for the fix-diff step.
- Jira backlog integration described in the earlier conversation (project/board, ticket schema) — destination for approved drafts. Reuse rather than build a second Jira pathway.
- Scheduling mechanism: Claude Code scheduled/cron agent (`/schedule`) or existing company automation (e.g. GitHub Actions), whichever the team already trusts for daily jobs.

## Constraints
- No Firebase MCP available — must go through BigQuery (SQL query via BigQuery client/MCP, or an approved BigQuery MCP if one exists) instead of the Crashlytics Management API/MCP.
- Severity = Crashlytics' own signals (crash-free users %, event count, users affected, regression/velocity flag) — no bespoke scoring formula to build or maintain.
- Fix output is a **suggested diff**, not an applied patch or PR — agent reads stack trace → source file/line and proposes a change, but never commits or opens a PR.
- Jira write path requires **human approval** before ticket creation — the agent drafts, a person confirms, only then does the ticket get created. No auto-create.
- Must not duplicate tickets for issues already triaged/open in Jira — needs some de-dupe check (e.g. match by Crashlytics issue ID stored in a ticket field/label).

## Acceptance criteria
1. Running the agent (manually or on schedule) queries BigQuery for issues new or escalated since the last run.
2. Issues are grouped into severity buckets using Crashlytics-native fields, with the bucketing logic clearly stated in output (not a black box).
3. For each clustered issue (or top-N by severity, if volume is high), the agent produces: issue summary, affected users/events, suspected root cause, and a proposed code diff referencing real file/line from the app repo.
4. Output is staged somewhere a human reviews before Jira (e.g. a draft doc, Slack message, or PR-comment-style preview) — nothing is written to Jira without explicit approval.
5. Approved drafts map cleanly onto the existing Jira ticket schema/board from the earlier-described pipeline.
6. Re-running the agent doesn't re-surface issues already turned into open Jira tickets.

## Risks
- **BigQuery export lag/config**: Crashlytics → BigQuery export isn't real-time and must be explicitly enabled per Firebase project; if it's off, there's no data to query until enabled.
- **Stack trace → source mapping accuracy**: symbolicated traces may not map cleanly to current source (obfuscation, refactors since the crash was recorded), producing wrong or stale fix suggestions.
- **De-dupe correctness**: without a reliable link between a Crashlytics issue and its Jira ticket, re-runs could create duplicate tickets or miss regressions on a "resolved" issue.
- **BigQuery cost/quota**: daily full-table scans across large event tables can get expensive if not scoped with date filters.
- **Diff quality/trust**: an LLM-proposed diff without tests run against it could be plausible but wrong — human review step is load-bearing, not optional.

## Verification steps
1. Confirm BigQuery export is enabled for the target Firebase project; run a manual SQL query to confirm issue/event data is present and recent.
2. Do one dry run against a known, already-triaged crash: verify the severity bucket assigned matches Crashlytics' console view for that issue.
3. Manually check one proposed diff against the actual app repo — does the referenced file/line exist and plausibly relate to the crash?
4. Confirm the draft output includes everything a human needs to approve/reject without opening Crashlytics or the repo separately.
5. Re-run the agent immediately after an approval and confirm the just-approved issue is not re-surfaced as new.
6. Push one approved draft through to Jira and confirm it lands in the right project/board with the schema matching other backlog items.

## Out of scope
- Adding Firebase MCP to the company's approved list (separate approval process, not part of this build).
- Auto-creating Jira tickets without human review.
- Applying fixes or opening PRs automatically — diffs are suggestions only.
- Real-time/hourly monitoring — v1 is a daily batch job.
- Custom/bespoke severity scoring beyond what Crashlytics already reports.
- Symbol/deobfuscation tooling beyond what Crashlytics already provides in its stack traces.
