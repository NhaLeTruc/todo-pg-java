# Contributing Guide

## Pre-Commit Checks

This project enforces quality standards through automated Git hooks. Before every commit, the following checks run automatically:

### 1. Markdown File Location Check

All `.md` files must be in the correct location:

**✅ Allowed:**
- `/README.md` - Project overview
- `/CLAUDE.md` - Agent context
- `/docs/**/*.md` - All documentation
- `/specs/**/*.md` - Feature specifications

**❌ Blocked:**
- Any other `.md` file in the root directory
- Any `.md` file in source code directories (except specs)

**To fix violations:**
```bash
# Move the file to docs/
git mv path/to/file.md docs/file.md
```

### 2. Credential Leak Prevention

The hook scans for common credential patterns:

**Detected patterns:**
- Passwords in configuration
- API keys and secret keys
- AWS Access Keys (`AKIA...`)
- Google API Keys (`AIza...`)
- GitHub Tokens (`ghp_...`)
- Bearer tokens
- Private keys

**Best practices:**
- ✅ Use environment variables
- ✅ Use `.env.example` with placeholders
- ✅ Add credential files to `.gitignore`
- ❌ Never hardcode credentials
- ❌ Never commit `.env` files

**Example - Bad:**
```yaml
# ❌ DON'T DO THIS
database:
  password: "SuperSecret123"
  api_key: "sk-1234567890abcdef"
```

**Example - Good:**
```yaml
# ✅ DO THIS INSTEAD
database:
  password: ${DB_PASSWORD}
  api_key: ${API_KEY}
```

### 3. Code Linting

All code changes are automatically linted before commit:

#### Java (Backend)
```bash
# Linter: Spotless + Checkstyle
# Standard: Google Java Style Guide

# To fix formatting issues:
cd backend
mvn spotless:apply
```

#### TypeScript/JavaScript (Frontend)
```bash
# Linter: ESLint + Prettier

# To fix formatting issues:
cd frontend
npm run lint:fix
```

#### Markdown
```bash
# Linter: markdownlint (optional)

# Install globally:
npm install -g markdownlint-cli

# Run manually:
markdownlint docs/**/*.md
```

## Bypassing Checks (Not Recommended)

If you absolutely must bypass the checks (emergencies only):

```bash
git commit --no-verify
```

**⚠️ Warning:** Bypassing checks may cause CI/CD failures and security issues.

## Testing the Pre-Commit Hook

To verify the hook is working:

```bash
# Test credential detection
echo 'my_token="test123"' > test.txt
git add test.txt
git commit -m "test"
# Should be blocked

# Clean up
git reset
rm test.txt

# Test markdown location
echo '# Test' > test.md
git add test.md
git commit -m "test"
# Should be blocked

# Clean up
git reset
rm test.md
```

## Installing Linting Tools

### Backend (Java)
Maven dependencies are configured in `backend/pom.xml`:
- Spotless: Auto-formatting
- Checkstyle: Style checking

No manual installation needed - Maven handles it.

### Frontend (TypeScript)
Install dependencies:
```bash
cd frontend
npm install
```

Configured linters:
- ESLint: Code quality
- Prettier: Code formatting

### Markdown (Optional)
```bash
npm install -g markdownlint-cli
```

## Pre-Commit Hook Location

The hook is located at:
```
.git/hooks/pre-commit
```

**Note:** Git hooks are not committed to the repository. New contributors need to ensure the hook is executable:

```bash
chmod +x .git/hooks/pre-commit
```

## Troubleshooting

### Hook not running
```bash
# Make sure it's executable
chmod +x .git/hooks/pre-commit

# Verify it exists
ls -la .git/hooks/pre-commit
```

### Linter not found
```bash
# Backend - install Maven
# macOS: brew install maven
# Ubuntu: sudo apt install maven
# Windows: Download from https://maven.apache.org/

# Frontend - install Node.js and npm
# macOS: brew install node
# Ubuntu: sudo apt install nodejs npm
# Windows: Download from https://nodejs.org/
```

### False positive credential detection
If legitimate code is flagged as a credential:
1. Check if it's truly not a credential
2. Use variable names that don't match patterns
3. Add it to a `.example` or `.template` file (excluded from scanning)
4. As last resort: use `--no-verify` (document why in commit message)

## Questions?

For questions or issues with the pre-commit hooks, please:
1. Check this guide first
2. Review the hook source: `.git/hooks/pre-commit`
3. Open an issue in the project repository
