# Enforcement Mechanisms

This document describes the automated enforcement mechanisms in place to maintain code quality and security.

## Overview

Three critical rules are automatically enforced through Git pre-commit hooks:

1. **Markdown File Organization** - All `.md` files must be in `docs/` (except root `README.md` and `CLAUDE.md`)
2. **Credential Protection** - No credentials or secrets can be committed
3. **Code Linting** - All code changes must pass linting before commit

## 1. Markdown File Organization

### Rule

All markdown documentation must be organized in the `docs/` directory.

### Exceptions

Only these files are allowed outside `docs/`:
- `/README.md` - Project overview (root level)
- `/CLAUDE.md` - Claude Code agent context (root level)
- `/specs/**/*.md` - Feature specifications (specs directory)

### Enforcement

The pre-commit hook scans all staged `.md` files and blocks commits if:
- Any `.md` file is in the root directory (except `README.md` and `CLAUDE.md`)
- Any `.md` file is in source directories (except `specs/`)

### Examples

✅ **Allowed:**
```
README.md
CLAUDE.md
docs/architecture/system-design.md
docs/api/authentication.md
specs/001-feature/spec.md
```

❌ **Blocked:**
```
CHANGELOG.md                    # Should be docs/CHANGELOG.md
backend/DATABASE.md             # Should be docs/development/database.md
design-notes.md                 # Should be docs/architecture/design-notes.md
```

### Fix

Move the file to the appropriate location:
```bash
git mv CHANGELOG.md docs/CHANGELOG.md
```

## 2. Credential Protection

### Rule

No credentials, API keys, secrets, or sensitive information can be committed to the repository.

### Detected Patterns

The hook scans for:
- Passwords: `password = "value"`
- API Keys: `api_key = "AKIAIOSFODNN7EXAMPLE"`
- Secret Keys: `secret_key = "long_random_string"`
- AWS Keys: `AKIA[0-9A-Z]{16}`
- Google API Keys: `AIza[0-9A-Za-z_-]{35}`
- GitHub Tokens: `ghp_[A-Za-z0-9]{36}`
- OpenAI Keys: `sk-[A-Za-z0-9]{20,}`
- Bearer Tokens: `Bearer eyJ...`
- Private Keys: `private_key = "-----BEGIN"`

### Exceptions

Files with these suffixes are excluded from scanning:
- `.example`
- `.sample`
- `.template`

These files can contain example credentials with placeholder values.

### Best Practices

✅ **Do:**
```yaml
# Use environment variables
database:
  password: ${DB_PASSWORD}

# Use .env.example with placeholders
DB_PASSWORD=CHANGE_ME_IN_PRODUCTION
```

❌ **Don't:**
```yaml
# Never hardcode credentials
database:
  password: "SuperSecret123"
  api_key: "sk-1234567890abcdef"
```

### Configuration

1. **Environment Files** - Use `.env` files (gitignored)
2. **Example Templates** - Provide `.env.example` with placeholders
3. **Documentation** - Document required environment variables

### Gitignore Protection

The `.gitignore` file is configured to automatically exclude:
```gitignore
.env
.env.*
*.key
*.pem
credentials.json
secrets.yaml
application-secrets.yml
```

## 3. Code Linting

### Rule

All code changes must pass automated linting before commit.

### Backend (Java)

**Linters:**
- **Spotless** - Code formatting (Google Java Style Guide)
- **Checkstyle** - Code style enforcement

**Configuration:**
- `backend/pom.xml` - Maven plugin configuration

**Manual Fix:**
```bash
cd backend
mvn spotless:apply
```

**Automatic Check:**
Runs on every commit affecting `.java` files.

### Frontend (TypeScript/JavaScript)

**Linters:**
- **ESLint** - Code quality rules
- **Prettier** - Code formatting

**Configuration:**
- `frontend/.eslintrc.json` - ESLint rules
- `frontend/.prettierrc` - Prettier rules
- `frontend/package.json` - npm scripts

**Manual Fix:**
```bash
cd frontend
npm run lint:fix
```

**Automatic Check:**
Runs on every commit affecting `.ts`, `.tsx`, `.js`, `.jsx` files.

### Markdown (Optional)

**Linter:**
- **markdownlint** - Markdown style checking

**Installation:**
```bash
npm install -g markdownlint-cli
```

**Manual Check:**
```bash
markdownlint docs/**/*.md
```

**Automatic Check:**
Runs on every commit affecting `.md` files (if installed).

## Pre-Commit Hook

### Location

`.git/hooks/pre-commit`

### How It Works

1. **Triggered:** Automatically before every `git commit`
2. **Checks:** Runs all three enforcement rules
3. **Blocking:** Prevents commit if any check fails
4. **Output:** Provides clear error messages and fix instructions

### Example Output

```
Running pre-commit checks...

[1/3] Checking markdown file locations...
✓ Markdown file locations OK

[2/3] Scanning for credentials...
✓ No credentials detected

[3/3] Running linters...
  Linting Java files...
  ✓ Java formatting OK
  Linting TypeScript/JavaScript files...
  ✓ TypeScript/JavaScript formatting OK

✓ All pre-commit checks passed!
```

### Bypass (Emergency Only)

```bash
git commit --no-verify
```

⚠️ **Warning:** Bypassing checks may cause:
- CI/CD pipeline failures
- Security vulnerabilities
- Code style inconsistencies

Only bypass in genuine emergencies and document the reason in the commit message.

## Installation for New Contributors

### Prerequisites

1. **Maven** - For Java linting
2. **Node.js/npm** - For frontend linting
3. **markdownlint** (optional) - For markdown linting

### Setup

The pre-commit hook is automatically in place at `.git/hooks/pre-commit`.

Verify it's executable:
```bash
chmod +x .git/hooks/pre-commit
```

Test it:
```bash
# Should pass (good commit)
git add .gitignore
git commit -m "test"

# Should fail (credential detected)
echo 'api_key="sk-test123"' > test.txt
git add test.txt
git commit -m "test"

# Clean up
git reset
rm test.txt
```

## Troubleshooting

### Hook Not Running

```bash
# Check if executable
ls -la .git/hooks/pre-commit

# Make executable
chmod +x .git/hooks/pre-commit
```

### Linter Not Found

```bash
# Install Maven (Java linting)
# macOS
brew install maven

# Ubuntu/Debian
sudo apt install maven

# Windows
# Download from https://maven.apache.org/

# Install Node.js (Frontend linting)
# macOS
brew install node

# Ubuntu/Debian
sudo apt install nodejs npm

# Windows
# Download from https://nodejs.org/
```

### False Positive

If legitimate code is flagged:
1. Verify it's not actually a credential
2. Use different variable naming
3. Move to a `.example` file if it's truly example code
4. Document reason and use `--no-verify` as last resort

## Maintenance

### Updating the Hook

The hook is located at `.git/hooks/pre-commit` and can be edited directly.

After making changes:
```bash
chmod +x .git/hooks/pre-commit
```

### Adding New Patterns

To detect additional credential patterns, edit the `CREDENTIAL_PATTERNS` array in the hook:

```bash
CREDENTIAL_PATTERNS=(
    'password\s*=\s*["\047][^"\047]{3,}["\047]'
    'your_new_pattern_here'
)
```

### Disabling a Check

Comment out the relevant section in `.git/hooks/pre-commit`:

```bash
# To disable credential scanning, comment this:
# if [ "$CREDENTIAL_FOUND" = true ]; then
#     exit 1
# fi
```

## Benefits

✅ **Security** - Prevents credential leaks before they reach GitHub
✅ **Consistency** - Enforces uniform code style across all contributors
✅ **Organization** - Maintains clean documentation structure
✅ **Quality** - Catches issues before code review
✅ **Automation** - No manual checks needed

## Related Files

- `.git/hooks/pre-commit` - The enforcement hook
- `.gitignore` - Credential file exclusions
- `.env.example` - Environment variable template
- `docs/CONTRIBUTING.md` - Developer guide
- `backend/pom.xml` - Java linter configuration
- `frontend/package.json` - Frontend linter configuration
