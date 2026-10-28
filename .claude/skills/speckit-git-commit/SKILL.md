---
name: speckit-git-commit
description: "Review and manually commit changes, or auto-commit when explicitly enabled as a hook"
compatibility: Requires spec-kit project structure with .specify/ directory
metadata:
  author: spec-kit-core
  source: git:commands/speckit.git.commit.md
---

# Review and Commit Changes

This command supports two modes:

## Manual invocation

When the user directly invokes `/speckit-git-commit` (without a hook event):

1. Inspect and summarize all staged, unstaged, and untracked changes. Include `git status --short --untracked-files=all`, `git diff --stat`, and `git diff --cached --stat`; also inspect relevant diff content before summarizing. Do not expose secret values in the summary.
2. Propose a concise commit message based on the changes, or use a message supplied by the user.
3. Use `ask_user` to show the proposed commit message and change summary, and ask the user to confirm committing **all** outstanding changes. Include an option to cancel.
4. If the user cancels or declines, do not stage or change anything.
5. After confirmation, stage all changes with `git add .` and commit with the approved message. Include the required `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>` trailer unless the user explicitly asks to omit it.
6. Pass the commit message to Git without interpolating repository-derived or user-provided text into a shell command. Use a quoted here-document with `git commit -F -` or another safe message-file mechanism.
7. Verify the commit and report its hash and the remaining worktree status.

Do not enable or change automatic hook settings as part of a manual invocation. If inspection finds likely credentials or other sensitive material, do not commit; identify the affected paths without revealing secret values and ask the user how to proceed.

## Hook invocation

When invoked as a before/after Spec Kit hook with an event name (for example `after_tasks` or `before_plan`), preserve the existing automatic behavior:

1. Check `.specify/extensions/git/git-config.yml` for the `auto_commit` section.
2. Look up the event-specific enabled flag; fall back to `auto_commit.default` only when there is no event-specific setting.
3. If disabled, exit without staging or committing.
4. If enabled and changes exist, determine the commit message based on `commit_style` and run the matching auto-commit script.
5. Automatic hook invocations do not prompt the user.

## Commit Message Styles for Hooks

Controlled by the `commit_style` key in `.specify/extensions/git/git-config.yml`:

- **`fixed`** (default): use the per-command `message` if configured, otherwise a generic `[Spec Kit] Auto-commit <phase> <command>` message.
- **`conventional`**: inspect the actual changes (`git diff` / `git status`) since the last commit and generate a single-line [Conventional Commit](https://www.conventionalcommits.org/) message (`type(scope): subject`, e.g. `feat: add OAuth specification` or `docs: update implementation plan`) that accurately summarizes the change. Write this message to a temporary file and pass the file's path to the script (see Execution below). The configured `message` values are ignored in this mode.

## Hook execution

Determine the event name from the hook that triggered this command, then run the script:

- **Bash**: `.specify/extensions/git/scripts/bash/auto-commit.sh <event_name> [--message-file <path>]`
- **PowerShell**: `.specify/extensions/git/scripts/powershell/auto-commit.ps1 <event_name> [-MessageFile <path>]`

Replace `<event_name>` with the actual hook event (e.g., `after_specify`, `before_plan`, `after_implement`). Only pass a generated message when `commit_style: conventional` is configured — first check `.specify/extensions/git/git-config.yml` for the value of `commit_style`:

- If `conventional`: inspect the diff and generate a Conventional Commit message. **Do not interpolate the generated message directly into a shell command string** — its content is derived from repository changes and may contain characters (quotes, `$(...)`, backticks) that a shell would execute or that would break command quoting. Instead, write the message to a temporary file using your file-editing tool (not a shell `echo`/`printf`), then pass that file's path via `--message-file <path>` (Bash) or `-MessageFile <path>` (PowerShell).
- If `fixed` or absent: run the script with just `<event_name>`; it uses the configured/static message.

## Configuration

In `.specify/extensions/git/git-config.yml`:

```yaml
# "fixed" (default) uses the messages below; "conventional" asks the agent
# to generate a Conventional Commit message from the diff instead.
commit_style: fixed

auto_commit:
  default: false          # Global toggle — set true to enable for all commands
  after_specify:
    enabled: true          # Override per-command
    message: "[Spec Kit] Add specification"
  after_plan:
    enabled: false
    message: "[Spec Kit] Add implementation plan"
```

## Graceful Degradation

- If Git is not available or the current directory is not a repository: skips with a warning
- If no config file exists: skips (disabled by default)
- If no changes to commit: skips with a message
- If `commit_style: conventional` is set and no generated message was supplied: fails with a clear error instead of silently falling back to the fixed message format