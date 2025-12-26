# Enforcement Quick Reference

## Three Critical Rules (Auto-Enforced)

### 1️⃣ Markdown Files → `docs/` Directory

```bash
✅ README.md                        # Root level (allowed)
✅ CLAUDE.md                        # Root level (allowed)
✅ docs/architecture/design.md      # In docs/ (allowed)
✅ specs/001-feature/spec.md        # In specs/ (allowed)

❌ CHANGELOG.md                     # Should be docs/CHANGELOG.md
❌ backend/SETUP.md                 # Should be docs/development/backend-setup.md
```

**Fix:** `git mv <file.md> docs/<file.md>`

---

### 2️⃣ No Credentials in Commits

```bash
❌ password = "secret123"
❌ api_key = "AKIAIOSFODNN7EXAMPLE"
❌ Bearer eyJhbGciOiJIUzI1NiIs...

✅ password = ${DB_PASSWORD}
✅ # See .env.example for configuration
```

**Fix:**
1. Remove hardcoded credentials
2. Use environment variables
3. Add to `.env` (automatically gitignored)
4. Provide `.env.example` with placeholders

---

### 3️⃣ Code Must Pass Linting

```bash
# Java
cd backend
mvn spotless:apply

# TypeScript/JavaScript
cd frontend
npm run lint:fix
```

---

## Common Commands

```bash
# Test if hook is working
.git/hooks/pre-commit

# Bypass hook (emergency only!)
git commit --no-verify

# Check what's staged
git diff --cached --name-only

# Unstage a file
git reset <file>
```

---

## When Pre-Commit Fails

### Markdown Location Error
```
ERROR: Found .md files outside docs/ directory:
  - design-notes.md
```
**Fix:** `git mv design-notes.md docs/design-notes.md`

### Credential Detected
```
ERROR: Possible credentials detected!
  ✗ config.yml
```
**Fix:**
1. Remove the credential
2. Use `${ENV_VAR}` instead
3. Add value to `.env`

### Linting Error
```
✗ Java formatting issues detected
```
**Fix:** `cd backend && mvn spotless:apply`

---

## File Locations

| Item | Location |
|------|----------|
| Pre-commit hook | `.git/hooks/pre-commit` |
| Gitignore | `.gitignore` |
| Env template | `.env.example` |
| Your env file | `.env` (gitignored) |
| Full docs | `docs/ENFORCEMENT.md` |

---

## Emergency Bypass

```bash
# Only use if absolutely necessary!
git commit --no-verify -m "EMERGENCY: reason for bypass"
```

⚠️ **Bypassed commits may:**
- Fail CI/CD pipelines
- Expose credentials
- Break code style

---

## Questions?

- Read full docs: [docs/ENFORCEMENT.md](ENFORCEMENT.md)
- Contributing guide: [docs/CONTRIBUTING.md](CONTRIBUTING.md)
- File an issue if the hook misbehaves
