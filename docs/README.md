# Documentation

This directory contains all project documentation except for the root-level README.md and CLAUDE.md files.

## Directory Structure

```
docs/
├── README.md              # This file
├── architecture/          # Architecture documentation
├── api/                   # API documentation
├── deployment/            # Deployment guides
├── development/           # Development guides
└── user-guides/          # End-user documentation
```

## Enforcement

The project uses a Git pre-commit hook to enforce that all `.md` files (except `README.md` and `CLAUDE.md` in the root) must be placed in this `docs/` directory or the `specs/` directory.

## Exceptions

The following markdown files are allowed outside `docs/`:
- `/README.md` - Project overview
- `/CLAUDE.md` - Claude Code agent context
- `/specs/**/*.md` - Feature specifications

All other documentation must be in `/docs/`.
