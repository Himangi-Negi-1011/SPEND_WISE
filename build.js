const fs = require('fs');
const path = require('path');

console.log('[SpendWise Build] Starting web asset preparation for Vercel deployment...');

const rootIndex = path.join(__dirname, 'index.html');
const publicDir = path.join(__dirname, 'public');
const distDir = path.join(__dirname, 'dist');
const publicIndex = path.join(publicDir, 'index.html');
const distIndex = path.join(distDir, 'index.html');

if (!fs.existsSync(rootIndex)) {
  console.error('[SpendWise Build ERROR] index.html not found in root directory!');
  process.exit(1);
}

// Ensure public/ and dist/ directories exist
if (!fs.existsSync(publicDir)) {
  fs.mkdirSync(publicDir, { recursive: true });
}
if (!fs.existsSync(distDir)) {
  fs.mkdirSync(distDir, { recursive: true });
}

// Copy index.html to public/ and dist/
fs.copyFileSync(rootIndex, publicIndex);
fs.copyFileSync(rootIndex, distIndex);

console.log(`[SpendWise Build] Successfully copied index.html to:`);
console.log(`  - ${publicIndex}`);
console.log(`  - ${distIndex}`);

// Copy assets if they exist
const publicAssetsDir = path.join(publicDir, 'assets');
const distAssetsDir = path.join(distDir, 'assets');

function copyDirRecursive(src, dest) {
  if (!fs.existsSync(src)) return;
  if (!fs.existsSync(dest)) fs.mkdirSync(dest, { recursive: true });
  
  const entries = fs.readdirSync(src, { withFileTypes: true });
  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);
    if (entry.isDirectory()) {
      copyDirRecursive(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

if (fs.existsSync(publicAssetsDir)) {
  copyDirRecursive(publicAssetsDir, distAssetsDir);
  console.log('[SpendWise Build] Assets directory successfully synchronized to dist/assets.');
}

console.log('[SpendWise Build] Web build completed successfully for Vercel and Web deployment!');
