#!/bin/bash

# Security Audit Script with Dependency Scanning
# Checks for vulnerabilities in dependencies and code

set -e

BOLD='\033[1m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BOLD}================================${NC}"
echo -e "${BOLD}Security Audit Script${NC}"
echo -e "${BOLD}================================${NC}"
echo ""

# Function to print success message
success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Function to print error message
error() {
    echo -e "${RED}✗ $1${NC}"
}

# Function to print warning message
warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Function to print section header
section() {
    echo -e "\n${BOLD}$1${NC}"
    echo "----------------------------------------"
}

# Check if Maven is available
section "1. Checking Prerequisites"
if command -v mvn &> /dev/null; then
    success "Maven found"
else
    error "Maven not found - required for backend dependency scanning"
    exit 1
fi

if command -v npm &> /dev/null; then
    success "npm found"
else
    warning "npm not found - frontend dependency scanning will be skipped"
fi

# Backend Security Audit
section "2. Backend Dependency Scanning (Maven)"

cd backend

# Check for OWASP Dependency Check plugin
if grep -q "dependency-check-maven" pom.xml; then
    success "OWASP Dependency Check plugin configured"

    echo "  Running OWASP dependency check (this may take several minutes)..."
    if mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 2>&1 | tee /tmp/dep-check-output.log; then
        success "No critical vulnerabilities found (CVSS < 7)"
    else
        if grep -q "One or more dependencies were identified with known vulnerabilities" /tmp/dep-check-output.log; then
            error "Critical vulnerabilities found in dependencies!"
            echo "  Check: backend/target/dependency-check-report.html for details"
        else
            warning "Dependency check completed with warnings"
        fi
    fi
else
    warning "OWASP Dependency Check plugin not configured"
    echo "  Add to pom.xml:"
    echo "  <plugin>"
    echo "    <groupId>org.owasp</groupId>"
    echo "    <artifactId>dependency-check-maven</artifactId>"
    echo "    <version>9.0.0</version>"
    echo "  </plugin>"
fi

# Check for outdated dependencies
section "3. Checking for Outdated Dependencies"

echo "  Checking for dependency updates..."
if mvn versions:display-dependency-updates -q 2>&1 | grep -q "No dependencies"; then
    success "All dependencies are up to date"
else
    warning "Some dependencies have newer versions available"
    echo "  Run 'mvn versions:display-dependency-updates' for details"
fi

cd ..

# Frontend Security Audit
section "4. Frontend Dependency Scanning (npm)"

if command -v npm &> /dev/null; then
    cd frontend

    # npm audit
    echo "  Running npm audit..."
    if npm audit --audit-level=moderate 2>&1 | tee /tmp/npm-audit-output.log; then
        success "No moderate or higher vulnerabilities found"
    else
        if grep -q "found.*vulnerabilities" /tmp/npm-audit-output.log; then
            warning "Vulnerabilities found in frontend dependencies"
            echo "  Run 'npm audit fix' to fix automatically"
            echo "  Run 'npm audit' for detailed report"
        fi
    fi

    # Check for outdated packages
    echo "  Checking for outdated packages..."
    if npm outdated 2>&1 | grep -q "Package.*Current.*Wanted.*Latest"; then
        warning "Some npm packages have updates available"
        echo "  Run 'npm outdated' for details"
    else
        success "All npm packages are up to date"
    fi

    cd ..
else
    warning "npm not available, skipping frontend audit"
fi

# Code Security Checks
section "5. Code Security Analysis"

cd backend

# Check for common security issues in Java code
echo "  Scanning for common security patterns..."

SECURITY_ISSUES=0

# Check for hardcoded credentials
if grep -r "password.*=.*['\"]" src/ --include="*.java" | grep -v "password.*\${" | grep -v "//"; then
    error "Found potential hardcoded passwords"
    ((SECURITY_ISSUES++))
fi

# Check for SQL injection vulnerabilities (raw SQL usage)
if grep -r "createNativeQuery\|createQuery.*concat\|executeQuery.*concat" src/ --include="*.java" 2>/dev/null; then
    warning "Found potential SQL injection risks (review manual queries)"
    ((SECURITY_ISSUES++))
fi

# Check for insecure random usage
if grep -r "java\.util\.Random\|Math\.random" src/ --include="*.java" 2>/dev/null; then
    warning "Found usage of insecure random (use SecureRandom for security)"
    ((SECURITY_ISSUES++))
fi

# Check for disabled security features
if grep -r "\.csrf()\.disable()\|\.cors()\.disable()" src/ --include="*.java" 2>/dev/null; then
    warning "Found disabled security features (CSRF/CORS)"
    ((SECURITY_ISSUES++))
fi

if [ $SECURITY_ISSUES -eq 0 ]; then
    success "No obvious security issues found in code patterns"
else
    warning "Found $SECURITY_ISSUES potential security issues - manual review recommended"
fi

cd ..

# Configuration Security
section "6. Configuration Security Check"

echo "  Checking application.yml for security issues..."

CONFIG_ISSUES=0

# Check for hardcoded secrets
if grep -E "(password|secret|key):.*['\"]" backend/src/main/resources/application.yml | grep -v "\${" | grep -v "#"; then
    error "Found potential hardcoded secrets in application.yml"
    ((CONFIG_ISSUES++))
fi

# Check for disabled security features
if grep -q "csrf.*enabled.*false\|cors.*enabled.*false" backend/src/main/resources/application.yml 2>/dev/null; then
    warning "Security features may be disabled in configuration"
    ((CONFIG_ISSUES++))
fi

# Check for production-unsafe settings
if grep -q "spring\.jpa\.show-sql.*true\|logging\.level.*DEBUG" backend/src/main/resources/application.yml 2>/dev/null; then
    warning "Found development settings that should not be used in production"
    ((CONFIG_ISSUES++))
fi

if [ $CONFIG_ISSUES -eq 0 ]; then
    success "No security issues found in configuration"
fi

# Docker Image Security
section "7. Docker Security Check"

if [ -f "backend/Dockerfile" ]; then
    echo "  Checking Dockerfile for security best practices..."

    DOCKER_ISSUES=0

    # Check for non-root user
    if ! grep -q "USER.*[^root]" backend/Dockerfile; then
        warning "Dockerfile may be running as root user"
        ((DOCKER_ISSUES++))
    fi

    # Check for pinned base image versions
    if grep -q "FROM.*:latest" backend/Dockerfile; then
        warning "Using :latest tag in base image (pin specific versions)"
        ((DOCKER_ISSUES++))
    fi

    # Check for exposed secrets in build args
    if grep -q "ARG.*PASSWORD\|ARG.*SECRET" backend/Dockerfile; then
        warning "Potential secrets in build arguments"
        ((DOCKER_ISSUES++))
    fi

    if [ $DOCKER_ISSUES -eq 0 ]; then
        success "Dockerfile follows security best practices"
    fi
fi

# Environment Variable Check
section "8. Environment Variables Security"

if [ -f ".env" ]; then
    warning ".env file found in repository (should be in .gitignore)"
    if ! grep -q "^\.env$" .gitignore 2>/dev/null; then
        error ".env is NOT in .gitignore - risk of committing secrets!"
    else
        success ".env is properly ignored by git"
    fi
fi

if [ -f ".env.example" ]; then
    success ".env.example template found"

    # Check for actual secrets in .env.example
    if grep -E "(password|secret|key)=.{8,}" .env.example | grep -v "CHANGE_ME\|example\|your-" 2>/dev/null; then
        warning "Potential actual secrets in .env.example"
    fi
fi

# Git History Check
section "9. Git History Scan"

if command -v git &> /dev/null && [ -d ".git" ]; then
    echo "  Scanning git history for potential secrets..."

    # Check last 100 commits for common secret patterns
    if git log --all --pretty=format: -p -100 | \
       grep -E "(password|secret|api_key|private_key).*=.*['\"]" | \
       grep -v "CHANGE_ME\|example\|your-\|\$\{" | head -5; then
        error "Found potential secrets in git history!"
        echo "  Manual review recommended"
        echo "  Consider using BFG Repo-Cleaner or git-filter-repo"
    else
        success "No obvious secrets found in recent git history"
    fi
else
    warning "Git not available or not a git repository"
fi

# Summary
section "Security Audit Summary"

echo ""
echo -e "${BOLD}Recommendations:${NC}"
echo "  1. Keep all dependencies up to date"
echo "  2. Run 'mvn org.owasp:dependency-check-maven:check' regularly"
echo "  3. Run 'npm audit' before each release"
echo "  4. Never commit .env files with actual secrets"
echo "  5. Use environment variables for all sensitive configuration"
echo "  6. Pin Docker base image versions"
echo "  7. Run as non-root user in production containers"
echo "  8. Enable and configure HTTPS/TLS in production"
echo "  9. Implement proper secret rotation procedures"
echo "  10. Use a secrets management service (AWS Secrets Manager, Vault, etc.)"
echo ""
echo -e "${GREEN}${BOLD}✓ Security audit completed${NC}\n"
echo "Reports generated:"
echo "  - Backend: backend/target/dependency-check-report.html"
echo "  - Frontend: Run 'cd frontend && npm audit' for detailed report"
echo ""
