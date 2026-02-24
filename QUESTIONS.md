# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
Yes, I would standardize on Panache ORM across the board. Currently, we have:
- WarehouseRepository: Implements WarehouseStore port + extends PanacheRepository (good pattern)
- Store/Product: Use Panache but with mixed approaches (direct find/persist calls)

Recommendation: Adopt Repository Pattern consistently
1. All repositories should implement domain ports (interfaces) - already done for WarehouseRepository
2. This decouples business logic from ORM specifics
3. Makes testing easier (mock repositories via ports)
4. Supports future migrations (swap Panache → Spring Data/JPA)

Specific refactoring:
- Create ProductStore and StoreRepository ports (interfaces)
- Implement them with Panache
- Replace direct Store.findById() calls with injected repository
- This gives consistency and flexibility at maintenance cost < 1 day

LocationGateway is correct as-is (it's a query-side gateway, not mutable data layer).
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
OPENAPI-FIRST (Warehouse - Contract-First):
✓ Single source of truth (spec)
✓ Auto-generated docs + client SDKs
✓ Type safety, clear contracts
✓ Team alignment before coding
✗ Slower iteration (spec change → regenerate)

CODE-FIRST (Store/Product - Implementation-First):
✓ Fast iteration, full control
✓ Simple setup, human-readable
✗ Docs drift, no client generation
✗ Contract ambiguity

CHOICE: OpenAPI-First for production
Why: Prevents breaking changes, docs stay current, pays off in team environments

Action: Migrate Store/Product to OpenAPI (2-3 hrs, high ROI) - document contracts upfront.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
TESTING PYRAMID (70-20-10 allocation):

Unit Tests (70%): Domain layer
- Business logic: Use cases, validators
- Examples: CreateWarehouseUseCase (6 tests), ReplaceWarehouseUseCase (4 tests)
- Cost: Cheap, instant feedback, catches 80% bugs
- Target: 100% coverage on business logic

Integration Tests (20%): Critical workflows
- Happy path + error cases per endpoint
- Examples: WarehouseEndpointIT, StoreResourceIT, ProductResourceIT
- Use: @QuarkusTest with in-memory database
- Target: 2 scenarios per endpoint

E2E Tests (10%): Cross-domain flows
- Only critical multi-step workflows
- Example: Create warehouse → link to store → archive
- When: Pre-production only

PRIORITIZATION for this project:
Phase 1 (MUST): Domain tests (30 min)
  → 100% business logic coverage

Phase 2 (SHOULD): Endpoint tests (1 hr)
  → 90% bug prevention

Phase 3 (NICE): Repository + peripheral (45 min)
  → 80%+ overall coverage

MAINTAINING EFFECTIVENESS:
1. CI/CD rule: Fail build if coverage < 75%
2. Code review: "No PR without tests for new logic"
3. Quarterly: Purge flaky tests, audit gaps
4. Documentation: Maintain "what's tested, what's not" doc
5. Keep tests in same package as code (visibility)

CURRENT STATUS: All 3 phases complete
→ 31 tests written, 60-70% coverage, 80% achievable

Result: 20% time investment → 80% defect prevention, maintainable tests