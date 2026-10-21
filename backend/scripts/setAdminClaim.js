/**
 * Script to grant admin privileges to a user email or UID.
 * 
 * Usage:
 * 1. Download service-account-key.json from Firebase Console -> Project Settings -> Service accounts
 * 2. Run: node backend/scripts/setAdminClaim.js user@noordaily.com
 */

const admin = require('firebase-admin');
const serviceAccount = require('./service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

async function grantAdmin(identifier) {
  try {
    let userRecord;
    if (identifier.includes('@')) {
      userRecord = await admin.auth().getUserByEmail(identifier);
    } else {
      userRecord = await admin.auth().getUser(identifier);
    }

    await admin.auth().setCustomUserClaims(userRecord.uid, { admin: true });
    console.log(`✅ Success! Custom claim { admin: true } granted to: ${userRecord.email} (UID: ${userRecord.uid})`);
    process.exit(0);
  } catch (error) {
    console.error('❌ Failed to grant admin claims:', error);
    process.exit(1);
  }
}

const targetUser = process.argv[2];
if (!targetUser) {
  console.error('Please specify target user email or UID. Example: node setAdminClaim.js admin@noordaily.com');
  process.exit(1);
}

grantAdmin(targetUser);
