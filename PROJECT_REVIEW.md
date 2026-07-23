# Production Readiness Review

Audit completed on 2026-07-22. Line numbers refer to the reviewed baseline. Findings are grouped by remediation priority; the implementation changes that follow this report address the critical and high-risk findings where feasible without changing the product's intended behaviour.

## Critical Issues

### C-1: Database and signing secrets committed in application configuration
- **File:** `src/main/resources/application.properties:8-10, 28`
- **Problem:** A PostgreSQL password and a JWT HMAC signing secret are committed in source control.
- **Why it is wrong:** Source-controlled secrets are routinely copied into developer machines, build logs, backups, and forks. The signing key permits forged access tokens.
- **Production impact:** Database compromise and full authentication bypass; the existing credentials must be considered exposed and rotated.
- **Recommended fix:** Read all credentials from environment variables or a managed secret store, fail safely when required secrets are missing, and rotate the exposed credentials before deployment.

### C-2: Any client can attempt to create an administrator
- **File:** `src/main/java/com/vof/config/SecurityConfig.java:31-36`; `src/main/java/com/vof/controller/AuthController.java:78-87`
- **Problem:** `/api/auth/**` is globally permitted, which includes `/register-admin`. The controller performs its own role check instead of enforcing authorization in the security policy.
- **Why it is wrong:** Security policy should protect privileged endpoints before controller code runs. Controller-only checks are easy to regress and are not visible in the security boundary.
- **Production impact:** Privilege-escalation risk if authentication context handling changes or an endpoint is refactored.
- **Recommended fix:** Permit only public authentication endpoints explicitly and require `ROLE_ADMIN` for admin registration; use method security as defense in depth.

### C-3: Booking status is vulnerable to insecure direct object reference
- **File:** `src/main/java/com/vof/config/SecurityConfig.java:36`; `src/main/java/com/vof/controller/BookingController.java:20-24`; `src/main/java/com/vof/service/impl/BookingServiceImpl.java:64-73`
- **Problem:** `GET /api/bookings/{bookingId}` is public and booking IDs are predictable in the current generator.
- **Why it is wrong:** A booking ID is not an authorization mechanism. The response exposes a customer's booking workflow state to unauthenticated callers.
- **Production impact:** Enumeration and disclosure of customer booking information.
- **Recommended fix:** Associate bookings with the authenticated user and enforce ownership (or administrator role) before returning them. Use non-enumerable public identifiers if a public lookup is a business requirement.

### C-4: Payment-proof upload has no ownership check
- **File:** `src/main/java/com/vof/controller/PaymentController.java:17-22`; `src/main/java/com/vof/service/impl/PaymentServiceImpl.java:22-40`
- **Problem:** Any authenticated account can upload a payment proof for any booking ID.
- **Why it is wrong:** Authentication does not establish authorization to alter another customer's booking/payment record.
- **Production impact:** Fraudulent proof attachment, denial of service to a real customer, and tampering with financial records.
- **Recommended fix:** Persist booking ownership and verify the current principal owns the booking; allow administrators only where operationally intended. Reject a second proof deterministically.

### C-5: Refresh-token lifecycle is unsafe and leaks sensitive token material
- **File:** `src/main/java/com/vof/controller/AuthController.java:52-64`; `src/main/java/com/vof/service/RefreshTokenService.java:18-34`; `src/main/java/com/vof/exception/TokenRefreshException.java:8`
- **Problem:** Multiple refresh tokens may exist for a user despite a one-to-one mapping; raw token values are included in exception messages; rotation deletion and creation are separate transactions.
- **Why it is wrong:** This allows unbounded active sessions, leaks bearer credentials through error responses/logs, and risks inconsistent rotation on failure.
- **Production impact:** Token replay, stolen-token disclosure, and failed logout/revocation semantics.
- **Recommended fix:** Store a hash of a cryptographically random refresh token, use a single transaction for rotation, revoke previous tokens according to an explicit session policy, and never include tokens in errors.

## High Priority Issues

### H-1: Login request accepts a client-supplied role field
- **File:** `src/main/java/com/vof/dto/request/SignupRequest.java:12`; request brief additionally identifies a role field in login flows
- **Problem:** The registration DTO exposes `role`, even though the current controller ignores it. This contradicts the intended privilege model and invites future mass-assignment bugs. (The checked-in `LoginRequest` no longer contains it.)
- **Why it is wrong:** Clients must never choose application privileges; roles must be assigned by server-side policy.
- **Production impact:** A later mapper/controller change could immediately create arbitrary administrators.
- **Recommended fix:** Remove the field from public sign-up/login DTOs and keep role assignment exclusively in trusted admin workflows.

### H-2: JWT configuration is weakly typed, accepts arbitrary secret bytes, and lacks issuer/audience validation
- **File:** `src/main/java/com/vof/util/JwtUtil.java:18-32`
- **Problem:** Secret conversion uses the platform default charset; token claims contain no issuer/audience; token type is not differentiated.
- **Why it is wrong:** Tokens should be scoped to the issuing application and secrets should use explicit encoding and length validation.
- **Production impact:** Cross-environment token acceptance and fragile configuration; insufficient claim validation as the system evolves.
- **Recommended fix:** Bind typed JWT properties, use Base64 decoding, validate key strength, issuer, and audience, and issue short-lived access tokens with a fixed token-type claim.

### H-3: Authentication filter silently continues on invalid security state
- **File:** `src/main/java/com/vof/config/JwtRequestFilter.java:28-40`
- **Problem:** It catches all exceptions, logs the exception object at error level, and continues processing; it also overwrites an existing authentication.
- **Why it is wrong:** Broad catches hide operational failures and may disclose implementation details in logs. A filter should only authenticate when no context is already established.
- **Production impact:** Difficult incident diagnosis and inconsistent authentication behavior.
- **Recommended fix:** Use constructor injection, only populate an empty context, log sanitized reason codes, and rely on the entry point for protected endpoints.

### H-4: User/account ownership is absent from the data model
- **File:** `src/main/java/com/vof/entity/Booking.java:10-19`; `src/main/java/com/vof/service/impl/BookingServiceImpl.java:27-61`
- **Problem:** A booking records supplied contact data but no creating user.
- **Why it is wrong:** The service cannot enforce access control for status or payment operations.
- **Production impact:** IDORs and inability to audit who created a booking.
- **Recommended fix:** Add a non-null `created_by_user_id` foreign key for newly created bookings, indexes for ownership lookups, and an ownership authorization helper.

### H-5: File upload accepts arbitrary content and buffers it fully in memory
- **File:** `src/main/java/com/vof/service/impl/CloudinaryServiceImpl.java:14-17`; `src/main/java/com/vof/dto/request/PaymentProofRequest.java:10-15`
- **Problem:** No content-type, extension, non-empty, or size validation is applied before `getBytes()` uploads to Cloudinary.
- **Why it is wrong:** Client content type is untrusted and full-buffering increases memory-pressure risk.
- **Production impact:** Malicious/unexpected uploads, excessive heap use, and storage abuse.
- **Recommended fix:** Allow only configured image MIME types, impose an application-level size limit, use restricted upload parameters/folders, and consider malware scanning in the production upload pipeline.

### H-6: Payment proofs can be duplicated and amount is not verified against the booking
- **File:** `src/main/java/com/vof/service/impl/PaymentServiceImpl.java:26-39`; `src/main/java/com/vof/entity/PaymentProof.java:7-12`
- **Problem:** A new proof is always saved, even when one already exists; the submitted amount is trusted and `Double` is used for currency.
- **Why it is wrong:** Database uniqueness can throw an opaque error, and floating-point values are inappropriate for money.
- **Production impact:** Payment reconciliation failures and inconsistent/duplicate proof records.
- **Recommended fix:** Check current proof/state before upload, calculate expected amount server-side, and model prices/amounts as `BigDecimal` with precision and scale.

### H-7: Booking ID generator is not safe across restarts or multiple instances
- **File:** `src/main/java/com/vof/util/BookingIdGenerator.java:7-15`
- **Problem:** An in-memory counter resets on restart and does not coordinate replicas.
- **Why it is wrong:** A unique database constraint only detects the collision after the business operation attempts to persist.
- **Production impact:** Booking creation failures and potential retries producing inconsistent customer experience.
- **Recommended fix:** Use a database sequence/identity, UUID/ULID public ID, or a database-backed sequence with retry handling.

### H-8: Missing request validation on package administration and weak validation elsewhere
- **File:** `src/main/java/com/vof/dto/request/CreatePackageRequest.java:4-12`; `ItineraryRequest.java:4`; `MealRequest.java:4`; `PaymentProofRequest.java:10-15`; `ContactRequest.java:8-12`
- **Problem:** Package DTOs have no constraints or nested `@Valid`; payment amount allows negative/NaN values; text fields have no length limits; phone validation only checks length.
- **Why it is wrong:** Controllers receive structurally invalid data and unbounded strings.
- **Production impact:** Invalid records, database exceptions, stored abuse payloads, and weak API contracts.
- **Recommended fix:** Add null/blank/size/range/pattern constraints, nested validation, and validate multipart metadata.

### H-9: JPA entities are returned directly by public API services
- **File:** `src/main/java/com/vof/service/FaqService.java:5`; `GalleryService.java:5`; `ReviewService.java:5`; implementations and `MiscController.java:16-18`
- **Problem:** Public endpoints serialize persistence entities rather than API response DTOs.
- **Why it is wrong:** Entity schema becomes a public contract and lazy relationships/annotations can leak accidentally.
- **Production impact:** Breaking API changes, accidental data exposure, and serialization failures once relationships evolve.
- **Recommended fix:** Define response DTOs and map all API output explicitly.

### H-10: N+1 queries and lazy-initialization risk in package/booking mappings
- **File:** `src/main/java/com/vof/service/impl/PackageServiceImpl.java:72-73`; `BookingServiceImpl.java:77-81`; `PackageMapper.java:18-22`; `BookingMapper.java:16-18`
- **Problem:** `findAll()` is followed by mapping of lazy images/itinerary/meal/package relations.
- **Why it is wrong:** Each row can trigger additional select statements; non-transactional package reads may throw `LazyInitializationException`.
- **Production impact:** Poor latency/load behavior and runtime failures depending on Open Session in View settings.
- **Recommended fix:** Disable OSIV, add read-only transactions, and use entity graphs or DTO projections tailored to each query.

### H-11: Schema generation is unsafe for production
- **File:** `src/main/resources/application.properties:13-16`
- **Problem:** `spring.jpa.hibernate.ddl-auto=update` and SQL logging are enabled by default.
- **Why it is wrong:** Hibernate updates are not an auditable migration strategy and logs can reveal personal data/queries.
- **Production impact:** Uncontrolled schema changes and sensitive operational data in logs.
- **Recommended fix:** Use versioned migrations (Flyway/Liquibase), `validate` in production, and configure SQL logging per non-production profile only.

### H-12: No CORS policy, authorization-denied handler, or standardized authentication response
- **File:** `src/main/java/com/vof/config/SecurityConfig.java:25-38`; `JwtAuthenticationEntryPoint.java:17-20`
- **Problem:** Security configuration leaves CORS implicit, has no access-denied handler, and uses `sendError` rather than the standard API envelope.
- **Why it is wrong:** Browser clients need an explicit origin policy; 401 and 403 errors should be indistinguishable in shape from other API errors.
- **Production impact:** Deployment-specific browser failures and inconsistent client error handling.
- **Recommended fix:** Bind an allow-list of origins from configuration, add a JSON access-denied handler, and return the common error response for authentication failures.

## Medium Issues

### M-1: Race conditions in user registration and role initialization
- **File:** `AuthController.java:66-75`; `DataLoader.java:19-29`
- **Problem:** Check-then-insert registration can race; role creation uses `count()` then inserts both roles.
- **Why it is wrong:** Application checks do not replace database constraints or atomic upserts.
- **Production impact:** Constraint errors/500s during concurrent requests or partial role setup.
- **Recommended fix:** Catch/translate integrity violations, use per-role `find-or-create` transactions, and retain unique constraints.

### M-2: Administrative booking state transitions lack transition rules and payment review
- **File:** `BookingServiceImpl.java:85-111`
- **Problem:** Any current status can be approved/rejected and no payment-proof verification is required.
- **Why it is wrong:** State changes must be explicit business invariants.
- **Production impact:** Invalid booking lifecycle states and possible fulfillment without verified payment.
- **Recommended fix:** Model legal transitions, require the intended payment status, and record actor/time/audit reason.

### M-3: Package slug is non-unique and generated ambiguously
- **File:** `Package.java:20`; `PackageServiceImpl.java:75`
- **Problem:** Slug is neither constrained nor normalized beyond spaces.
- **Why it is wrong:** Route/readability identifiers require stable uniqueness.
- **Production impact:** Ambiguous public URLs and duplicate content references.
- **Recommended fix:** Add a unique constraint/index, normalize punctuation, and resolve duplicates deterministically.

### M-4: Entity column constraints and indexes are incomplete
- **File:** `Booking.java`, `PaymentProof.java`, `ContactMessage.java`, `Package.java`, `User.java`
- **Problem:** Most required fields are nullable/unbounded; frequent lookup and foreign-key fields lack explicit indexes; version columns are absent.
- **Why it is wrong:** Application validation is bypassable and concurrent writes can overwrite each other.
- **Production impact:** Bad records, slower lookup paths, and lost updates.
- **Recommended fix:** Add database null/length/check/unique constraints, indexes for lookup/foreign-key columns, and `@Version` where concurrent updates are possible.

### M-5: Transaction boundaries are inconsistent
- **File:** `RefreshTokenService.java:21-34`; `PackageServiceImpl.java:71-73`; `PaymentServiceImpl.java:24-40`
- **Problem:** refresh rotation spans controller/service calls, read mappings lack explicit read-only transactions, and an external Cloudinary upload runs inside a database transaction.
- **Why it is wrong:** Transactions should define atomic persistence only; external calls can keep database resources open and cannot be rolled back.
- **Production impact:** partial updates, lock contention, and orphaned remote files on failure.
- **Recommended fix:** Encapsulate rotation in one service transaction; use read-only transactions for mapping; design a compensating upload/delete workflow or durable outbox.

### M-6: Deleting a package with bookings has undefined referential behavior
- **File:** `PackageServiceImpl.java:70`; `Package.java:35-36`; `Booking.java:14`
- **Problem:** The service deletes a package without checking associated bookings and the relation is nullable.
- **Why it is wrong:** Historical booking records must retain their purchased package context.
- **Production impact:** foreign-key failures or loss of referential/history integrity.
- **Recommended fix:** prevent deletion when bookings exist, or soft-delete/retire packages; make booking package relationship non-null.

### M-7: Global validation handler incorrectly assumes every error is a field error
- **File:** `GlobalExceptionHandler.java:30-35`
- **Problem:** It casts every `ObjectError` to `FieldError`.
- **Why it is wrong:** Class-level constraints produce `ObjectError`, causing the error handler itself to fail.
- **Production impact:** malformed 500 responses for otherwise valid 400 validation cases.
- **Recommended fix:** Handle `FieldError` and global errors separately; also translate malformed JSON, constraint violations, multipart errors, auth errors, and integrity violations.

### M-8: Sensitive/internal details are exposed by exception messages
- **File:** `TokenRefreshException.java:8`; `ResourceNotFoundException.java:5`; `AuthController.java:46`
- **Problem:** Raw tokens and unexpected internal assertions can reach clients.
- **Why it is wrong:** Error responses should be stable, minimal, and free of credentials/internal implementation.
- **Production impact:** Credential leakage and information disclosure.
- **Recommended fix:** Use safe, fixed client messages plus structured server-side logs with correlation IDs.

### M-9: No logout endpoint or refresh-token session revocation policy
- **File:** `AuthController.java`; `RefreshTokenService.java`
- **Problem:** Clients cannot revoke a refresh token through the API.
- **Why it is wrong:** Logout must invalidate the durable credential, not only remove it from a browser.
- **Production impact:** Stolen or shared refresh tokens remain valid until expiry.
- **Recommended fix:** Add an authenticated logout/revoke endpoint and revoke refresh tokens by user/session as policy dictates.

## Low Priority Issues

### L-1: Constructor injection is not used consistently
- **File:** `JwtRequestFilter.java:20-21`; `SecurityConfig.java:19-20`; `RefreshTokenService.java:18-19`
- **Problem:** Required dependencies use field injection.
- **Why it is wrong:** It obscures dependencies and makes unit testing harder.
- **Production impact:** Reduced testability and easier accidental null/circular wiring.
- **Recommended fix:** Use final fields and constructor injection everywhere.

### L-2: API response and REST semantics are inconsistent
- **File:** all controllers, especially `MiscController.java:16-21` and `AdminPackageController.java:27-31`
- **Problem:** Raw/wildcard response types, inconsistent message punctuation, and `200 OK` for delete/contact creation.
- **Why it is wrong:** Clients need a stable documented contract and standard resource semantics.
- **Production impact:** Harder integrations and brittle generated SDKs.
- **Recommended fix:** Use generic envelopes, return `201 Created` for creation, `204 No Content` for deletion where no body is needed, and document schemas/statuses in OpenAPI.

### L-3: OpenAPI only declares a scheme, not endpoint contracts
- **File:** `OpenApiConfig.java:9-11`; controllers
- **Problem:** The API lacks operation summaries, response definitions, request examples, and explicit public/protected behavior.
- **Why it is wrong:** A security scheme alone does not make an integration contract.
- **Production impact:** Incorrect client usage and accidental invocation of privileged APIs.
- **Recommended fix:** Add `@Operation`, `@ApiResponses`, request/response schemas, and `@SecurityRequirement` consistently.

### L-4: No tests cover security, refresh-token rotation, authorization, validation, or upload rejection
- **File:** `src/test/java/com/vof/service/impl/BookingServiceImplTest.java`
- **Problem:** Only two happy-path service tests exist.
- **Why it is wrong:** The highest-risk paths lack executable regression protection.
- **Production impact:** Security regressions can ship unnoticed.
- **Recommended fix:** Add controller/security integration tests and focused unit tests for ownership, token rotation, validation, and state transitions.

### L-5: Dependency versions are stale relative to the current ecosystem
- **File:** `pom.xml:8, 49-63, 65-69`
- **Problem:** Spring Boot 3.2.0, JJWT 0.11.5, and springdoc 2.1.0 are pinned to older releases.
- **Why it is wrong:** Older frameworks may miss fixes and compatibility improvements.
- **Production impact:** Increased CVE exposure and less-supported runtime behavior.
- **Recommended fix:** Upgrade after compatibility testing and enable automated dependency/security scanning.

