# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**

I would standardize the repository pattern across all modules. Currently, WarehouseRepository correctly implements the WarehouseStore port and extends PanacheRepository, while Store and Product modules use Panache directly with mixed approaches. This inconsistency makes testing harder and couples business logic to the ORM layer.

The refactoring would involve creating ProductStore and StoreRepository as domain ports (interfaces), implementing them with Panache, and injecting them into the resource classes. This decouples business logic from ORM specifics, makes unit testing easier through mocking, and allows future migrations to different ORMs without changing business code.

Benefits:
- Consistent architecture across the codebase
- Better testability through dependency injection
- Future-proof design if we need to swap Panache for Spring Data or another ORM
- Maintenance cost is less than one day

LocationGateway is correct as a query-side gateway and doesn't need refactoring since it's not handling mutable data.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**

OpenAPI-First approach (used for Warehouse) has clear advantages over Code-First. With OpenAPI, the specification becomes the single source of truth for the API contract. This allows auto-generation of API documentation, client SDKs for different languages, and ensures type safety across different implementations. Team members can review the API spec before implementation begins, preventing misalignment.

Code-First approach (used for Store and Product) allows faster iteration since you can change code immediately without regenerating from specs. The downside is that documentation can drift from the actual implementation, there's no automatic client code generation, and frontend teams must reverse-engineer the API contract from the code.

For production systems, I would choose OpenAPI-First. Although it requires discipline to keep the spec updated and adds iteration overhead, it prevents breaking API changes, keeps documentation current automatically, and is invaluable in team environments. The cost to migrate Store and Product modules to OpenAPI would be 2-3 hours and would pay back within the first month through reduced integration bugs and clearer contracts between frontend and backend teams.
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**

I would use the testing pyramid approach with a 70-20-10 allocation of effort across unit, integration, and end-to-end tests respectively.

Unit tests should form the base, targeting 100% coverage of the business logic layer. These tests are cheap to write, provide instant feedback, and catch about 80% of bugs. Focus on use cases like CreateWarehouseUseCase, ReplaceWarehouseUseCase, LocationGateway resolution, and domain validators. Each critical business rule should have at least one test case verifying success and failure scenarios.

Integration tests should cover critical workflows with both happy paths and error cases. Use QuarkusTest with an in-memory database to test endpoints like WarehouseEndpointIT and StoreResourceIT. Aim for two scenarios per endpoint - one successful operation and one validation failure. This layer catches issues related to database integration, transaction handling, and API contract violations.

End-to-end tests should only cover critical multi-step workflows before production release, such as creating a warehouse and archiving it. These are expensive to maintain, so limit them to genuinely critical paths.

For this project, I would prioritize in three phases: first, complete domain layer tests to achieve 100% coverage of business logic in 30 minutes. Second, add endpoint tests for critical workflows to achieve 90% bug prevention in 1 hour. Third, add repository and remaining service tests to reach 80% overall coverage in 45 minutes.

To maintain effectiveness over time, establish clear policies: the build must fail if coverage drops below 75%, no pull request should be merged without tests for new logic, conduct quarterly reviews to remove flaky tests and identify coverage gaps, and maintain documentation of what is tested and what is intentionally not tested. Keep tests in the same package as code for better visibility.

This approach requires about 20% of development time but prevents 80% of potential bugs and keeps tests maintainable and non-brittle.