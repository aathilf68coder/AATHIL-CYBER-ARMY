const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_sec_01";
const BOB_UID = "bob_sec_02";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validRegistrationPayload(uid, timestamp = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    customerName: "Aathil Cyber Client",
    mobileNumber: "+94771234567",
    authorizedTarget: "https://client-portal.example.com",
    instagramUsername: "@client_target",
    authorizationDocName: "signed_scope_auth.pdf",
    authorizationDocUri: "content://com.android.providers.downloads/document/42",
    referenceImageNames: ["evidence_01.png", "evidence_02.png"],
    authorizationConfirmed: true,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

function validOrderPayload(uid, timestamp = new Date(Date.now() - 5000)) {
  return {
    userId: uid,
    orderCode: "AEG-2026-8F4A",
    sessionToken: "SES-991A-7C",
    serviceId: "website_vuln_assessment",
    serviceTitle: "Website Vulnerability Assessment",
    serviceCategory: "WEB_PENTEST",
    customerName: "Aathil Cyber Client",
    mobileNumber: "+94771234567",
    authorizedTarget: "https://client-portal.example.com",
    instagramUsername: "@client_target",
    authorizationDocName: "signed_scope_auth.pdf",
    referenceImagesCount: 2,
    status: "SCOPE_AND_AUTH_CONFIRMED",
    paymentInstructionsUnlocked: true,
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

test("Unauthenticated user cannot read or write registrations or orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("registrations").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(ALICE_UID)
    )
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").set(
      validOrderPayload(ALICE_UID)
    )
  );
});

test("Authenticated owner: valid registration create, read, and partial update succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(ALICE_UID, created)
    )
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").update({
      authorizedTarget: "https://updated-target.example.com",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Registration security bounds: reject ghost fields, @DocumentId id, future timestamps, and false confirmation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // Ghost field injection
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set({
      ...validRegistrationPayload(ALICE_UID),
      isAdmin: true,
    })
  );

  // @DocumentId `id` leaked into payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set({
      ...validRegistrationPayload(ALICE_UID),
      id: "active",
    })
  );

  // Tampered future timestamp
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(ALICE_UID, new Date(Date.now() + 60000))
    )
  );

  // Missing authorization confirmation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set({
      ...validRegistrationPayload(ALICE_UID),
      authorizationConfirmed: false,
    })
  );

  // Blank authorization document name
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set({
      ...validRegistrationPayload(ALICE_UID),
      authorizationDocName: "",
    })
  );
});

test("Registration update preserves immutable createdAt and userId", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(ALICE_UID, created)
    )
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").update({
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("registrations").doc("active").update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Orders: owner can create, list, get, delete; cannot update or spoof status", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_001").set(
      validOrderPayload(ALICE_UID)
    )
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_001").get()
  );

  // Orders are immutable once confirmed
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_001").update({
      serviceTitle: "Tampered",
    })
  );

  // Invalid status rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_002").set({
      ...validOrderPayload(ALICE_UID),
      status: "BYPASSED",
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_001").delete()
  );
});

test("Cross-user isolation: Alice cannot read, write, or delete Bob's registrations or orders", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(BOB_UID)
    );
    await db.collection("users").doc(BOB_UID).collection("orders").doc("bob_order_1").set(
      validOrderPayload(BOB_UID)
    );
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("registrations").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("registrations").doc("active").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("registrations").doc("active").set(
      validRegistrationPayload(ALICE_UID)
    )
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("orders").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("orders").doc("bob_order_1").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("orders").doc("bob_order_1").delete());
});
