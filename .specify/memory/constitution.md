<!--
SYNC IMPACT REPORT
==================
Version Change: 1.0.0 → 1.1.0
Type: MINOR (removed technology-specific sections to keep constitution principle-based)

Modified Principles: None

Added Sections: N/A

Removed Sections:
  - Technology Stack Requirements (moved to plan.md per-feature basis)
  - Project Structure (moved to plan.md per-feature basis)

Rationale: Constitution should remain technology-agnostic and principle-based.
Technology choices and project structure are implementation details that belong
in feature-specific plan.md documents, not in the overarching constitution.

Templates Requiring Updates:
  ✅ .specify/templates/plan-template.md - Already contains Technical Context and Project Structure sections
  ✅ .specify/templates/spec-template.md - No updates needed (technology-agnostic)
  ✅ .specify/templates/tasks-template.md - No updates needed (structure-agnostic)

Follow-up TODOs:
  - None (all placeholders filled)
-->

# TODO List Application Constitution

## Core Principles

### I. Test-Driven Development (TDD) for Core Logic (NON-NEGOTIABLE)

**All core business logic MUST follow strict TDD**:
- Tests are written FIRST, before any implementation code
- Tests MUST fail initially (red phase)
- Implementation follows to make tests pass (green phase)
- Refactoring occurs only after tests pass (refactor phase)
- No core logic code without corresponding tests written first

**Core logic** includes: domain models, business rules, validation logic, calculations, state management, data transformations.

**Rationale**: TDD ensures correctness, prevents regression, drives better design through testability constraints, and creates living documentation of expected behavior. Core logic is the heart of the application and must have the highest quality standards.

### II. Hybrid Testing Strategy

**Testing MUST follow a two-tier approach**:

**Tier 1 - TDD Unit Tests (Core Logic)**: Written BEFORE implementation
- Domain models and entities
- Business rule validators
- Service layer logic
- Utility functions and transformations
- All code that contains business logic

**Tier 2 - Integration Tests (After Implementation)**: Written AFTER implementation works
- API endpoint contracts
- Database operations (CRUD with real/test DB)
- Cross-service communication
- External system integrations
- End-to-end user workflows

**Contract tests** verify interfaces between components and MUST be included when:
- Creating new service boundaries
- Modifying existing contracts/interfaces
- Integrating with external systems
- Defining API endpoints

**Rationale**: TDD for core logic provides fast feedback and design guidance. Integration tests written after validate the complete system behavior without slowing initial development. This hybrid approach balances quality with velocity.

### III. Clean Code & Maintainability

**Code MUST prioritize long-term maintainability**:
- **Readability First**: Code is read 10x more than written; optimize for comprehension
- **Self-Documenting**: Clear naming eliminates most comment needs (variable, function, class names reveal intent)
- **Single Responsibility**: Each class/function does ONE thing well
- **DRY (Don't Repeat Yourself)**: Extract common logic into reusable components
- **YAGNI (You Aren't Gonna Need It)**: Don't build for hypothetical future needs
- **Minimal Complexity**: Prefer simple, obvious solutions over clever abstractions
- **Consistent Style**: Follow language-specific conventions and project formatting rules

**Refactoring is mandatory** when:
- Tests pass but code clarity suffers
- Duplication appears (3+ instances = extract)
- Functions exceed ~30 lines or have >3 levels of nesting
- Cognitive load is high (hard to understand in 30 seconds)

**Rationale**: Maintainable code reduces bugs, accelerates feature development, eases onboarding, and lowers long-term costs. The TODO application should serve as a reference for clean code practices.

### IV. 15-Factor App Methodology

**The application MUST adhere to 15-Factor App principles** (extending 12-Factor):

1. **Codebase**: One codebase tracked in version control, many deploys
2. **Dependencies**: Explicitly declare and isolate dependencies (no system-wide packages)
3. **Config**: Store config in environment variables (never in code)
4. **Backing Services**: Treat databases, caches, queues as attached resources (swappable via config)
5. **Build, Release, Run**: Strictly separate build, release, and run stages
6. **Processes**: Execute app as stateless processes (state in backing services only)
7. **Port Binding**: Export services via port binding (self-contained)
8. **Concurrency**: Scale out via the process model (horizontal scaling)
9. **Disposability**: Maximize robustness with fast startup and graceful shutdown
10. **Dev/Prod Parity**: Keep development, staging, production as similar as possible
11. **Logs**: Treat logs as event streams (stdout/stderr, no file management)
12. **Admin Processes**: Run admin/management tasks as one-off processes
13. **API First**: Design APIs before implementation; APIs are contracts
14. **Telemetry**: Instrument for monitoring, metrics, distributed tracing
15. **Security**: Bake security in from the start (auth, encryption, input validation, least privilege)

**Rationale**: 15-Factor principles ensure the TODO application is cloud-native, scalable, maintainable, and resilient. They provide proven patterns for modern application architecture.

### V. Separation of Concerns

**Architecture MUST enforce clear boundaries**:
- **Domain/Model Layer**: Business entities, rules, validations (no framework dependencies)
- **Service/Application Layer**: Use cases, orchestration, business workflows
- **Infrastructure Layer**: Database, external APIs, file systems, frameworks
- **Presentation Layer**: Controllers, views, API endpoints, serialization

**Dependencies flow inward**: Presentation → Service → Domain (never reverse)
**Infrastructure is pluggable**: Domain layer has no knowledge of databases, HTTP, or frameworks

**Rationale**: Separation enables independent testing, technology swapping (e.g., database changes), and prevents business logic from leaking into infrastructure code. Changes in one layer don't cascade to others.

### VI. Progressive Enhancement

**Build features incrementally with independent value**:
- Start with simplest working solution (MVP)
- Each increment must be independently testable and deployable
- Add complexity only when justified by real requirements
- Features should degrade gracefully if dependencies fail

**Avoid premature optimization** including:
- Elaborate caching before performance issues identified
- Complex abstraction layers for single-use cases
- Over-engineered error handling for scenarios that can't occur

**Rationale**: Progressive enhancement reduces risk, enables faster feedback, and prevents over-engineering. Users get value sooner; developers can pivot based on real usage patterns.

### VII. Observability & Debugging

**Applications MUST be observable in production**:
- **Structured Logging**: JSON logs with context (request ID, user ID, timestamps)
- **Log Levels**: ERROR (requires action), WARN (potential issue), INFO (state changes), DEBUG (detailed flow)
- **Metrics**: Track key operations (request counts, durations, error rates)
- **Health Checks**: Expose endpoints for liveness and readiness probes
- **Correlation IDs**: Trace requests across service boundaries

**Local Development**:
- Logs to stdout/stderr (12-Factor compliance)
- Human-readable format acceptable in dev; JSON in production
- Ability to debug without rebuilding (hot reload where possible)

**Rationale**: Observability is essential for diagnosing production issues quickly. Structured logs enable powerful querying, metrics enable alerting, and health checks enable automated recovery. Good observability reduces mean time to resolution (MTTR).

## Architecture Standards

### Complexity Constraints

**MUST justify exceptions to simplicity**:
- More than 3 layers of abstraction
- More than 3 projects/modules in monorepo
- Introduction of design patterns beyond basic (e.g., Repository, Strategy, Factory)
- New external dependencies (each dependency is a liability)

**When violating YAGNI**, document in plan.md Complexity Tracking table:
- What constraint is being violated
- Why the complexity is necessary NOW (not hypothetical future)
- What simpler alternative was considered and why it's insufficient

## Development Workflow

### Workflow Phases

**Phase 0 - Specification** (speckit.specify):
- Gather user requirements
- Define user stories with acceptance criteria
- Identify key entities and success metrics
- Output: `specs/[###-feature]/spec.md`

**Phase 1 - Planning** (speckit.plan):
- Research existing codebase patterns
- Design data models and API contracts
- Identify technical constraints and risks
- Output: `plan.md`, `research.md`, `data-model.md`, `contracts/`

**Phase 2 - Task Breakdown** (speckit.tasks):
- Generate ordered task list from design docs
- Group tasks by user story (enable independent delivery)
- Mark parallel tasks, identify dependencies
- Output: `tasks.md`

**Phase 3 - Implementation** (speckit.implement):
- Execute tasks in dependency order
- TDD for core logic (tests first)
- Integration tests after implementation
- Commit after each task or logical group

**Phase 4 - Review & Polish**:
- Code review (check constitution compliance)
- Documentation updates
- Performance validation
- Security review

### Constitution Compliance Gates

**Before Phase 0 (Specification)**:
- Feature aligns with product vision
- Sufficient user context provided

**Before Phase 1 (Planning)**:
- ✅ All user stories have acceptance criteria
- ✅ Requirements are testable
- ✅ Success criteria are measurable

**Before Phase 2 (Task Breakdown)**:
- ✅ Data model follows domain-driven design
- ✅ API contracts defined (if applicable)
- ✅ Test strategy documented (TDD vs integration)
- ✅ 15-Factor compliance checked
- ✅ Complexity justified (if applicable)

**Before Phase 3 (Implementation)**:
- ✅ Tasks clearly map to user stories
- ✅ TDD tasks identified and ordered first
- ✅ Integration test tasks placed after implementation
- ✅ Parallel opportunities identified

**During Phase 3 (Implementation)**:
- ✅ Tests written before implementation (for TDD tasks)
- ✅ Tests fail initially (red phase verified)
- ✅ Code follows clean code principles
- ✅ Separation of concerns enforced
- ✅ Logging and observability added

**Before Phase 4 (Review)**:
- ✅ All tests passing
- ✅ Code coverage meets minimum threshold (define in plan.md)
- ✅ No secrets in code
- ✅ API documentation generated
- ✅ Quickstart.md updated/validated

### Code Review Checklist

Reviewers MUST verify:
- [ ] Tests written before implementation (for TDD tasks)
- [ ] Test coverage is adequate (unit + integration)
- [ ] Code is self-documenting (clear naming, minimal comments needed)
- [ ] Single responsibility principle followed
- [ ] No duplication (DRY violations)
- [ ] Configuration externalized (no hardcoded values)
- [ ] Logs include context (correlation IDs, structured format)
- [ ] Error handling is appropriate (not over-engineered)
- [ ] Security basics: input validation, no secrets, auth checks
- [ ] Complexity is justified (or doesn't exist)

### Git Commit Conventions

**Commit messages** follow conventional format:
```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `perf`, `style`

**Example**:
```
feat(todo): add priority field to Task entity

- Add priority enum (HIGH, MEDIUM, LOW)
- Update database schema with migration
- Add validation for priority values

Implements US-001 acceptance criteria 2
```

## Governance

### Amendment Process

**Constitution changes require**:
1. Proposal with rationale (why current constitution is blocking progress)
2. Impact analysis (which templates, code, processes affected)
3. Approval from project stakeholders
4. Migration plan (how existing code/docs will be updated)
5. Version bump following semantic versioning

**Version Bumping Rules**:
- **MAJOR (X.0.0)**: Principle removal, redefinition, or backward-incompatible governance change
- **MINOR (x.Y.0)**: New principle added, section expansion with new mandatory requirements
- **PATCH (x.y.Z)**: Clarifications, wording improvements, typo fixes, non-semantic updates

### Compliance Review

**All pull requests MUST**:
- Reference constitution principles (which apply)
- Explain complexity if introducing any
- Include test evidence (TDD red-green cycle for core logic)
- Pass automated checks (tests, linting, security scans)

**Retrospectives** (after each feature):
- Did TDD improve design quality?
- Were integration tests sufficient?
- Did 15-Factor principles help or hinder?
- Should constitution be amended based on learnings?

### Living Document Philosophy

This constitution is a **living document**. It should:
- Evolve based on real project experience
- Be challenged when it blocks productivity without clear benefit
- Be strengthened when violations cause pain
- Remain concise and actionable (not bureaucratic)

**When in doubt**, favor:
- Simplicity over complexity
- Working code over perfect architecture
- Real user value over theoretical purity
- Team consensus over individual preference

**Version**: 1.1.0 | **Ratified**: 2025-12-26 | **Last Amended**: 2025-12-26
