# Git Hard Reset & Git Stash — Enterprise Real-Time Recovery Guide

## 1. Purpose

This document explains two important Git recovery mechanisms used in real-world DevOps environments:

* `git stash`
* `git reset --hard`

It also documents a **real ShopSphere project incident** where a stash created for `ECOM-010` was accidentally applied to the `ECOM-009` branch, causing merge conflicts.

The objective is to understand:

* Why developers use `git stash`
* Difference between `stash apply` and `stash pop`
* What happens when a stash is applied to the wrong branch
* How Git conflicts occur
* How `git reset --hard` can recover a working tree
* How commit IDs protect us during recovery
* How to safely recover without losing committed work
* Enterprise best practices for Git recovery

---

# 2. Real-Time Incident Summary

### Project

**ShopSphere E-Commerce Platform**

Repository:

```text
https://github.com/rakesh-perala/ShopSphere-E-Commerce-Platform.git
```

### Relevant branches

```text
feature/ECOM-009-jenkins-ci
feature/ECOM-010-jenkins-release
```

### Important commit

ECOM-009 successful CI implementation:

```text
a3a8882
```

Commit message:

```text
ECOM-009 publish validated artifact to Nexus
```

This commit was already pushed to GitHub.

Therefore:

```text
a3a8882 = SAFE CHECKPOINT
```

The ECOM-009 branch was clean and working at this commit.

---

# 3. Git Repository Timeline

The important project history looked approximately like this:

```text
ECOM-009 branch

feature/ECOM-009-jenkins-ci
        |
        |
        v
a3a8882
ECOM-009 publish validated artifact to Nexus
        |
        |
        +---- pushed to GitHub
        |
        +---- Jenkins CI successful
        |
        +---- Nexus artifact published
        |
        v
SAFE CHECKPOINT
```

Then development moved to:

```text
feature/ECOM-010-jenkins-release
```

The release pipeline was still under development.

---

# 4. Why Was Git Stash Used?

During ECOM-010 development, files were modified but not yet ready for commit.

For example:

```text
jenkins/release/user-service/Jenkinsfile
pom.xml
```

Instead of committing unfinished work, the changes were temporarily stored using:

```bash
git stash
```

The stash created was:

```text
stash@{0}: On feature/ECOM-010-jenkins-release: ECOM-010 release pipeline WIP
```

Meaning:

```text
stash@{0}
     |
     +---- created on ECOM-010 branch
     |
     +---- contains unfinished ECOM-010 changes
     |
     +---- message: ECOM-010 release pipeline WIP
```

---

# 5. What Does Git Stash Actually Do?

Suppose we have:

```text
Working Tree
     |
     +-- modified file A
     +-- modified file B
     +-- untracked file C
```

Running:

```bash
git stash
```

temporarily stores the changes.

The working directory becomes clean.

Conceptually:

```text
              git stash
                  |
                  v
        +-------------------+
        | Uncommitted Work  |
        +-------------------+
                  |
                  v
             stash@{0}
                  |
                  |
        +---------+---------+
        |                   |
        v                   v
 Working Tree            Stash
    CLEAN              WIP saved
```

Important:

`git stash` does **not** create a normal branch commit in your project history.

It creates a stash entry managed by Git.

---

# 6. Check Existing Stashes

Command:

```bash
git stash list
```

Example from the real incident:

```text
stash@{0}: On feature/ECOM-010-jenkins-release: ECOM-010 release pipeline WIP
stash@{1}: On feature/ECOM-004-cart-service: WIP ECOM-004 documentation
```

Interpretation:

```text
stash@{0}
    |
    +-- ECOM-010 release WIP

stash@{1}
    |
    +-- ECOM-004 documentation WIP
```

The stash number matters.

For example:

```bash
git stash apply stash@{0}
```

means:

```text
Apply ECOM-010 WIP
```

---

# 7. `git stash apply`

Command:

```bash
git stash apply stash@{0}
```

This restores the stash into the current working tree.

### Important behavior

The stash remains available.

Example:

```text
Before:

stash@{0}
   |
   +---- ECOM-010 WIP


git stash apply stash@{0}


After:

Working Tree
   |
   +---- ECOM-010 changes restored

stash@{0}
   |
   +---- STILL EXISTS
```

This is safer when you are unsure.

---

# 8. `git stash pop`

Command:

```bash
git stash pop stash@{0}
```

Conceptually:

```text
stash
  |
  +---- restore changes
  |
  +---- remove stash if successful
```

Therefore:

```text
stash pop = apply + remove
```

But there is an important exception.

If a conflict occurs, Git may keep the stash because the application was not cleanly completed.

Therefore, for recovery work:

```text
Prefer:

git stash apply

instead of:

git stash pop
```

until you confirm everything is correct.

---

# 9. Real Incident — Wrong Branch

This is the actual important incident.

The stash was created on:

```text
feature/ECOM-010-jenkins-release
```

But it was accidentally applied while the repository was on:

```text
feature/ECOM-009-jenkins-ci
```

The dangerous flow was:

```text
ECOM-010 branch
     |
     | git stash
     v
stash@{0}
     |
     | ECOM-010 WIP
     |
     v
switch to ECOM-009
     |
     v
feature/ECOM-009-jenkins-ci
     |
     | git stash pop
     v
WRONG BRANCH
```

---

# 10. Why Did Git Produce Conflicts?

The stash contained changes based on the ECOM-010 branch state.

But the current branch was ECOM-009.

The branches contained different versions of files.

For example:

```text
ECOM-009

pom.xml
    |
    +---- ECOM-009 Nexus configuration


ECOM-010 stash

pom.xml
    |
    +---- ECOM-010 WIP changes
```

Git attempted to combine:

```text
Current branch
      +
Stashed changes
```

and encountered incompatible changes.

---

# 11. Real Conflict

Git reported:

```text
application/services/user-service/pom.xml
```

as conflicted.

It also reported:

```text
jenkins/release/user-service/Jenkinsfile
```

with a message similar to:

```text
deleted in Updated upstream and modified in Stashed changes
```

This means Git detected different changes between:

```text
Current branch
```

and:

```text
Stashed changes
```

---

# 12. What Does This Error Mean?

Example:

```text
deleted in Updated upstream
and
modified in Stashed changes
```

means:

```text
Current branch:
    Jenkinsfile does not exist / was deleted

Stash:
    Jenkinsfile contains changes
```

Git cannot automatically decide:

```text
Should the file remain deleted?
```

or:

```text
Should the stashed version be restored?
```

Therefore Git asks for human resolution.

---

# 13. Important Rule During Conflict

When Git says:

```text
You have unmerged paths.
```

DO NOT immediately run:

```bash
git stash pop
```

again.

DO NOT start making random edits.

First inspect:

```bash
git status
```

Then inspect:

```bash
git diff
```

And determine:

```text
Which branch am I on?
What commit am I on?
Which stash did I apply?
What files are conflicted?
```

---

# 14. First Recovery Question — Which Branch?

Always run:

```bash
git branch --show-current
```

Example:

```text
feature/ECOM-009-jenkins-ci
```

If your stash belongs to ECOM-010:

```text
WRONG BRANCH
```

Stop.

---

# 15. Second Recovery Question — What Commit?

Run:

```bash
git log --oneline -5
```

For our real ECOM-009 checkpoint:

```text
a3a8882 ECOM-009 publish validated artifact to Nexus
```

This was our trusted checkpoint.

Conceptually:

```text
HEAD
 |
 v
a3a8882
 |
 +---- known-good ECOM-009 state
```

---

# 16. Why Commit IDs Are Extremely Important

Git commit IDs provide recovery points.

Example:

```text
a3a8882
```

represents a specific repository state.

We can inspect it:

```bash
git show --stat a3a8882
```

or:

```bash
git show a3a8882
```

We can compare:

```bash
git diff a3a8882 HEAD
```

This is why experienced engineers do not panic when Git becomes messy.

They identify:

```text
Branch
+
HEAD
+
Commit ID
+
Stash
```

and recover systematically.

---

# 17. Real Recovery — `git reset --hard`

After the accidental stash operation, the ECOM-009 branch had unresolved conflicts.

We had a known-good commit:

```text
a3a8882
```

Therefore we used:

```bash
git reset --hard HEAD
```

This restored the tracked files to the current `HEAD` commit.

Conceptually:

```text
Current branch

HEAD
 |
 v
a3a8882
 |
 +----------------------+
 |                      |
 |  conflicted working  |
 |  tree                |
 |                      |
 +----------------------+
            |
            |
      git reset --hard HEAD
            |
            v
       CLEAN WORKTREE
            |
            v
          a3a8882
```

---

# 18. What Does `git reset --hard HEAD` Do?

Command:

```bash
git reset --hard HEAD
```

means:

```text
Make:

HEAD
Index
Working Tree

match the current HEAD commit.
```

Conceptually:

```text
             HEAD
              |
              v
       +--------------+
       | a3a8882      |
       +--------------+
          /       \
         /         \
        v           v
     Index      Working Tree
        |           |
        +-----+-----+
              |
              v
           RESET
              |
              v
         All tracked
         modifications
         discarded
```

---

# 19. Very Important Warning

`git reset --hard` is destructive for **uncommitted tracked changes**.

Example:

```text
pom.xml
```

contains uncommitted changes.

Running:

```bash
git reset --hard HEAD
```

can remove those changes.

Therefore:

```text
COMMITTED WORK
    |
    +---- SAFE

UNCOMMITTED WORK
    |
    +---- CAN BE LOST
```

This is why we first rely on:

```text
commit
stash
remote branch
```

as recovery points.

---

# 20. Why Our ECOM-009 Work Was Safe

The important ECOM-009 work had already been committed:

```text
a3a8882
```

and pushed:

```text
feature/ECOM-009-jenkins-ci
        |
        v
GitHub
        |
        v
a3a8882
```

Therefore:

```text
ECOM-009 implementation
        |
        v
COMMITTED
        |
        v
PUSHED
        |
        v
SAFE CHECKPOINT
```

The accidental stash operation did not destroy that commit.

---

# 21. `reset --hard HEAD` vs `reset --hard <commit>`

### Option 1

```bash
git reset --hard HEAD
```

Means:

```text
Discard working-tree/index changes
and return to current HEAD.
```

Example:

```text
HEAD = a3a8882

reset --hard HEAD

        |
        v

still a3a8882
```

---

### Option 2

```bash
git reset --hard a3a8882
```

Means:

```text
Move HEAD and working tree
to commit a3a8882.
```

Example:

```text
Current:

HEAD
 |
 v
b7c1234
 |
 v
a3a8882


git reset --hard a3a8882


HEAD
 |
 v
a3a8882
```

This is more powerful and therefore more dangerous.

---

# 22. Difference Between `git restore` and `git reset --hard`

### `git restore`

Useful for restoring files.

Example:

```bash
git restore pom.xml
```

This restores the file from the index/HEAD depending on options.

### `git reset --hard`

Resets:

```text
HEAD
Index
Working Tree
```

Example:

```bash
git reset --hard HEAD
```

For normal file-level recovery:

```text
git restore
```

is often safer.

For completely abandoning all tracked working-tree changes:

```text
git reset --hard HEAD
```

can be appropriate.

---

# 23. Safe ECOM-010 Recovery Flow

After ECOM-009 was restored to a clean state, we switched to the correct branch:

```bash
git switch feature/ECOM-010-jenkins-release
```

Then verified:

```bash
git status
```

Then applied the stash safely:

```bash
git stash apply stash@{0}
```

This was better than:

```bash
git stash pop stash@{0}
```

because the stash remained available.

---

# 24. Actual Correct Flow

The corrected flow became:

```text
                    ECOM-010 WIP
                         |
                         v
              +---------------------+
              |     stash@{0}        |
              | ECOM-010 release WIP |
              +---------------------+
                         |
                         |
                         | switch to correct branch
                         v
        feature/ECOM-010-jenkins-release
                         |
                         |
                         | git stash apply stash@{0}
                         v
                Working Tree
                         |
                         v
              ECOM-010 changes restored
                         |
                         v
                    Validate
                         |
                         v
                     Commit
                         |
                         v
                       Push
```

---

# 25. Why `stash apply` Was Safer

After:

```bash
git stash apply stash@{0}
```

we had:

```text
Working Tree
     |
     +---- ECOM-010 changes
     
stash@{0}
     |
     +---- STILL AVAILABLE
```

If something went wrong:

```bash
git reset --hard HEAD
```

could restore the branch.

Then we could apply the stash again.

This gives us a second recovery point.

---

# 26. Current Real-Time State

The current ECOM-010 branch has:

```text
feature/ECOM-010-jenkins-release
```

and the stash remains:

```text
stash@{0}: On feature/ECOM-010-jenkins-release:
ECOM-010 release pipeline WIP
```

The working tree contains changes to:

```text
pom.xml

jenkins/release/user-service/Jenkinsfile
```

The important point is:

```text
DO NOT DROP stash@{0}
```

until the ECOM-010 changes are successfully committed.

---

# 27. Never Do This

### Mistake 1

```bash
git stash pop
```

without checking the branch.

Always check:

```bash
git branch --show-current
```

---

### Mistake 2

Applying an ECOM-010 stash on ECOM-009:

```text
ECOM-010 stash
       |
       v
ECOM-009 branch
```

Avoid this.

---

### Mistake 3

Running `git reset --hard` without understanding the changes.

Bad:

```bash
git reset --hard
```

when you have important uncommitted work.

---

### Mistake 4

Dropping stash immediately.

Bad:

```bash
git stash drop stash@{0}
```

before validating the restored files.

---

# 28. Recommended Enterprise Recovery Sequence

When Git becomes messy:

```bash
git status
```

Then:

```bash
git branch --show-current
```

Then:

```bash
git log --oneline -5
```

Then:

```bash
git stash list
```

Then inspect:

```bash
git stash show --stat stash@{0}
```

For detailed stash changes:

```bash
git stash show -p stash@{0}
```

Only after understanding the situation should you restore anything.

---

# 29. Enterprise Recovery Decision Tree

```text
                 Git problem
                     |
                     v
                git status
                     |
                     v
              What happened?
                     |
          +----------+----------+
          |                     |
          v                     v
   Uncommitted work        Stash conflict
          |                     |
          v                     v
   Need to preserve?       Check branch
          |                     |
      +---+---+                 v
      |       |          git branch --show-current
     YES      NO                |
      |       |                 v
      v       v          Check stash source branch
git stash   restore            |
          files                 v
                         Correct branch?
                              |
                         +----+----+
                         |         |
                        YES        NO
                         |         |
                         v         v
                  git stash apply  switch branch
                                   |
                                   v
                            git stash apply
```

---

# 30. Real Enterprise Git Recovery Flow

```text
Developer
    |
    | modifies files
    v
Working Tree
    |
    | not ready to commit
    v
git stash
    |
    v
stash@{0}
    |
    +-----------------------------+
    |                             |
    | correct branch              | wrong branch
    v                             v
git stash apply              conflict
    |                             |
    v                             v
validate                    git status
    |                             |
    v                             v
commit                      identify HEAD
    |                             |
    v                             v
push                        known-good commit
                                  |
                                  v
                           git reset --hard HEAD
                                  |
                                  v
                              clean tree
                                  |
                                  v
                           switch correct branch
                                  |
                                  v
                       git stash apply stash@{0}
```

---

# 31. Commit-Based Recovery Model

A professional Git workflow should always have identifiable checkpoints.

For example:

```text
                    ShopSphere
                        |
                        v
             ECOM-009 implementation
                        |
                        v
                     a3a8882
                        |
            +-----------+-----------+
            |                       |
            v                       v
         GitHub                 Local HEAD
            |                       |
            +-----------+-----------+
                        |
                        v
                 SAFE CHECKPOINT
```

Then:

```text
SAFE CHECKPOINT
      |
      v
ECOM-010 development
      |
      +---- working changes
      |
      +---- stash@{0}
      |
      v
release pipeline WIP
```

If ECOM-010 goes wrong:

```text
ECOM-010 WIP
     |
     v
reset/recovery
     |
     v
a3a8882
     |
     v
known-good ECOM-009
```

---

# 32. Useful Commands Cheat Sheet

## Check current branch

```bash
git branch --show-current
```

## Check working tree

```bash
git status
```

## Check recent commits

```bash
git log --oneline -5
```

## Check all branches

```bash
git branch -a
```

## Check stash list

```bash
git stash list
```

## Show stash summary

```bash
git stash show --stat stash@{0}
```

## Show complete stash diff

```bash
git stash show -p stash@{0}
```

## Safely apply stash

```bash
git stash apply stash@{0}
```

## Apply and remove stash

```bash
git stash pop stash@{0}
```

## Delete a stash

```bash
git stash drop stash@{0}
```

## Create stash

```bash
git stash push -m "ECOM-010 release pipeline WIP"
```

## Restore current branch to HEAD

```bash
git reset --hard HEAD
```

## Restore to specific commit

```bash
git reset --hard a3a8882
```

## Inspect a commit

```bash
git show a3a8882
```

## Compare two commits

```bash
git diff a3a8882 HEAD
```

## Check reflog

```bash
git reflog
```

---

# 33. `git reflog` — Emergency Recovery

If something appears to be lost:

```bash
git reflog
```

Example:

```text
a3a8882 HEAD@{0}: reset: moving to HEAD
a3a8882 HEAD@{1}: checkout: moving from ...
```

`reflog` helps identify where `HEAD` previously pointed.

This is extremely useful after commands such as:

```bash
git reset
git rebase
git checkout
git switch
```

Example:

```bash
git reflog
```

Find the required commit:

```text
a3a8882
```

Then inspect it:

```bash
git show a3a8882
```

If required, recovery can be performed from that commit.

---

# 34. Important Difference

## `git stash`

Purpose:

```text
Temporarily store uncommitted work.
```

Think:

```text
"I am not ready to commit this yet."
```

---

## `git reset --hard HEAD`

Purpose:

```text
Discard current tracked working-tree/index changes
and return to HEAD.
```

Think:

```text
"I don't want these current tracked changes anymore."
```

---

# 35. Real-Time Example

Developer is working on:

```text
feature/ECOM-010-jenkins-release
```

Files:

```text
pom.xml
jenkins/release/user-service/Jenkinsfile
```

Developer needs to temporarily switch tasks.

Run:

```bash
git stash push -m "ECOM-010 release pipeline WIP"
```

Now:

```text
Working Tree = clean
```

Later:

```bash
git switch feature/ECOM-010-jenkins-release
```

Verify:

```bash
git status
```

Then:

```bash
git stash apply stash@{0}
```

Validate:

```bash
git diff
```

Run tests/validation.

Then:

```bash
git add .
git commit -m "ECOM-010 implement user service release pipeline"
git push
```

Only after confirming everything is committed:

```bash
git stash drop stash@{0}
```

---

# 36. Enterprise Golden Rule

Before using any destructive Git command:

```text
STOP
 |
 +-- What branch am I on?
 |
 +-- What is HEAD?
 |
 +-- Is my work committed?
 |
 +-- Is my work stashed?
 |
 +-- Is the commit pushed?
 |
 +-- Can I recover this if something goes wrong?
```

Then execute the command.

---

# 37. Golden Git Recovery Formula

Remember:

```text
BRANCH
   +
HEAD
   +
COMMIT ID
   +
STASH
   +
REFLOG
   =
GIT RECOVERY
```

These five things allow a DevOps engineer to troubleshoot most normal Git recovery incidents systematically.

---

# 38. ShopSphere Incident — Final Lessons

### Incident

```text
ECOM-010 stash
      |
      v
accidentally applied to
      |
      v
ECOM-009 branch
      |
      v
merge conflicts
```

### Recovery

```text
Identify ECOM-009 known-good commit
              |
              v
           a3a8882
              |
              v
git reset --hard HEAD
              |
              v
ECOM-009 clean
              |
              v
switch to ECOM-010
              |
              v
git stash apply stash@{0}
              |
              v
ECOM-010 changes restored
```

### Important safety decision

We used:

```bash
git stash apply stash@{0}
```

instead of immediately using:

```bash
git stash pop stash@{0}
```

because `apply` keeps the stash available.

---

# 39. Interview Questions

## Q1. What is `git stash`?

`git stash` temporarily stores uncommitted changes so the working directory can be cleaned without committing incomplete work.

---

## Q2. Difference between `git stash apply` and `git stash pop`?

```text
apply = restore stash + keep stash

pop = restore stash + remove stash if successfully applied
```

For recovery, `apply` is safer.

---

## Q3. What does `git reset --hard HEAD` do?

It resets the index and working tree to the current `HEAD` commit, discarding uncommitted tracked changes.

---

## Q4. Is `git reset --hard` dangerous?

Yes.

It can permanently discard uncommitted tracked changes that are not available through another recovery point.

---

## Q5. How would you recover from an accidental stash conflict?

Typical approach:

```bash
git status
git branch --show-current
git log --oneline -5
git stash list
```

Identify the correct branch and known-good commit.

If the current branch has unwanted conflict changes:

```bash
git reset --hard HEAD
```

Then switch to the correct branch and safely apply the stash:

```bash
git stash apply stash@{0}
```

---

## Q6. How do you find lost Git history?

Use:

```bash
git reflog
```

Then inspect the required commit.

---

## Q7. Why are commit IDs important?

A commit ID identifies an exact repository state and provides a reliable recovery/reference point.

Example:

```text
a3a8882
```

---

## Q8. Why should you check the branch before applying a stash?

Because a stash created from one branch may contain changes based on a different code state. Applying it to another branch can cause conflicts or unintended modifications.

---

# 40. Production/Enterprise Best Practices

### 1. Check branch before stash operations

```bash
git branch --show-current
```

### 2. Prefer explicit stash names

```bash
git stash push -m "ECOM-010 release pipeline WIP"
```

### 3. Prefer `stash apply` when recovering

```bash
git stash apply stash@{0}
```

### 4. Do not immediately delete the stash

Validate first.

### 5. Commit working milestones frequently

Example:

```text
a3a8882
```

was a valuable checkpoint.

### 6. Push important commits

```text
Local commit
      |
      v
Remote GitHub
      |
      v
Additional recovery point
```

### 7. Before destructive commands, inspect

```bash
git status
git log --oneline -5
git branch --show-current
git stash list
```

### 8. Use `reflog` when history appears lost

```bash
git reflog
```

---

# 41. One-Line Memory Trick

```text
STASH = Save unfinished work

APPLY = Restore but keep stash

POP = Restore and remove stash

RESET --HARD = Throw away current tracked changes and match HEAD

REFLOG = Find where Git HEAD was previously
```

---

# 42. Final Enterprise Flow

```text
             Developer Working
                    |
                    v
             Uncommitted Changes
                    |
          +---------+---------+
          |                   |
          v                   v
      Ready to commit      Not ready
          |                   |
          v                   v
       git add             git stash
          |                   |
          v                   v
       commit              stash@{0}
          |                   |
          v                   |
        push                  |
          |                   |
          +---------+---------+
                    |
                    v
              Need changes
                    |
                    v
          Check current branch
                    |
                    v
             Correct branch?
                /       \
              YES        NO
               |          |
               v          v
       git stash apply   switch branch
               |          |
               +----+-----+
                    |
                    v
               Validate
                    |
                    v
                 Commit
                    |
                    v
                  Push
                    |
                    v
              Delete stash
              only if safe
```

---

# 43. ShopSphere Recovery Reference

| Item                       | Value                                          |
| -------------------------- | ---------------------------------------------- |
| Project                    | ShopSphere E-Commerce Platform                 |
| ECOM-009 Branch            | `feature/ECOM-009-jenkins-ci`                  |
| ECOM-010 Branch            | `feature/ECOM-010-jenkins-release`             |
| Known-good ECOM-009 commit | `a3a8882`                                      |
| Commit message             | `ECOM-009 publish validated artifact to Nexus` |
| ECOM-010 stash             | `stash@{0}`                                    |
| ECOM-010 stash message     | `ECOM-010 release pipeline WIP`                |
| ECOM-004 stash             | `stash@{1}`                                    |
| Recovery command           | `git reset --hard HEAD`                        |
| Safe stash restoration     | `git stash apply stash@{0}`                    |
| Emergency history recovery | `git reflog`                                   |

---

# 44. Key Takeaway

The most important lesson from this real incident is:

```text
Git problem
    ≠
Git data is lost
```

First identify:

```text
1. Current branch
2. Current HEAD
3. Last known-good commit
4. Stash entries
5. Reflog
```

Then recover systematically.

In our ShopSphere incident:

```text
ECOM-009
   |
   v
a3a8882
   |
   v
Known-good checkpoint
   |
   v
Accidental ECOM-010 stash application
   |
   v
Conflict
   |
   v
reset --hard HEAD
   |
   v
Clean ECOM-009
   |
   v
Switch to ECOM-010
   |
   v
stash apply
   |
   v
ECOM-010 WIP restored
```

This is the real-world Git recovery pattern a DevOps engineer should be comfortable handling.
